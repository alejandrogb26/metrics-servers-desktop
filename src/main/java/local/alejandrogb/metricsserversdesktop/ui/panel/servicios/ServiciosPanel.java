package local.alejandrogb.metricsserversdesktop.ui.panel.servicios;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import local.alejandrogb.metricsserversdesktop.models.Servicio;
import local.alejandrogb.metricsserversdesktop.services.crud.ServicioService;
import local.alejandrogb.metricsserversdesktop.services.permission.PermissionGuard;
import local.alejandrogb.metricsserversdesktop.ui.component.BaseTablePanel;
import local.alejandrogb.metricsserversdesktop.ui.component.ImageCellRenderer;
import local.alejandrogb.metricsserversdesktop.ui.dialog.servicios.ServicioDialog;
import local.alejandrogb.metricsserversdesktop.ui.util.SwingUtils;

public class ServiciosPanel extends BaseTablePanel<Servicio> {

	private static final long serialVersionUID = -8450623536790280266L;

	private final ServicioService service = new ServicioService();
	private final PermissionGuard guard = PermissionGuard.getInstance();
	private final ServicioTableModel model = new ServicioTableModel();

	@Override
	protected String getPanelTitle() {
		return "Gestion de Servicios";
	}

	@Override
	protected AbstractTableModel createTableModel() {
		return model;
	}

	@Override
	protected void configureTable(JTable tbl) {
		if (tbl.getColumnModel().getColumnCount() < 4)
			return;

		// Columna de logo (miniatura)
		tbl.getColumnModel().getColumn(ServicioTableModel.COL_LOGO_IMG).setCellRenderer(new ImageCellRenderer());
		tbl.getColumnModel().getColumn(ServicioTableModel.COL_LOGO_IMG).setPreferredWidth(ImageCellRenderer.COL_WIDTH);
		tbl.getColumnModel().getColumn(ServicioTableModel.COL_LOGO_IMG).setMaxWidth(ImageCellRenderer.COL_WIDTH);
		tbl.setRowHeight(ImageCellRenderer.ROW_HEIGHT);

		tbl.getColumnModel().getColumn(1).setMaxWidth(60);
		tbl.getColumnModel().getColumn(2).setPreferredWidth(220);
		tbl.getColumnModel().getColumn(3).setPreferredWidth(180);
	}

	@Override
	protected List<Servicio> loadData() {
		return service.findAll();
	}

	@Override
	protected void applyData(List<Servicio> data) {
		model.setData(data);
		refreshTable(model);
	}

	@Override
	protected boolean canEdit() {
		return guard.canManageServicios();
	}

	@Override
	protected boolean canDelete() {
		return guard.canManageServicios();
	}

	@Override
	protected void onNew() {
		ServicioDialog dlg = new ServicioDialog(SwingUtilities.getWindowAncestor(this), null);
		dlg.setVisible(true);
		if (!dlg.isConfirmed())
			return;
		showLoading("Creando servicio...");
		Path logoPath = dlg.getLogoPath();
		SwingUtils.runAsync(() -> {
			int id = service.create(dlg.getServicio());
			if (id > 0 && logoPath != null) {
				service.subirLogo(id, logoPath);
			}
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
	protected void onEdit() {
		int row = table.getSelectedRow();
		if (row < 0)
			return;
		Servicio s = model.getRow(table.convertRowIndexToModel(row));
		ServicioDialog dlg = new ServicioDialog(SwingUtilities.getWindowAncestor(this), s);
		dlg.setVisible(true);
		if (!dlg.isConfirmed())
			return;
		showLoading("Actualizando servicio...");
		Servicio updated = dlg.getServicio();
		updated.setId(s.getId());
		Path logoPath = dlg.getLogoPath();
		SwingUtils.runAsync(() -> {
			service.update(updated);
			if (logoPath != null) {
				service.subirLogo(s.getId(), logoPath);
			}
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
		Servicio s = model.getRow(table.convertRowIndexToModel(row));
		if (!SwingUtils.confirm(this, "Eliminar el servicio '" + s.getNombre() + "'?"))
			return;
		showLoading("Eliminando...");
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

	// ── Table model ───────────────────────────────────────────────────────

	static class ServicioTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 5013425244761738968L;

		/** Columna de la miniatura del logo. */
		public static final int COL_LOGO_IMG = 0;

		// "", ID, Nombre, Logo (nombre fichero)
		private static final String[] COLS = { "", "ID", "Nombre", "Logo" };
		private List<Servicio> data = new ArrayList<>();

		void setData(List<Servicio> d) {
			data = d != null ? d : new ArrayList<>();
			fireTableDataChanged();
		}

		Servicio getRow(int r) {
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
			Servicio s = data.get(r);
			return switch (c) {
			case COL_LOGO_IMG -> s.getUrlLogo() != null ? s.getUrlLogo() : "";
			case 1 -> s.getId();
			case 2 -> s.getNombre();
			case 3 -> s.getLogo() != null ? s.getLogo() : "—";
			default -> null;
			};
		}

		@Override
		public Class<?> getColumnClass(int c) {
			return c == 1 ? Integer.class : String.class;
		}
	}
}
