package local.alejandrogb.metricsserversdesktop.ui.panel.secciones;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import local.alejandrogb.metricsserversdesktop.models.Seccion;
import local.alejandrogb.metricsserversdesktop.services.crud.SeccionService;
import local.alejandrogb.metricsserversdesktop.services.permission.PermissionGuard;
import local.alejandrogb.metricsserversdesktop.ui.component.BaseTablePanel;
import local.alejandrogb.metricsserversdesktop.ui.dialog.secciones.SeccionDialog;
import local.alejandrogb.metricsserversdesktop.ui.util.SwingUtils;

public class SeccionesPanel extends BaseTablePanel<Seccion> {

	private static final long serialVersionUID = 7763040578826802360L;
	private final SeccionService service = new SeccionService();
	private final PermissionGuard guard = PermissionGuard.getInstance();
	private final SeccionTableModel model = new SeccionTableModel();

	@Override
	protected String getPanelTitle() {
		return "Gestión de Secciones";
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

		tbl.getColumnModel().getColumn(0).setPreferredWidth(50);
		tbl.getColumnModel().getColumn(0).setMaxWidth(70);
		tbl.getColumnModel().getColumn(1).setPreferredWidth(200);
		tbl.getColumnModel().getColumn(2).setPreferredWidth(400);
	}

	@Override
	protected List<Seccion> loadData() {
		return service.findAll();
	}

	@Override
	protected void applyData(List<Seccion> data) {
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
		return guard.canManageSecciones();
	}

	@Override
	protected boolean canDelete() {
		return guard.canManageSecciones();
	}

	@Override
	protected void onNew() {
		SeccionDialog dlg = new SeccionDialog(SwingUtilities.getWindowAncestor(this), null);
		dlg.setVisible(true);
		if (!dlg.isConfirmed())
			return;
		showLoading("Creando sección…");
		SwingUtils.runAsync(() -> service.create(dlg.getSeccion()), id -> {
			hideLoading();
			refresh();
		}, err -> {
			hideLoading();
			SwingUtils.showError(this, err.getMessage());
		});
	}

	@Override
	protected void onEdit() {
		int row = table.getSelectedRow();
		if (row < 0)
			return;
		Seccion s = model.getRow(table.convertRowIndexToModel(row));
		SeccionDialog dlg = new SeccionDialog(SwingUtilities.getWindowAncestor(this), s);
		dlg.setVisible(true);
		if (!dlg.isConfirmed())
			return;
		showLoading("Actualizando sección…");
		Seccion updated = dlg.getSeccion();
		updated.setId(s.getId());
		SwingUtils.runAsync(() -> {
			service.update(updated);
			return null;
		}, v -> {
			hideLoading();
			refresh();
		}, err -> {
			hideLoading();
			SwingUtils.showError(this, err.getMessage());
		});
	}

	@Override
	protected void onDelete() {
		int row = table.getSelectedRow();
		if (row < 0)
			return;
		Seccion s = model.getRow(table.convertRowIndexToModel(row));
		if (!SwingUtils.confirm(this, "¿Eliminar la sección '" + s.getNombre() + "'?"))
			return;
		showLoading("Eliminando…");
		SwingUtils.runAsync(() -> {
			service.delete(s.getId());
			return null;
		}, v -> {
			hideLoading();
			refresh();
		}, err -> {
			hideLoading();
			SwingUtils.showError(this, err.getMessage());
		});
	}

	// ── Table model interno ───────────────────────────────────────────────

	static class SeccionTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -8070477320987675712L;
		private static final String[] COLS = { "ID", "Nombre", "Descripción" };
		private List<Seccion> data = new ArrayList<>();

		void setData(List<Seccion> d) {
			data = d != null ? d : new ArrayList<>();
			fireTableDataChanged();
		}

		Seccion getRow(int r) {
			return data.get(r);
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
			Seccion s = data.get(r);
			return switch (c) {
			case 0 -> s.getId();
			case 1 -> s.getNombre();
			default -> s.getDescripcion();
			};
		}

		@Override
		public Class<?> getColumnClass(int c) {
			return c == 0 ? Integer.class : String.class;
		}
	}
}
