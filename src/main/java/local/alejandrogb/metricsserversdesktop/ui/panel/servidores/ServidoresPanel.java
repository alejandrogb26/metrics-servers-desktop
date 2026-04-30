package local.alejandrogb.metricsserversdesktop.ui.panel.servidores;

import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import local.alejandrogb.metricsserversdesktop.models.BulkResult;
import local.alejandrogb.metricsserversdesktop.models.Seccion;
import local.alejandrogb.metricsserversdesktop.models.Servicio;
import local.alejandrogb.metricsserversdesktop.models.servidor.Servidor;
import local.alejandrogb.metricsserversdesktop.models.servidor.ServidorDTO;
import local.alejandrogb.metricsserversdesktop.services.crud.SeccionService;
import local.alejandrogb.metricsserversdesktop.services.crud.ServidorService;
import local.alejandrogb.metricsserversdesktop.services.crud.ServicioService;
import local.alejandrogb.metricsserversdesktop.services.permission.PermissionGuard;
import local.alejandrogb.metricsserversdesktop.ui.component.BaseTablePanel;
import local.alejandrogb.metricsserversdesktop.ui.component.ImageCellRenderer;
import local.alejandrogb.metricsserversdesktop.ui.dialog.servidores.ServidorDialog;
import local.alejandrogb.metricsserversdesktop.ui.util.SwingUtils;

public class ServidoresPanel extends BaseTablePanel<Servidor> {

	private static final long serialVersionUID = -4900329855912330424L;

	private final ServidorService service = new ServidorService();
	private final SeccionService secService = new SeccionService();
	private final ServicioService srvService = new ServicioService();
	private final ServidorTableModel model = new ServidorTableModel();
	private final PermissionGuard guard = PermissionGuard.getInstance();

	private List<Seccion> cachedSecciones = List.of();
	private List<Servicio> cachedServicios = List.of();

	public ServidoresPanel() {
		super();
		initToolbar();
	}

	@Override
	protected String getPanelTitle() {
		return "Gestion de Servidores";
	}

	@Override
	protected AbstractTableModel createTableModel() {
		return model;
	}

	@Override
	protected void configureTable(JTable tbl) {
		if (tbl.getColumnModel().getColumnCount() < 10)
			return;

		// Columna de imagen
		tbl.getColumnModel().getColumn(ServidorTableModel.COL_IMAGEN).setCellRenderer(new ImageCellRenderer());
		tbl.getColumnModel().getColumn(ServidorTableModel.COL_IMAGEN).setPreferredWidth(ImageCellRenderer.COL_WIDTH);
		tbl.getColumnModel().getColumn(ServidorTableModel.COL_IMAGEN).setMaxWidth(ImageCellRenderer.COL_WIDTH);
		tbl.setRowHeight(ImageCellRenderer.ROW_HEIGHT);

		tbl.getColumnModel().getColumn(1).setPreferredWidth(45);
		tbl.getColumnModel().getColumn(1).setMaxWidth(60);
		tbl.getColumnModel().getColumn(2).setPreferredWidth(120);
		tbl.getColumnModel().getColumn(3).setPreferredWidth(180);
		tbl.getColumnModel().getColumn(4).setPreferredWidth(110);
		tbl.getColumnModel().getColumn(5).setPreferredWidth(160);
		tbl.getColumnModel().getColumn(6).setPreferredWidth(65);
		tbl.getColumnModel().getColumn(7).setPreferredWidth(110);
		tbl.getColumnModel().getColumn(8).setPreferredWidth(120);
		tbl.getColumnModel().getColumn(9).setPreferredWidth(190);
	}

	@Override
	protected void addExtraToolbarButtons(JToolBar toolBar) {
		if (guard.canManageServidores(0) || guard.isSuperAdmin()) {
			toolBar.addSeparator();

			JButton btnImport = SwingUtils.actionButton("Importar JSON",
					"Carga masiva de servidores desde un fichero JSON");
			btnImport.addActionListener(e -> doImportJson());
			toolBar.add(btnImport);

			toolBar.addSeparator();

			JButton btnFoto = SwingUtils.actionButton("Subir imagen",
					"Sube o reemplaza la imagen del servidor seleccionado");
			btnFoto.addActionListener(e -> doSubirFoto());
			toolBar.add(btnFoto);
		}
	}

	// ── Carga de datos ────────────────────────────────────────────────────

	private record ServidoresData(List<Servidor> servidores, List<Seccion> secciones, List<Servicio> servicios) {
	}

	@Override
	protected List<Servidor> loadData() throws Exception {
		return service.findAll();
	}

	@Override
	public void refresh() {
		showLoading("Cargando datos...");
		SwingUtils.runAsync(() -> new ServidoresData(service.findAll(), secService.findAll(), srvService.findAll()),
				data -> {
					cachedSecciones = data.secciones();
					cachedServicios = data.servicios();
					applyServidoresData(data);
					hideLoading();
				}, err -> {
					err.printStackTrace();
					hideLoading();
					SwingUtils.showError(this, "Error al cargar datos: " + err.getMessage());
				});
	}

	private void applyServidoresData(ServidoresData data) {
		model.setData(data.servidores(), data.secciones(), data.servicios());
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
	protected void applyData(List<Servidor> data) {
		model.setData(data, cachedSecciones, cachedServicios);
		table.setModel(model);
		table.createDefaultColumnsFromModel();
		configureTable(table);
		table.revalidate();
		table.repaint();
		revalidate();
		repaint();
	}

	// ── Permisos ──────────────────────────────────────────────────────────

	@Override
	protected boolean canEdit() {
		return guard.canManageServidores(0) || guard.isSuperAdmin();
	}

	@Override
	protected boolean canDelete() {
		return guard.canManageServidores(0) || guard.isSuperAdmin();
	}

	// ── CRUD ──────────────────────────────────────────────────────────────

	@Override
	protected void onNew() {
		ServidorDialog dlg = new ServidorDialog(SwingUtilities.getWindowAncestor(this), null);
		dlg.setVisible(true);
		if (dlg.isConfirmed()) {
			showLoading("Creando servidor...");
			SwingUtils.runAsync(() -> service.create(dlg.getServidorDTO()), result -> {
				hideLoading();
				showBulkResult(result);
				refresh();
			}, err -> {
				hideLoading();
				SwingUtils.showError(this, err.getMessage());
			});
		}
	}

	@Override
	protected void onEdit() {
		int row = table.getSelectedRow();
		if (row < 0)
			return;
		Servidor s = model.getRow(table.convertRowIndexToModel(row));
		ServidorDialog dlg = new ServidorDialog(SwingUtilities.getWindowAncestor(this), s);
		dlg.setVisible(true);
		if (dlg.isConfirmed()) {
			showLoading("Actualizando servidor...");
			Servidor updated = dlg.getServidor();
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
	}

	@Override
	protected void onDelete() {
		int row = table.getSelectedRow();
		if (row < 0)
			return;
		Servidor s = model.getRow(table.convertRowIndexToModel(row));
		if (!SwingUtils.confirm(this,
				"Eliminar el servidor '" + s.getServerId() + "'?\nEsta accion no se puede deshacer."))
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

	// ── Subida de imagen del servidor ─────────────────────────────────────

	private void doSubirFoto() {
		int row = table.getSelectedRow();
		if (row < 0) {
			SwingUtils.showError(this, "Selecciona un servidor antes de subir una imagen.");
			return;
		}
		Servidor s = model.getRow(table.convertRowIndexToModel(row));

		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Seleccionar imagen para '" + s.getServerId() + "'");
		fc.setFileFilter(
				new FileNameExtensionFilter("Imagenes (PNG, JPG, GIF, WEBP)", "png", "jpg", "jpeg", "gif", "webp"));
		if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;

		File file = fc.getSelectedFile();
		showLoading("Subiendo imagen...");

		SwingUtils.runAsync(() -> service.subirFoto(s.getId(), file.toPath()), result -> {
			hideLoading();
			SwingUtils.showInfo(this, "Imagen subida correctamente.");
			refresh();
		}, err -> {
			hideLoading();
			SwingUtils.showError(this, "Error al subir la imagen: " + err.getMessage());
		});
	}

	// ── Importacion masiva JSON ───────────────────────────────────────────

	private void doImportJson() {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Seleccionar fichero JSON de servidores");
		fc.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
		if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;

		File file = fc.getSelectedFile();
		showLoading("Importando " + file.getName() + "...");

		SwingUtils.runAsync(() -> {
			ObjectMapper mapper = new ObjectMapper();
			List<ServidorDTO> dtos = mapper.readValue(file, new TypeReference<List<ServidorDTO>>() {
			});
			return service.createBulk(dtos);
		}, result -> {
			hideLoading();
			showBulkResult(result);
			refresh();
		}, err -> {
			hideLoading();
			SwingUtils.showError(this, "Error importando JSON: " + err.getMessage());
		});
	}

	private void showBulkResult(BulkResult r) {
		if (r == null)
			return;
		String msg = r.getSummary();
		if (r.getErrors() != null && !r.getErrors().isEmpty())
			msg += "\n\nErrores:\n- " + String.join("\n- ", r.getErrors());
		if (r.getFailed() == 0)
			SwingUtils.showInfo(this, msg);
		else
			SwingUtils.showError(this, msg);
	}
}
