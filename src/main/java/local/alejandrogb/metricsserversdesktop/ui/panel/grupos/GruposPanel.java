package local.alejandrogb.metricsserversdesktop.ui.panel.grupos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import local.alejandrogb.metricsserversdesktop.models.BulkResult;
import local.alejandrogb.metricsserversdesktop.models.Grupo;
import local.alejandrogb.metricsserversdesktop.services.crud.GrupoService;
import local.alejandrogb.metricsserversdesktop.services.permission.PermissionGuard;
import local.alejandrogb.metricsserversdesktop.ui.component.BaseTablePanel;
import local.alejandrogb.metricsserversdesktop.ui.dialog.grupos.GrupoDialog;
import local.alejandrogb.metricsserversdesktop.ui.util.SwingUtils;

public class GruposPanel extends BaseTablePanel<Grupo> {

	private static final long serialVersionUID = 8261215021008229831L;
	private final GrupoService service = new GrupoService();
	private final PermissionGuard guard = PermissionGuard.getInstance();
	private final GrupoTableModel model = new GrupoTableModel();

	@Override
	protected String getPanelTitle() {
		return "Gestión de Grupos";
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

		tbl.getColumnModel().getColumn(0).setMaxWidth(70);
		tbl.getColumnModel().getColumn(1).setPreferredWidth(200);
		tbl.getColumnModel().getColumn(2).setPreferredWidth(350);
		tbl.getColumnModel().getColumn(3).setPreferredWidth(90);
		tbl.getColumnModel().getColumn(3).setMaxWidth(110);
	}

	@Override
	protected List<Grupo> loadData() {
		return service.findAll();
	}

	@Override
	protected void applyData(List<Grupo> data) {
		model.setData(data);
		refreshTable(model);
	}

	@Override
	protected boolean canEdit() {
		return guard.canManageUsuarios();
	}

	@Override
	protected boolean canDelete() {
		return guard.canManageUsuarios();
	}

	@Override
	protected void onNew() {
		GrupoDialog dlg = new GrupoDialog(SwingUtilities.getWindowAncestor(this), null);
		dlg.setVisible(true);
		if (!dlg.isConfirmed())
			return;
		showLoading("Creando grupo…");
		GrupoDialog.PermisoChanges changes = dlg.getPermisoChanges();
		List<Integer> globalIds = (changes != null) ? changes.globalAdd() : List.of();
		Map<Integer, List<Integer>> sectionIds = (changes != null) ? changes.sectionAdd() : Map.of();
		SwingUtils.runAsync(() -> service.createWithPermisos(dlg.getGrupo(), globalIds, sectionIds), res -> {
			hideLoading();
			showBulk(res);
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
		Grupo g = model.getRow(table.convertRowIndexToModel(row));
		// Cargamos el grupo completo (con permisos) antes de abrir el diálogo
		showLoading("Cargando grupo…");
		SwingUtils.runAsync(() -> service.findById(g.getId()), full -> {
			hideLoading();
			GrupoDialog dlg = new GrupoDialog(SwingUtilities.getWindowAncestor(this), full);
			dlg.setVisible(true);
			if (!dlg.isConfirmed())
				return;
			showLoading("Actualizando grupo…");
			SwingUtils.runAsync(() -> {
				service.update(g.getId(), full, dlg.getGrupo());
				GrupoDialog.PermisoChanges changes = dlg.getPermisoChanges();
				if (changes != null && !changes.isEmpty()) {
					if (!changes.globalAdd().isEmpty() || !changes.globalRemove().isEmpty()) {
						service.updateGlobalPermisos(g.getId(), changes.globalAdd(), changes.globalRemove());
					}
					// Secciones con añadidos (incluyendo sus eliminaciones del mismo seccionId)
					for (Map.Entry<Integer, List<Integer>> e : changes.sectionAdd().entrySet()) {
						service.updateSeccionPermisos(g.getId(), e.getKey(), e.getValue(),
								changes.sectionRemove().getOrDefault(e.getKey(), List.of()));
					}
					// Secciones con solo eliminaciones
					for (Map.Entry<Integer, List<Integer>> e : changes.sectionRemove().entrySet()) {
						if (!changes.sectionAdd().containsKey(e.getKey())) {
							service.updateSeccionPermisos(g.getId(), e.getKey(), List.of(), e.getValue());
						}
					}
				}
				return null;
			}, v -> {
				hideLoading();
				refresh();
			}, err -> {
				hideLoading();
				SwingUtils.showError(this, err.getMessage());
			});
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
		Grupo g = model.getRow(table.convertRowIndexToModel(row));
		if (!SwingUtils.confirm(this, "¿Eliminar el grupo '" + g.getNombre() + "'?\n"
				+ "Los usuarios vinculados a este grupo perderán acceso."))
			return;
		showLoading("Eliminando…");
		SwingUtils.runAsync(() -> service.delete(g.getId()), res -> {
			hideLoading();
			showBulk(res);
			refresh();
		}, err -> {
			hideLoading();
			SwingUtils.showError(this, err.getMessage());
		});
	}

	private void showBulk(BulkResult r) {
		if (r == null)
			return;
		if (r.getFailed() == 0)
			SwingUtils.showInfo(this, r.getSummary());
		else
			SwingUtils.showError(this, r.getSummary());
	}

	// ── Table model ───────────────────────────────────────────────────────

	static class GrupoTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -8387734676430858565L;
		private static final String[] COLS = { "ID", "Nombre", "DN", "Superadmin" };
		private List<Grupo> data = new ArrayList<>();

		void setData(List<Grupo> d) {
			data = d != null ? d : new ArrayList<>();
			fireTableDataChanged();
		}

		Grupo getRow(int r) {
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
			Grupo g = data.get(r);
			return switch (c) {
			case 0 -> g.getId();
			case 1 -> g.getNombre();
			case 2 -> g.getDn();
			case 3 -> Boolean.TRUE.equals(g.isSuperAdmin()) ? "✓ Sí" : "No";
			default -> null;
			};
		}

		@Override
		public Class<?> getColumnClass(int c) {
			return c == 0 ? Integer.class : String.class;
		}
	}
}
