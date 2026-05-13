package local.alejandrogb.metricsserversdesktop.ui.dialog.grupos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import local.alejandrogb.metricsserversdesktop.models.Grupo;
import local.alejandrogb.metricsserversdesktop.models.Permiso;
import local.alejandrogb.metricsserversdesktop.models.PermissionMap;
import local.alejandrogb.metricsserversdesktop.models.Seccion;
import local.alejandrogb.metricsserversdesktop.models.Session;
import local.alejandrogb.metricsserversdesktop.services.auth.SessionHolder;
import local.alejandrogb.metricsserversdesktop.services.crud.PermisoService;
import local.alejandrogb.metricsserversdesktop.services.crud.SeccionService;
import local.alejandrogb.metricsserversdesktop.services.permission.PermissionGuard;
import local.alejandrogb.metricsserversdesktop.ui.util.SwingUtils;

/**
 * Diálogo para crear/editar grupos. Tres pestañas:
 * 1. Datos básicos (nombre, dn, superadmin)
 * 2. Permisos globales — checkboxes editables si el usuario tiene MODIFY_USER
 * 3. Permisos por sección — selector de sección + checkboxes
 *
 * Incluye protección de auto-privación: no permite eliminar MODIFY_USER del
 * propio grupo del usuario que está editando.
 */
public class GrupoDialog extends JDialog {

	private static final long serialVersionUID = 957097414028531146L;

	// ── Tipos públicos ────────────────────────────────────────────────────────

	/**
	 * Cambios de permisos que el caller debe aplicar via GrupoService.
	 * globalAdd/globalRemove usan IDs de Permiso.
	 * sectionAdd/sectionRemove mapean seccionId → lista de IDs de Permiso.
	 */
	public record PermisoChanges(
			List<Integer> globalAdd,
			List<Integer> globalRemove,
			Map<Integer, List<Integer>> sectionAdd,
			Map<Integer, List<Integer>> sectionRemove) {

		public boolean isEmpty() {
			return globalAdd.isEmpty() && globalRemove.isEmpty()
					&& sectionAdd.isEmpty() && sectionRemove.isEmpty();
		}
	}

	// ── Tipos internos ────────────────────────────────────────────────────────

	private static class PermisoRow {
		final Permiso permiso;
		boolean checked;

		PermisoRow(Permiso p, boolean checked) {
			this.permiso = p;
			this.checked = checked;
		}
	}

	private static class CheckTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private static final String[] COLS = { "✓", "Permiso", "Ámbito" };
		private final boolean editable;
		private List<PermisoRow> rows = new ArrayList<>();

		CheckTableModel(boolean editable) {
			this.editable = editable;
		}

		void setRows(List<PermisoRow> r) {
			rows = r != null ? r : new ArrayList<>();
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		public int getColumnCount() {
			return COLS.length;
		}

		@Override
		public String getColumnName(int c) {
			return COLS[c];
		}

		@Override
		public Class<?> getColumnClass(int c) {
			return c == 0 ? Boolean.class : String.class;
		}

		@Override
		public boolean isCellEditable(int r, int c) {
			return editable && c == 0;
		}

		@Override
		public Object getValueAt(int r, int c) {
			PermisoRow row = rows.get(r);
			return switch (c) {
			case 0 -> row.checked;
			case 1 -> row.permiso.getNombre() != null ? row.permiso.getNombre() : "—";
			case 2 -> row.permiso.getAmbito() != null ? row.permiso.getAmbito().getNombre() : "—";
			default -> null;
			};
		}

		@Override
		public void setValueAt(Object val, int r, int c) {
			if (c == 0 && val instanceof Boolean b) {
				rows.get(r).checked = b;
				fireTableCellUpdated(r, c);
			}
		}
	}

	// ── Campos de datos del diálogo ───────────────────────────────────────────

	private final Grupo existing;
	private boolean confirmed = false;
	private final boolean canEditPermisos;
	private final int myGroupId;
	private PermisoChanges permisoChanges;

	// UI – datos básicos
	private JTextField txtNombre;
	private JTextField txtDn;
	private JCheckBox chkSuperAdmin;

	// UI – permisos
	private final CheckTableModel globalPermisoModel;
	private final CheckTableModel sectPermisoModel;
	private JComboBox<Seccion> cmbSecciones;

	// Estado de permisos (cargado en background)
	private List<Permiso> allPermisos = new ArrayList<>();
	private Map<String, Permiso> permisoByKey = new HashMap<>();
	private List<Seccion> allSecciones = new ArrayList<>();
	private List<PermisoRow> globalRows = new ArrayList<>();
	private Map<Integer, List<PermisoRow>> sectionRows = new HashMap<>();
	// IDs enteros: GET /grupos/{id} devuelve PermissionMap[int], no strings
	private Set<Integer> initialGlobalIds = new HashSet<>();
	private Map<Integer, Set<Integer>> initialSectionIds = new HashMap<>();
	private int selectedSeccionId = -1;

	// ── Constructor ───────────────────────────────────────────────────────────

	public GrupoDialog(Window parent, Grupo existing) {
		super(parent,
				existing == null ? "Nuevo Grupo"
						: "Editar Grupo — " + (existing.getNombre() != null ? existing.getNombre() : ""),
				ModalityType.APPLICATION_MODAL);
		this.existing = existing;

		Session session = SessionHolder.getInstance().getSession();
		myGroupId = (session != null && session.getGrupo() != null) ? session.getGrupo().getId() : -1;
		canEditPermisos = PermissionGuard.getInstance().canManageUsuarios();

		globalPermisoModel = new CheckTableModel(canEditPermisos);
		sectPermisoModel = new CheckTableModel(canEditPermisos);

		buildUI();
		pack();
		setMinimumSize(new Dimension(600, 480));
		setResizable(true);
		setLocationRelativeTo(parent);

		loadPermisosYSecciones();
	}

	// ── Construcción de la interfaz ───────────────────────────────────────────

	private void buildUI() {
		JPanel main = new JPanel(new BorderLayout(0, 8));
		main.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

		JTabbedPane tabs = new JTabbedPane();

		// ── Tab 1: Datos básicos ──────────────────────────────────────────────
		JPanel tabBasic = new JPanel(new GridBagLayout());
		tabBasic.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		tabBasic.add(new JLabel("Nombre *"), SwingUtils.gbc(0, 0, GridBagConstraints.NONE, 0));
		txtNombre = new JTextField(24);
		tabBasic.add(txtNombre, SwingUtils.gbc(1, 0, GridBagConstraints.HORIZONTAL, 1.0));

		tabBasic.add(new JLabel("DN (LDAP)"), SwingUtils.gbc(0, 1, GridBagConstraints.NONE, 0));
		txtDn = new JTextField(24);
		if (existing != null) {
			txtDn.setEditable(false);
			txtDn.setBackground(new Color(0xF2F2F2));
			txtDn.setToolTipText("El DN se gestiona externamente (LDAP) y no puede editarse desde esta aplicación.");
		} else {
			txtDn.setToolTipText("Nombre Distinguido (DN) de Active Directory. Opcional.");
		}
		tabBasic.add(txtDn, SwingUtils.gbc(1, 1, GridBagConstraints.HORIZONTAL, 1.0));

		tabBasic.add(new JLabel("Superadmin"), SwingUtils.gbc(0, 2, GridBagConstraints.NONE, 0));
		chkSuperAdmin = new JCheckBox();
		tabBasic.add(chkSuperAdmin, SwingUtils.gbc(1, 2, GridBagConstraints.NONE, 0));

		GridBagConstraints fill = new GridBagConstraints();
		fill.gridx = 0;
		fill.gridy = 3;
		fill.gridwidth = 2;
		fill.weighty = 1.0;
		fill.fill = GridBagConstraints.VERTICAL;
		tabBasic.add(Box.createVerticalGlue(), fill);

		tabs.addTab("Datos básicos", tabBasic);

		// ── Tab 2: Permisos globales ──────────────────────────────────────────
		JTable tblGlobal = new JTable(globalPermisoModel);
		tblGlobal.setRowHeight(26);
		tblGlobal.setAutoCreateRowSorter(true);
		tblGlobal.getColumnModel().getColumn(0).setPreferredWidth(35);
		tblGlobal.getColumnModel().getColumn(0).setMaxWidth(35);
		tblGlobal.getColumnModel().getColumn(1).setPreferredWidth(190);
		tblGlobal.getColumnModel().getColumn(2).setPreferredWidth(100);

		JPanel tabGlobal = new JPanel(new BorderLayout());
		tabGlobal.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		tabGlobal.add(new JScrollPane(tblGlobal), BorderLayout.CENTER);

		String globalNoteText = canEditPermisos
				? "  Marca los permisos que se asignan globalmente al grupo."
				: "  Los permisos globales son de solo lectura.";
		tabGlobal.add(noteLabel(globalNoteText), BorderLayout.SOUTH);

		tabs.addTab("Permisos globales", tabGlobal);

		// ── Tab 3: Permisos por sección ───────────────────────────────────────
		cmbSecciones = new JComboBox<>();
		cmbSecciones.addActionListener(e -> onSectionChange());

		JTable tblSect = new JTable(sectPermisoModel);
		tblSect.setRowHeight(26);
		tblSect.setAutoCreateRowSorter(true);
		tblSect.getColumnModel().getColumn(0).setPreferredWidth(35);
		tblSect.getColumnModel().getColumn(0).setMaxWidth(35);
		tblSect.getColumnModel().getColumn(1).setPreferredWidth(190);
		tblSect.getColumnModel().getColumn(2).setPreferredWidth(100);

		JPanel northSect = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		northSect.add(new JLabel("Sección:"));
		northSect.add(cmbSecciones);

		JPanel tabSections = new JPanel(new BorderLayout());
		tabSections.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		tabSections.add(northSect, BorderLayout.NORTH);
		tabSections.add(new JScrollPane(tblSect), BorderLayout.CENTER);

		String sectNoteText = canEditPermisos
				? "  Selecciona una sección y marca los permisos para esa sección."
				: "  Los permisos por sección son de solo lectura.";
		tabSections.add(noteLabel(sectNoteText), BorderLayout.SOUTH);

		tabs.addTab("Permisos por sección", tabSections);

		// ── Rellenar campos de texto ──────────────────────────────────────────
		if (existing != null) {
			txtNombre.setText(existing.getNombre() != null ? existing.getNombre() : "");
			txtDn.setText(existing.getDn() != null ? existing.getDn() : "");
			chkSuperAdmin.setSelected(Boolean.TRUE.equals(existing.isSuperAdmin()));
		}

		main.add(tabs, BorderLayout.CENTER);

		// ── Botones ───────────────────────────────────────────────────────────
		JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton btnCancel = new JButton("Cancelar");
		JButton btnOk = new JButton(existing == null ? "Crear" : "Guardar");
		btnOk.setBackground(new Color(0x2C7BE5));
		btnOk.setForeground(Color.WHITE);
		btns.add(btnCancel);
		btns.add(btnOk);
		main.add(btns, BorderLayout.SOUTH);

		btnCancel.addActionListener(e -> dispose());
		btnOk.addActionListener(e -> doConfirm());

		setContentPane(main);
	}

	private static JLabel noteLabel(String text) {
		JLabel lbl = new JLabel(text);
		lbl.setFont(lbl.getFont().deriveFont(11f));
		lbl.setForeground(Color.GRAY);
		return lbl;
	}

	// ── Cambio de sección ─────────────────────────────────────────────────────

	private void onSectionChange() {
		Seccion sec = (Seccion) cmbSecciones.getSelectedItem();
		if (sec == null) {
			sectPermisoModel.setRows(new ArrayList<>());
			selectedSeccionId = -1;
			return;
		}
		if (sec.getId() == selectedSeccionId)
			return;
		selectedSeccionId = sec.getId();
		sectPermisoModel.setRows(sectionRows.getOrDefault(selectedSeccionId, new ArrayList<>()));
	}

	// ── Carga de permisos en background ───────────────────────────────────────

	private void loadPermisosYSecciones() {
		// En edición sin datos de permisos no hay nada que cargar
		if (existing != null && existing.getPermisos() == null)
			return;

		SwingUtils.runAsync(() -> {
			List<Permiso> permisos = new PermisoService().findAll();
			List<Seccion> secciones = new SeccionService().findAll();
			return new Object[] { permisos, secciones };
		}, result -> {
			@SuppressWarnings("unchecked")
			List<Permiso> permisos = (List<Permiso>) result[0];
			@SuppressWarnings("unchecked")
			List<Seccion> secciones = (List<Seccion>) result[1];

			allPermisos = permisos;
			allSecciones = secciones;

			// compound key → Permiso (para safetyCheck)
			permisoByKey = new HashMap<>();
			for (Permiso p : allPermisos) {
				if (p.getNombre() != null && p.getAmbito() != null) {
					permisoByKey.put(p.getNombre() + "_" + p.getAmbito().getNombre(), p);
				}
			}

			// En edición: estado inicial desde el grupo existente (IDs enteros, coercionados a String)
			// En creación: estado inicial vacío → todos los checkboxes desmarcados
			if (existing != null) {
				PermissionMap pm = existing.getPermisos();
				initialGlobalIds = new HashSet<>();
				if (pm.getGlobal() != null) {
					for (String s : pm.getGlobal()) {
						try {
							initialGlobalIds.add(Integer.parseInt(s));
						} catch (NumberFormatException ignored) {
						}
					}
				}
				Map<Integer, List<String>> initSects = pm.getSections() != null ? pm.getSections() : Map.of();
				initialSectionIds = new HashMap<>();
				for (Map.Entry<Integer, List<String>> e : initSects.entrySet()) {
					Set<Integer> ids = new HashSet<>();
					for (String s : e.getValue()) {
						try {
							ids.add(Integer.parseInt(s));
						} catch (NumberFormatException ignored) {
						}
					}
					initialSectionIds.put(e.getKey(), ids);
				}
			}
			// Si existing == null: initialGlobalIds e initialSectionIds quedan vacíos (ya inicializados)

			// Filas globales (todos los permisos; marcados según estado inicial)
			globalRows = new ArrayList<>();
			for (Permiso p : allPermisos) {
				if (p.getNombre() == null || p.getAmbito() == null)
					continue;
				globalRows.add(new PermisoRow(p, initialGlobalIds.contains(p.getId())));
			}
			globalPermisoModel.setRows(globalRows);

			// Filas por sección
			sectionRows = new HashMap<>();
			for (Seccion sec : allSecciones) {
				Set<Integer> secPermIds = initialSectionIds.getOrDefault(sec.getId(), Set.of());
				List<PermisoRow> rows = new ArrayList<>();
				for (Permiso p : allPermisos) {
					if (p.getNombre() == null || p.getAmbito() == null)
						continue;
					rows.add(new PermisoRow(p, secPermIds.contains(p.getId())));
				}
				sectionRows.put(sec.getId(), rows);
			}

			// Poblar combo de secciones (ActionListener carga la tabla automáticamente)
			cmbSecciones.removeAllItems();
			for (Seccion s : allSecciones) {
				cmbSecciones.addItem(s);
			}

		}, err -> SwingUtils.showError(this, "Error al cargar permisos: " + err.getMessage()));
	}

	// ── Confirmación ──────────────────────────────────────────────────────────

	private void doConfirm() {
		if (txtNombre.getText().trim().isEmpty()) {
			SwingUtils.showError(this, "El nombre del grupo es obligatorio.");
			txtNombre.requestFocus();
			return;
		}

		if (canEditPermisos && !globalRows.isEmpty()) {
			permisoChanges = computePermisoChanges();
			// Protección de auto-privación solo en edición (en creación el grupo aún no existe)
			if (existing != null && !safetyCheck(permisoChanges))
				return;
		} else {
			permisoChanges = new PermisoChanges(List.of(), List.of(), Map.of(), Map.of());
		}

		confirmed = true;
		dispose();
	}

	private PermisoChanges computePermisoChanges() {
		List<Integer> addGlobal = new ArrayList<>();
		List<Integer> removeGlobal = new ArrayList<>();
		for (PermisoRow row : globalRows) {
			boolean wasChecked = initialGlobalIds.contains(row.permiso.getId());
			if (row.checked && !wasChecked)
				addGlobal.add(row.permiso.getId());
			else if (!row.checked && wasChecked)
				removeGlobal.add(row.permiso.getId());
		}

		Map<Integer, List<Integer>> addSections = new HashMap<>();
		Map<Integer, List<Integer>> removeSections = new HashMap<>();
		for (Map.Entry<Integer, List<PermisoRow>> entry : sectionRows.entrySet()) {
			int secId = entry.getKey();
			Set<Integer> initialSect = initialSectionIds.getOrDefault(secId, Set.of());
			List<Integer> add = new ArrayList<>();
			List<Integer> remove = new ArrayList<>();
			for (PermisoRow row : entry.getValue()) {
				boolean wasChecked = initialSect.contains(row.permiso.getId());
				if (row.checked && !wasChecked)
					add.add(row.permiso.getId());
				else if (!row.checked && wasChecked)
					remove.add(row.permiso.getId());
			}
			if (!add.isEmpty())
				addSections.put(secId, add);
			if (!remove.isEmpty())
				removeSections.put(secId, remove);
		}

		return new PermisoChanges(addGlobal, removeGlobal, addSections, removeSections);
	}

	private boolean safetyCheck(PermisoChanges changes) {
		if (changes.globalRemove().isEmpty())
			return true;

		Permiso modifyUser = permisoByKey.get(PermissionGuard.MODIFY_USER);
		if (modifyUser == null)
			return true;

		if (myGroupId != existing.getId())
			return true;

		if (changes.globalRemove().contains(modifyUser.getId())) {
			SwingUtils.showError(this,
					"No puedes quitarte el permiso MODIFY_USER de tu propio grupo.\n"
							+ "Perderías acceso a la gestión de grupos.");
			return false;
		}
		return true;
	}

	// ── Getters ───────────────────────────────────────────────────────────────

	public boolean isConfirmed() {
		return confirmed;
	}

	public Grupo getGrupo() {
		Grupo g = new Grupo();
		g.setNombre(txtNombre.getText().trim());
		String dn = txtDn.getText().trim();
		g.setDn(dn.isEmpty() ? null : dn);
		g.setSuperAdmin(chkSuperAdmin.isSelected());
		return g;
	}

	/** Devuelve los cambios de permisos calculados al confirmar, o null si no aplica. */
	public PermisoChanges getPermisoChanges() {
		return permisoChanges;
	}
}
