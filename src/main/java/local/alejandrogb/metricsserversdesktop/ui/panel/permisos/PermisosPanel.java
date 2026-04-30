package local.alejandrogb.metricsserversdesktop.ui.panel.permisos;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import local.alejandrogb.metricsserversdesktop.models.Permiso;
import local.alejandrogb.metricsserversdesktop.services.crud.PermisoService;
import local.alejandrogb.metricsserversdesktop.ui.component.BaseTablePanel;

public class PermisosPanel extends BaseTablePanel<Permiso> {

	private static final long serialVersionUID = -7496018819736011313L;
	private final PermisoService service = new PermisoService();
	private final PermisoTableModel model = new PermisoTableModel();

	public PermisosPanel() {
		super();
		// Solo lectura: ocultar botones de acción
		btnNew.setVisible(false);
		btnEdit.setVisible(false);
		btnDelete.setVisible(false);
	}

	@Override
	protected String getPanelTitle() {
		return "Permisos del sistema (solo lectura)";
	}

	@Override
	protected AbstractTableModel createTableModel() {
		return model;
	}

	@Override
	protected void configureTable(JTable tbl) {
		if (tbl.getColumnModel().getColumnCount() < 4) {
			return;
		}
		tbl.getColumnModel().getColumn(0).setMaxWidth(60);
		tbl.getColumnModel().getColumn(1).setPreferredWidth(180);
		tbl.getColumnModel().getColumn(2).setPreferredWidth(320);
		tbl.getColumnModel().getColumn(3).setPreferredWidth(120);
	}

	@Override
	protected List<Permiso> loadData() {
		return service.findAll();
	}

	@Override
	protected void applyData(List<Permiso> data) {
		model.setData(data);

		table.setModel(model);
		table.createDefaultColumnsFromModel();
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

	@Override
	protected boolean canEdit() {
		return false;
	}

	@Override
	protected boolean canDelete() {
		return false;
	}

	@Override
	protected void onNew() {
	}

	@Override
	protected void onEdit() {
	}

	@Override
	protected void onDelete() {
	}

	// ── Table model ───────────────────────────────────────────────────────

	static class PermisoTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 5210125867477090958L;
		private static final String[] COLS = { "ID", "Nombre", "Descripción", "Ámbito" };
		private List<Permiso> data = new ArrayList<>();

		void setData(List<Permiso> d) {
			data = d != null ? d : new ArrayList<>();
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return data.size();
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
		public Object getValueAt(int r, int c) {
			Permiso p = data.get(r);
			return switch (c) {
			case 0 -> p.getId();
			case 1 -> p.getNombre();
			case 2 -> p.getDescripcion();
			case 3 -> p.getAmbito() != null ? p.getAmbito().getNombre() : "—";
			default -> null;
			};
		}

		@Override
		public Class<?> getColumnClass(int c) {
			return c == 0 ? Integer.class : String.class;
		}
	}
}
