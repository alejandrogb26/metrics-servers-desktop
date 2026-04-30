package local.alejandrogb.metricsserversdesktop.ui.dialog.servidores;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import local.alejandrogb.metricsserversdesktop.models.Seccion;
import local.alejandrogb.metricsserversdesktop.models.Servicio;
import local.alejandrogb.metricsserversdesktop.models.servidor.Servidor;
import local.alejandrogb.metricsserversdesktop.models.servidor.ServidorDTO;
import local.alejandrogb.metricsserversdesktop.services.crud.SeccionService;
import local.alejandrogb.metricsserversdesktop.services.crud.ServicioService;
import local.alejandrogb.metricsserversdesktop.ui.util.SwingUtils;

/**
 * Diálogo modal para crear o editar un servidor.
 * <p>
 * Solo expone los campos que el usuario debe introducir: Server ID, DNS,
 * Sección y Servicios. Los campos hostname, OS, arquitectura y kernel los
 * rellena automáticamente la API al registrar el servidor.
 * </p>
 */
public class ServidorDialog extends JDialog {

	private static final long serialVersionUID = -8416329043766969578L;
	private final Servidor existing;
	private boolean confirmed = false;

	// Únicos campos editables por el usuario
	private JTextField txtServerId;
	private JTextField txtDns;
	private JComboBox<Seccion> cmbSeccion;
	private JList<Servicio> lstServicios;
	private DefaultListModel<Servicio> serviciosModel;

	private List<Seccion> secciones = new ArrayList<>();
	private List<Servicio> servicios = new ArrayList<>();

	public ServidorDialog(Window parent, Servidor existing) {
		super(parent, existing == null ? "Nuevo Servidor" : "Editar Servidor", ModalityType.APPLICATION_MODAL);
		this.existing = existing;
		buildUI();
		loadCombos();
		if (existing != null)
			populateFields();
		pack();
		setMinimumSize(new Dimension(420, 320));
		setResizable(true);
		setLocationRelativeTo(parent);
	}

	private void buildUI() {
		JPanel main = new JPanel(new BorderLayout(0, 8));
		main.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

		JPanel form = new JPanel(new GridBagLayout());
		int row = 0;

		// Server ID
		form.add(new JLabel("Server ID *"), SwingUtils.gbc(0, row, GridBagConstraints.NONE, 0));
		txtServerId = new JTextField(20);
		form.add(txtServerId, SwingUtils.gbc(1, row++, GridBagConstraints.HORIZONTAL, 1.0));

		// DNS
		form.add(new JLabel("DNS *"), SwingUtils.gbc(0, row, GridBagConstraints.NONE, 0));
		txtDns = new JTextField(20);
		form.add(txtDns, SwingUtils.gbc(1, row++, GridBagConstraints.HORIZONTAL, 1.0));

		// Sección
		form.add(new JLabel("Sección *"), SwingUtils.gbc(0, row, GridBagConstraints.NONE, 0));
		cmbSeccion = new JComboBox<>();
		form.add(cmbSeccion, SwingUtils.gbc(1, row++, GridBagConstraints.HORIZONTAL, 1.0));

		// Servicios
		form.add(new JLabel("Servicios"), SwingUtils.gbc(0, row, GridBagConstraints.NONE, 0));
		serviciosModel = new DefaultListModel<>();
		lstServicios = new JList<>(serviciosModel);
		lstServicios.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		lstServicios.setVisibleRowCount(5);
		JScrollPane scrollServ = new JScrollPane(lstServicios);
		scrollServ.setPreferredSize(new Dimension(0, 110));
		GridBagConstraints gbcList = SwingUtils.gbc(1, row++, GridBagConstraints.BOTH, 1.0);
		gbcList.weighty = 1.0;
		form.add(scrollServ, gbcList);

		main.add(form, BorderLayout.CENTER);

		// Botones
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton btnCancel = new JButton("Cancelar");
		JButton btnOk = new JButton(existing == null ? "Crear" : "Guardar");
		btnOk.setBackground(new Color(0x2C7BE5));
		btnOk.setForeground(Color.WHITE);
		btnPanel.add(btnCancel);
		btnPanel.add(btnOk);
		main.add(btnPanel, BorderLayout.SOUTH);

		btnCancel.addActionListener(e -> dispose());
		btnOk.addActionListener(e -> doConfirm());

		setContentPane(main);
	}

	private void loadCombos() {
		SwingUtils.runAsync(() -> {
			secciones = new SeccionService().findAll();
			servicios = new ServicioService().findAll();
			return null;
		}, v -> {
			cmbSeccion.removeAllItems();
			secciones.forEach(cmbSeccion::addItem);
			serviciosModel.clear();
			servicios.forEach(serviciosModel::addElement);
			if (existing != null)
				preselectFields();
		}, err -> SwingUtils.showError(this, "Error cargando datos: " + err.getMessage()));
	}

	/** Rellena los campos de texto con los valores del servidor existente. */
	private void populateFields() {
		txtServerId.setText(existing.getServerId() != null ? existing.getServerId() : "");
		txtDns.setText(existing.getDns() != null ? existing.getDns() : "");
	}

	/** Preselecciona sección y servicios en los controles de selección. */
	private void preselectFields() {
		// Sección
		for (int i = 0; i < cmbSeccion.getItemCount(); i++) {
			if (cmbSeccion.getItemAt(i).getId() == existing.getSeccion()) {
				cmbSeccion.setSelectedIndex(i);
				break;
			}
		}
		// Servicios
		if (existing.getServicios() != null) {
			List<Integer> ids = existing.getServicios();
			List<Integer> indices = new ArrayList<>();
			for (int i = 0; i < serviciosModel.size(); i++) {
				if (ids.contains(serviciosModel.get(i).getId()))
					indices.add(i);
			}
			lstServicios.setSelectedIndices(indices.stream().mapToInt(Integer::intValue).toArray());
		}
	}

	private void doConfirm() {
		String serverId = txtServerId.getText().trim();
		String dns = txtDns.getText().trim();
		Seccion seccion = (Seccion) cmbSeccion.getSelectedItem();

		if (serverId.isEmpty()) {
			SwingUtils.showError(this, "El campo 'Server ID' es obligatorio.");
			txtServerId.requestFocus();
			return;
		}
		if (dns.isEmpty()) {
			SwingUtils.showError(this, "El campo 'DNS' es obligatorio.");
			txtDns.requestFocus();
			return;
		}
		if (seccion == null) {
			SwingUtils.showError(this, "Debe seleccionar una sección.");
			return;
		}
		confirmed = true;
		dispose();
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	/** DTO para crear un servidor via POST /servidor/bulk. */
	public ServidorDTO getServidorDTO() {
		Seccion sec = (Seccion) cmbSeccion.getSelectedItem();
		List<Integer> servicioIds = lstServicios.getSelectedValuesList().stream().map(Servicio::getId).toList();
		return new ServidorDTO(txtServerId.getText().trim(), txtDns.getText().trim(), sec != null ? sec.getId() : 0,
				servicioIds);
	}

	/**
	 * Servidor con solo los campos editables para PATCH. hostname, prettyOs, arch y
	 * kernel NO se incluyen — los gestiona la API.
	 */
	public Servidor getServidor() {
		Servidor s = new Servidor();
		s.setServerId(txtServerId.getText().trim());
		s.setDns(txtDns.getText().trim());
		Seccion sec = (Seccion) cmbSeccion.getSelectedItem();
		if (sec != null)
			s.setSeccion(sec.getId());
		List<Integer> servicioIds = lstServicios.getSelectedValuesList().stream().map(Servicio::getId).toList();
		s.setServicios(servicioIds);
		return s;
	}
}
