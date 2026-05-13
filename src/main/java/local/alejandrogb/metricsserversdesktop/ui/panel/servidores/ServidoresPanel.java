package local.alejandrogb.metricsserversdesktop.ui.panel.servidores;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import local.alejandrogb.metricsserversdesktop.models.BulkResult;
import local.alejandrogb.metricsserversdesktop.models.PageResponse;
import local.alejandrogb.metricsserversdesktop.models.Seccion;
import local.alejandrogb.metricsserversdesktop.models.Servicio;
import local.alejandrogb.metricsserversdesktop.models.servidor.Servidor;
import local.alejandrogb.metricsserversdesktop.models.servidor.ServidorDTO;
import local.alejandrogb.metricsserversdesktop.services.crud.SeccionService;
import local.alejandrogb.metricsserversdesktop.services.crud.ServidorService;
import local.alejandrogb.metricsserversdesktop.services.crud.ServicioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import local.alejandrogb.metricsserversdesktop.services.permission.PermissionGuard;
import local.alejandrogb.metricsserversdesktop.ui.component.BaseTablePanel;
import local.alejandrogb.metricsserversdesktop.ui.component.ImageCellRenderer;
import local.alejandrogb.metricsserversdesktop.ui.dialog.servidores.ServidorDialog;
import local.alejandrogb.metricsserversdesktop.ui.util.SwingUtils;

public class ServidoresPanel extends BaseTablePanel<Servidor> {

	private static final long serialVersionUID = -4900329855912330424L;
	private static final Logger log = LoggerFactory.getLogger(ServidoresPanel.class);
	private static final int PAGE_SIZE = 20;

	private final ServidorService service = new ServidorService();
	private final SeccionService secService = new SeccionService();
	private final ServicioService srvService = new ServicioService();
	private final ServidorTableModel model = new ServidorTableModel();
	private final PermissionGuard guard = PermissionGuard.getInstance();

	private List<Seccion> cachedSecciones = List.of();
	private List<Servicio> cachedServicios = List.of();

	// ── Pagination state ──────────────────────────────────────────────────
	private int currentPage = 0;
	private int totalPages = 1;
	private int totalElements = 0;

	// ── Pagination controls ───────────────────────────────────────────────
	private JButton btnPrev;
	private JButton btnNext;
	private JLabel lblPagination;

	public ServidoresPanel() {
		super();
		initToolbar();
		setupPagination();
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

	private record ServidoresData(PageResponse<Servidor> response, List<Seccion> secciones,
			List<Servicio> servicios) {
	}

	@Override
	protected List<Servidor> loadData() throws Exception {
		return service.findAll();
	}

	/** Recarga desde la página 0 (p.ej. tras crear o importar). */
	@Override
	public void refresh() {
		loadPage(0);
	}

	/** Carga la página indicada actualizando estado y controles de paginación. */
	private void loadPage(int page) {
		showLoading("Cargando datos...");
		SwingUtils.runAsync(() -> {
			PageResponse<Servidor> response = service.findPage(page, PAGE_SIZE);
			List<Seccion> secciones;
			try {
				secciones = secService.findAll();
			} catch (Exception e) {
				log.warn("No se pudieron cargar las secciones, se mostrará columna vacía: {}", e.getMessage());
				secciones = List.of();
			}
			List<Servicio> servicios;
			try {
				servicios = srvService.findAll();
			} catch (Exception e) {
				log.warn("No se pudieron cargar los servicios, se mostrará columna vacía: {}", e.getMessage());
				servicios = List.of();
			}
			return new ServidoresData(response, secciones, servicios);
		}, data -> {
			currentPage = data.response().getPage();
			totalPages = Math.max(1, data.response().getTotalPages());
			totalElements = data.response().getTotal();
			cachedSecciones = data.secciones();
			cachedServicios = data.servicios();
			applyServidoresData(data);
			hideLoading();
		}, err -> {
			log.error("Error cargando servidores (página {})", page, err);
			hideLoading();
			SwingUtils.showError(this, "Error al cargar datos: " + err.getMessage());
		});
	}

	private void applyServidoresData(ServidoresData data) {
		model.setData(data.response().getData(), data.secciones(), data.servicios());
		refreshTable(model);
	}

	@Override
	protected void applyData(List<Servidor> data) {
		model.setData(data, cachedSecciones, cachedServicios);
		refreshTable(model);
	}

	// ── Paginación ────────────────────────────────────────────────────────

	private void setupPagination() {
		JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
		bar.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
				BorderFactory.createEmptyBorder(2, 8, 2, 8)));

		btnPrev = new JButton("◀ Anterior");
		btnPrev.setEnabled(false);
		btnPrev.addActionListener(e -> loadPage(currentPage - 1));

		lblPagination = new JLabel("Página 1 de 1  (0 servidores)");

		btnNext = new JButton("Siguiente ▶");
		btnNext.setEnabled(false);
		btnNext.addActionListener(e -> loadPage(currentPage + 1));

		bar.add(btnPrev);
		bar.add(lblPagination);
		bar.add(btnNext);

		add(bar, BorderLayout.SOUTH);
	}

	private void updatePaginationControls() {
		btnPrev.setEnabled(currentPage > 0);
		btnNext.setEnabled(currentPage < totalPages - 1);
		lblPagination.setText(String.format("Página %d de %d  (%d servidores)",
				currentPage + 1, totalPages, totalElements));
	}

	@Override
	protected void showLoading(String msg) {
		super.showLoading(msg);
		if (btnPrev != null) {
			btnPrev.setEnabled(false);
			btnNext.setEnabled(false);
		}
	}

	@Override
	protected void hideLoading() {
		super.hideLoading();
		if (btnPrev != null)
			updatePaginationControls();
	}

	// ── Permisos ──────────────────────────────────────────────────────────

	@Override
	protected boolean canEdit() {
		return guard.canManageServidores(0);
	}

	@Override
	protected boolean canDelete() {
		return guard.canManageServidores(0);
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
				loadPage(currentPage);
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
			// Si era el último elemento de la página, retroceder una página
			int targetPage = (totalElements - 1 <= currentPage * PAGE_SIZE && currentPage > 0)
					? currentPage - 1
					: currentPage;
			loadPage(targetPage);
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
			loadPage(currentPage);
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
