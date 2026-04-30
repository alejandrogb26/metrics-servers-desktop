package local.alejandrogb.metricsserversdesktop.ui.component;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import local.alejandrogb.metricsserversdesktop.ui.util.SwingUtils;

/**
 * Panel base reutilizable para todos los paneles de gestión CRUD.
 * <p>
 * Proporciona:
 * <ul>
 * <li>Barra de herramientas con botones Nuevo, Editar, Eliminar
 * (ocultables)</li>
 * <li>JTable configurada con selección de fila, doble clic para editar</li>
 * <li>Carga asíncrona con LoadingPanel</li>
 * <li>Título de sección</li>
 * </ul>
 * Las subclases solo deben implementar los métodos abstractos.
 */
public abstract class BaseTablePanel<T> extends JPanel {

	private static final long serialVersionUID = 7926822894074333216L;
	protected JTable table;
	protected AbstractTableModel tableModel;

	protected JButton btnNew;
	protected JButton btnEdit;
	protected JButton btnDelete;

	private final JPanel contentPanel;
	private final LoadingPanel loadingPanel;
	private final JLayeredPane layered;

	protected BaseTablePanel() {
		setLayout(new BorderLayout());

		JPanel headerBar = new JPanel(new BorderLayout());
		headerBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));
		JLabel title = SwingUtils.sectionTitle(getPanelTitle());
		headerBar.add(title, BorderLayout.WEST);
		add(headerBar, BorderLayout.NORTH);

		JToolBar toolBar = SwingUtils.createToolBar();
		toolBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

		btnNew = SwingUtils.actionButton("＋ Nuevo", "Crear registro");
		btnEdit = SwingUtils.actionButton("✎ Editar", "Editar seleccionado");
		btnDelete = SwingUtils.actionButton("✕ Eliminar", "Eliminar seleccionado");

		btnEdit.setEnabled(false);
		btnDelete.setEnabled(false);

		toolBar.add(btnNew);
		toolBar.addSeparator();
		toolBar.add(btnEdit);
		toolBar.add(btnDelete);

		tableModel = createTableModel();
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(28);
		table.setFillsViewportHeight(true);
		table.getTableHeader().setReorderingAllowed(false);
		table.setAutoCreateRowSorter(true);
		configureTable(table);

		table.getSelectionModel().addListSelectionListener(e -> {
			boolean sel = table.getSelectedRow() >= 0;
			btnEdit.setEnabled(sel && canEdit());
			btnDelete.setEnabled(sel && canDelete());
		});

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && table.getSelectedRow() >= 0 && canEdit()) {
					onEdit();
				}
			}
		});

		btnNew.addActionListener(e -> onNew());
		btnEdit.addActionListener(e -> onEdit());
		btnDelete.addActionListener(e -> onDelete());

		JScrollPane scroll = new JScrollPane(table);
		scroll.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

		contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(toolBar, BorderLayout.NORTH);
		contentPanel.add(scroll, BorderLayout.CENTER);

		loadingPanel = new LoadingPanel();
		layered = null; // o elimina el atributo si quieres

		add(contentPanel, BorderLayout.CENTER);
	}

	/**
	 * Aplica un nuevo modelo a la tabla, recrea las columnas, restaura el RowSorter
	 * y aplica la configuracion de anchos.
	 * <p>
	 * Llamar a este metodo desde {@code applyData()} en lugar de repetir la misma
	 * secuencia en cada panel garantiza que el ordenamiento por columna siga
	 * funcionando despues de cada recarga de datos.
	 * </p>
	 */
	protected void refreshTable(AbstractTableModel newModel) {
		table.setModel(newModel);
		table.createDefaultColumnsFromModel();
		table.setAutoCreateRowSorter(true);
		configureTable(table);
		table.revalidate();
		table.repaint();
		if (table.getParent() != null) {
			table.getParent().revalidate();
			table.getParent().repaint();
		}
		revalidate();
		repaint();
	}

	protected void initToolbar() {
		Container north = (Container) contentPanel.getComponent(0);
		if (north instanceof JToolBar toolBar) {
			addExtraToolbarButtons(toolBar);
		}
	}

	// ── Loading helpers ───────────────────────────────────────────────────

	protected void showLoading(String msg) {
//		loadingPanel.setMessage(msg);
//		loadingPanel.setVisible(true);
	}

	protected void hideLoading() {
//		loadingPanel.setVisible(false);
	}

	/** Recarga los datos desde la API de forma asíncrona. */
	public void refresh() {
		showLoading("Cargando datos…");
		SwingUtils.runAsync(this::loadData, data -> {
			applyData(data);
			hideLoading();
			revalidate();
			repaint();
		}, err -> {
			err.printStackTrace();
			hideLoading();
			SwingUtils.showError(this, "Error al cargar datos: " + err.getMessage());
		});
	}

	// ── Abstract API ──────────────────────────────────────────────────────

	/** Título visible en la cabecera del panel. */
	protected abstract String getPanelTitle();

	/** Crea el modelo de tabla. */
	protected abstract AbstractTableModel createTableModel();

	/** Ajustes adicionales a la tabla (anchos de columna, renders, etc.). */
	protected void configureTable(JTable tbl) {
	}

	/** Carga los datos desde la API (se ejecuta en hilo de fondo). */
	protected abstract java.util.List<T> loadData() throws Exception;

	/** Aplica los datos al modelo (se ejecuta en EDT). */
	protected abstract void applyData(java.util.List<T> data);

	/** Acción "Nuevo". */
	protected abstract void onNew();

	/** Acción "Editar". */
	protected abstract void onEdit();

	/** Acción "Eliminar". */
	protected abstract void onDelete();

	/**
	 * ¿Puede el usuario actual editar? Por defecto true. Subclases pueden
	 * sobreescribir.
	 */
	protected boolean canEdit() {
		return true;
	}

	/** ¿Puede el usuario actual eliminar? Por defecto true. */
	protected boolean canDelete() {
		return true;
	}

	/**
	 * Añade botones adicionales al toolbar (p.ej. "Importar JSON"). Por defecto no
	 * hace nada.
	 */
	protected void addExtraToolbarButtons(JToolBar toolBar) {
	}
}
