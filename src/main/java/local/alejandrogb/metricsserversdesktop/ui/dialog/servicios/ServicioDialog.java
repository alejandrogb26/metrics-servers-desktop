package local.alejandrogb.metricsserversdesktop.ui.dialog.servicios;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.io.File;
import java.nio.file.Path;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import local.alejandrogb.metricsserversdesktop.models.Servicio;
import local.alejandrogb.metricsserversdesktop.ui.util.SwingUtils;

public class ServicioDialog extends JDialog {

	private static final long serialVersionUID = -2501116656033597983L;
	private final Servicio existing;
	private boolean confirmed = false;

	private JTextField txtNombre;
	private JLabel lblLogoSelected;
	private Path logoPath;

	public ServicioDialog(Window parent, Servicio existing) {
		super(parent, existing == null ? "Nuevo Servicio" : "Editar Servicio", ModalityType.APPLICATION_MODAL);
		this.existing = existing;
		buildUI();
		if (existing != null)
			populateFields();
		pack();
		setMinimumSize(new Dimension(380, 200));
		setResizable(true);
		setLocationRelativeTo(parent);
	}

	private void buildUI() {
		JPanel main = new JPanel(new BorderLayout(0, 8));
		main.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

		JPanel form = new JPanel(new GridBagLayout());
		int row = 0;

		form.add(new JLabel("Nombre *"), SwingUtils.gbc(0, row, GridBagConstraints.NONE, 0));
		txtNombre = new JTextField(24);
		form.add(txtNombre, SwingUtils.gbc(1, row++, GridBagConstraints.HORIZONTAL, 1.0));

		form.add(new JLabel("Logo"), SwingUtils.gbc(0, row, GridBagConstraints.NONE, 0));
		JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		JButton btnLogo = new JButton("Seleccionar...");
		lblLogoSelected = new JLabel("Sin logo seleccionado");
		btnLogo.addActionListener(e -> chooseLogo());
		logoPanel.add(btnLogo);
		logoPanel.add(lblLogoSelected);
		form.add(logoPanel, SwingUtils.gbc(1, row++, GridBagConstraints.HORIZONTAL, 1.0));

		main.add(form, BorderLayout.CENTER);

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

	private void populateFields() {
		txtNombre.setText(existing.getNombre());
		if (existing.getUrlLogo() != null && !existing.getUrlLogo().isEmpty()) {
			lblLogoSelected.setText("Logo actual cargado");
		}
	}

	private void chooseLogo() {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Seleccionar logo para el servicio");
		fc.setFileFilter(new FileNameExtensionFilter("Imagenes (PNG, JPG, GIF, WEBP)", "png", "jpg", "jpeg", "gif", "webp"));
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			logoPath = file.toPath();
			lblLogoSelected.setText(file.getName());
		}
	}

	private void doConfirm() {
		if (txtNombre.getText().trim().isEmpty()) {
			SwingUtils.showError(this, "El nombre es obligatorio.");
			txtNombre.requestFocus();
			return;
		}
		confirmed = true;
		dispose();
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public Servicio getServicio() {
		Servicio s = new Servicio();
		s.setNombre(txtNombre.getText().trim());
		return s;
	}

	/** Ruta del fichero de logo seleccionado, o {@code null} si no se eligió ninguno. */
	public Path getLogoPath() {
		return logoPath;
	}
}
