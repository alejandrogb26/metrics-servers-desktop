package local.alejandrogb.metricsserversdesktop.ui.dialog.grupos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import local.alejandrogb.metricsserversdesktop.models.Grupo;
import local.alejandrogb.metricsserversdesktop.models.Permiso;
import local.alejandrogb.metricsserversdesktop.models.PermissionMap;
import local.alejandrogb.metricsserversdesktop.models.Seccion;
import local.alejandrogb.metricsserversdesktop.services.crud.PermisoService;
import local.alejandrogb.metricsserversdesktop.services.crud.SeccionService;
import local.alejandrogb.metricsserversdesktop.ui.util.SwingUtils;

/**
 * Dialogo para crear/editar grupos con tres pestanas: 1. Datos basicos (nombre,
 * dn, superadmin) 2. Permisos globales (solo lectura, muestra nombres) 3.
 * Permisos por seccion (solo lectura, muestra nombres de seccion y permiso)
 */
public class GrupoDialog extends JDialog {

	private static final long serialVersionUID = 957097414028531146L;

	private final Grupo existing;
	private boolean confirmed = false;

	private JTextField txtNombre;
	private JTextField txtDn;
	private JCheckBox chkSuperAdmin;

	/** Modelos de tabla de permisos — se rellenan al cargar en background. */
	private final DefaultTableModel globalModel;
	private final DefaultTableModel sectModel;

	public GrupoDialog(Window parent, Grupo existing) {
		super(parent,
				existing == null ? "Nuevo Grupo"
						: "Editar Grupo — " + (existing.getNombre() != null ? existing.getNombre() : ""),
				ModalityType.APPLICATION_MODAL);
		this.existing = existing;

		// Crear modelos vacíos antes de buildUI
		globalModel = new DefaultTableModel(new String[] { "Permiso", "Ambito" }, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
		sectModel = new DefaultTableModel(new String[] { "Seccion", "Permiso", "Ambito" }, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};

		buildUI();
		pack();
		setMinimumSize(new Dimension(560, 440));
		setResizable(true);
		setLocationRelativeTo(parent);

		// Cargar nombres en background para no bloquear el EDT
		loadPermisosYSecciones();
	}

	private void buildUI() {
		JPanel main = new JPanel(new BorderLayout(0, 8));
		main.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

		JTabbedPane tabs = new JTabbedPane();

		// ── Tab 1: Datos basicos ──────────────────────────────────────────
		JPanel tabBasic = new JPanel(new GridBagLayout());
		tabBasic.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		tabBasic.add(new JLabel("Nombre *"), SwingUtils.gbc(0, 0, GridBagConstraints.NONE, 0));
		txtNombre = new JTextField(24);
		tabBasic.add(txtNombre, SwingUtils.gbc(1, 0, GridBagConstraints.HORIZONTAL, 1.0));

		tabBasic.add(new JLabel("DN (LDAP)"), SwingUtils.gbc(0, 1, GridBagConstraints.NONE, 0));
		txtDn = new JTextField(24);
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

		tabs.addTab("Datos basicos", tabBasic);

		// ── Tab 2: Permisos globales ──────────────────────────────────────
		JPanel tabGlobal = new JPanel(new BorderLayout());
		tabGlobal.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		JTable tblGlobal = new JTable(globalModel);
		tblGlobal.setRowHeight(26);
		tblGlobal.getColumnModel().getColumn(0).setPreferredWidth(180);
		tblGlobal.getColumnModel().getColumn(1).setPreferredWidth(100);
		tabGlobal.add(new JScrollPane(tblGlobal), BorderLayout.CENTER);

		JLabel lblGlobalNote = new JLabel("  Los permisos son de solo lectura.");
		lblGlobalNote.setFont(lblGlobalNote.getFont().deriveFont(11f));
		lblGlobalNote.setForeground(Color.GRAY);
		tabGlobal.add(lblGlobalNote, BorderLayout.SOUTH);

		tabs.addTab("Permisos globales", tabGlobal);

		// ── Tab 3: Permisos por seccion ───────────────────────────────────
		JPanel tabSections = new JPanel(new BorderLayout());
		tabSections.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		JTable tblSect = new JTable(sectModel);
		tblSect.setRowHeight(26);
		tblSect.getColumnModel().getColumn(0).setPreferredWidth(160);
		tblSect.getColumnModel().getColumn(1).setPreferredWidth(160);
		tblSect.getColumnModel().getColumn(2).setPreferredWidth(100);
		tabSections.add(new JScrollPane(tblSect), BorderLayout.CENTER);

		JLabel lblSectNote = new JLabel("  Los permisos por seccion son de solo lectura.");
		lblSectNote.setFont(lblSectNote.getFont().deriveFont(11f));
		lblSectNote.setForeground(Color.GRAY);
		tabSections.add(lblSectNote, BorderLayout.SOUTH);

		tabs.addTab("Permisos por seccion", tabSections);

		// ── Populate campos de texto ──────────────────────────────────────
		if (existing != null) {
			txtNombre.setText(existing.getNombre() != null ? existing.getNombre() : "");
			txtDn.setText(existing.getDn() != null ? existing.getDn() : "");
			chkSuperAdmin.setSelected(Boolean.TRUE.equals(existing.isSuperAdmin()));
		}

		main.add(tabs, BorderLayout.CENTER);

		// ── Botones ───────────────────────────────────────────────────────
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

	/**
	 * Carga permisos y secciones en background para resolver IDs → nombres y
	 * rellena los modelos de tabla de las pestanas de permisos.
	 */
	private void loadPermisosYSecciones() {
		if (existing == null || existing.getPermisos() == null)
			return;

		SwingUtils.runAsync(() -> {
			List<Permiso> permisos = new PermisoService().findAll();
			List<Seccion> secciones = new SeccionService().findAll();

			// Construir mapas id → datos
			Map<Integer, Permiso> permisoMap = new HashMap<>();
			for (Permiso p : permisos)
				permisoMap.put(p.getId(), p);

			Map<Integer, String> seccionMap = new HashMap<>();
			for (Seccion s : secciones)
				seccionMap.put(s.getId(), s.getNombre());

			return new Object[] { permisoMap, seccionMap };
		}, result -> {
			@SuppressWarnings("unchecked")
			Map<Integer, Permiso> permisoMap = (Map<Integer, Permiso>) result[0];
			@SuppressWarnings("unchecked")
			Map<Integer, String> seccionMap = (Map<Integer, String>) result[1];

			PermissionMap<Integer> pm = existing.getPermisos();

			// Rellenar permisos globales
			globalModel.setRowCount(0);
			if (pm.getGlobal() != null) {
				for (Integer pid : pm.getGlobal()) {
					Permiso p = permisoMap.get(pid);
					if (p != null) {
						globalModel.addRow(new Object[] { p.getNombre(),
								p.getAmbito() != null ? p.getAmbito().getNombre() : "—" });
					} else {
						globalModel.addRow(new Object[] { "Permiso #" + pid, "—" });
					}
				}
			}

			// Rellenar permisos por seccion
			sectModel.setRowCount(0);
			if (pm.getSections() != null) {
				for (Map.Entry<Integer, List<Integer>> entry : pm.getSections().entrySet()) {
					String secNombre = seccionMap.getOrDefault(entry.getKey(), "Seccion #" + entry.getKey());
					for (Integer pid : entry.getValue()) {
						Permiso p = permisoMap.get(pid);
						if (p != null) {
							sectModel.addRow(new Object[] { secNombre, p.getNombre(),
									p.getAmbito() != null ? p.getAmbito().getNombre() : "—" });
						} else {
							sectModel.addRow(new Object[] { secNombre, "Permiso #" + pid, "—" });
						}
					}
				}
			}
		}, err -> {
			// En caso de error al cargar, dejamos los IDs como fallback
		});
	}

	private void doConfirm() {
		if (txtNombre.getText().trim().isEmpty()) {
			SwingUtils.showError(this, "El nombre del grupo es obligatorio.");
			txtNombre.requestFocus();
			return;
		}
		confirmed = true;
		dispose();
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public Grupo getGrupo() {
		Grupo g = new Grupo();
		g.setNombre(txtNombre.getText().trim());
		g.setDn(txtDn.getText().trim().isEmpty() ? null : txtDn.getText().trim());
		g.setSuperAdmin(chkSuperAdmin.isSelected());
		return g;
	}
}
