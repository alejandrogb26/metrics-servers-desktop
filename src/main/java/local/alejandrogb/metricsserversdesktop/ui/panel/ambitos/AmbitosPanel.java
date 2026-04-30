package local.alejandrogb.metricsserversdesktop.ui.panel.ambitos;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import local.alejandrogb.metricsserversdesktop.models.Ambito;
import local.alejandrogb.metricsserversdesktop.services.crud.AmbitoService;
import local.alejandrogb.metricsserversdesktop.ui.component.BaseTablePanel;

public class AmbitosPanel extends BaseTablePanel<Ambito> {

	private static final long serialVersionUID = -2119017158482812876L;
	private final AmbitoService service = new AmbitoService();
	private final AmbitoTableModel model = new AmbitoTableModel();

	public AmbitosPanel() {
		super();
		btnNew.setVisible(false);
		btnEdit.setVisible(false);
		btnDelete.setVisible(false);
	}

	@Override
	protected String getPanelTitle() {
		return "Ámbitos del sistema (solo lectura)";
	}

	@Override
	protected AbstractTableModel createTableModel() {
		return model;
	}

	@Override
	protected void configureTable(JTable tbl) {
		if (tbl.getColumnModel().getColumnCount() < 3) {
			return;
		}
		tbl.getColumnModel().getColumn(0).setMaxWidth(60);
		tbl.getColumnModel().getColumn(1).setPreferredWidth(180);
		tbl.getColumnModel().getColumn(2).setPreferredWidth(400);
	}

	@Override
	protected List<Ambito> loadData() {
		return service.findAll();
	}

	@Override
	protected void applyData(List<Ambito> data) {
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

	static class AmbitoTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -5956273484944822418L;
		private static final String[] COLS = { "ID", "Nombre", "Descripción" };
		private List<Ambito> data = new ArrayList<>();

		void setData(List<Ambito> d) {
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
			Ambito a = data.get(r);
			return switch (c) {
			case 0 -> a.getId();
			case 1 -> a.getNombre();
			default -> a.getDescripcion();
			};
		}

		@Override
		public Class<?> getColumnClass(int c) {
			return c == 0 ? Integer.class : String.class;
		}
	}
}
