package local.alejandrogb.metricsserversdesktop.ui.login;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import local.alejandrogb.metricsserversdesktop.client.exception.ApiException;
import local.alejandrogb.metricsserversdesktop.services.auth.AuthService;
import local.alejandrogb.metricsserversdesktop.ui.main.MainFrame;
import local.alejandrogb.metricsserversdesktop.ui.util.AppColors;
import local.alejandrogb.metricsserversdesktop.ui.util.SwingUtils;

/**
 * Pantalla de inicio de sesión. Valida campos localmente, llama a AuthService y
 * abre MainFrame si el login es correcto.
 */
public class LoginFrame extends JFrame {

	private static final long serialVersionUID = 5148300175023760668L;

	private final AuthService authService = new AuthService();

	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JButton btnLogin;
	private JLabel lblError;

	public LoginFrame() {
		super("Metrics Manager — Inicio de sesión");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		buildUI();
		pack();
		SwingUtils.centerOn(this, null);
	}

	private void buildUI() {
		// Panel raíz con fondo degradado
		JPanel root = new JPanel(new GridBagLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				GradientPaint gp = new GradientPaint(0, 0, new Color(0x1E2A3A), 0, getHeight(), new Color(0x2C3E50));
				g2.setPaint(gp);
				g2.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		root.setPreferredSize(new Dimension(420, 540));
		setContentPane(root);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(6, 0, 6, 0);

		// ── Logo / Título ─────────────────────────────────────────────────
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		JLabel logoLabel = new JLabel("📊", SwingConstants.CENTER);
		logoLabel.setFont(logoLabel.getFont().deriveFont(56f));
		logoLabel.setBorder(new EmptyBorder(28, 0, 8, 0));
		root.add(logoLabel, c);

		c.gridy++;
		JLabel appTitle = new JLabel("Metrics Manager", SwingConstants.CENTER);
		appTitle.setFont(appTitle.getFont().deriveFont(Font.BOLD, 22f));
		appTitle.setForeground(Color.WHITE);
		root.add(appTitle, c);

		c.gridy++;
		JLabel subtitle = new JLabel("Panel de administración", SwingConstants.CENTER);
		subtitle.setFont(subtitle.getFont().deriveFont(13f));
		subtitle.setForeground(new Color(0xAABBCC));
		subtitle.setBorder(new EmptyBorder(0, 0, 16, 0));
		root.add(subtitle, c);

		// ── Tarjeta de formulario ─────────────────────────────────────────
		JPanel card = new JPanel(new GridBagLayout());
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xDEE2E6), 1, true),
				new EmptyBorder(24, 28, 24, 28)));

		GridBagConstraints fc = new GridBagConstraints();
		fc.fill = GridBagConstraints.HORIZONTAL;
		fc.weightx = 1.0;
		fc.insets = new Insets(4, 0, 4, 0);

		// Usuario
		fc.gridx = 0;
		fc.gridy = 0;
		JLabel lblUser = new JLabel("Usuario");
		lblUser.setFont(lblUser.getFont().deriveFont(Font.BOLD, 12f));
		card.add(lblUser, fc);

		fc.gridy++;
		txtUsername = new JTextField(20);
		txtUsername.setPreferredSize(new Dimension(0, 36));
		card.add(txtUsername, fc);

		// Contraseña
		fc.gridy++;
		fc.insets = new Insets(10, 0, 4, 0);
		JLabel lblPass = new JLabel("Contraseña");
		lblPass.setFont(lblPass.getFont().deriveFont(Font.BOLD, 12f));
		card.add(lblPass, fc);

		fc.gridy++;
		fc.insets = new Insets(4, 0, 4, 0);
		txtPassword = new JPasswordField(20);
		txtPassword.setPreferredSize(new Dimension(0, 36));
		card.add(txtPassword, fc);

		// Error label (inicialmente oculto)
		fc.gridy++;
		fc.insets = new Insets(8, 0, 0, 0);
		lblError = new JLabel(" ");
		lblError.setForeground(AppColors.DANGER);
		lblError.setFont(lblError.getFont().deriveFont(12f));
		lblError.setHorizontalAlignment(SwingConstants.CENTER);
		card.add(lblError, fc);

		// Botón login
		fc.gridy++;
		fc.insets = new Insets(12, 0, 0, 0);
		btnLogin = new JButton("Iniciar sesión");
		btnLogin.setPreferredSize(new Dimension(0, 40));
		btnLogin.setBackground(AppColors.PRIMARY);
		btnLogin.setForeground(Color.WHITE);
		btnLogin.setFocusPainted(false);
		btnLogin.setFont(btnLogin.getFont().deriveFont(Font.BOLD, 14f));
		btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		card.add(btnLogin, fc);

		c.gridy++;
		c.insets = new Insets(0, 28, 28, 28);
		root.add(card, c);

		// ── Listeners ─────────────────────────────────────────────────────
		btnLogin.addActionListener(this::doLogin);

		// Enter en cualquier campo dispara el login
		Action loginAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doLogin(e);
			}
		};
		txtUsername.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "login");
		txtUsername.getActionMap().put("login", loginAction);
		txtPassword.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "login");
		txtPassword.getActionMap().put("login", loginAction);
	}

	private void doLogin(ActionEvent e) {
		String username = txtUsername.getText().trim();
		String password = new String(txtPassword.getPassword());

		if (username.isEmpty()) {
			showError("Por favor, introduce tu usuario.");
			txtUsername.requestFocus();
			return;
		}
		if (password.isEmpty()) {
			showError("Por favor, introduce tu contraseña.");
			txtPassword.requestFocus();
			return;
		}

		setFormEnabled(false);
		lblError.setText("Autenticando…");
		lblError.setForeground(Color.GRAY);

		SwingUtils.runAsync(() -> authService.login(username, password), resp -> {
			dispose();
			new MainFrame().setVisible(true);
		}, err -> {
			setFormEnabled(true);
			String msg;
			if (err instanceof ApiException ae) {
				msg = ae.isUnauthorized() ? "Credenciales inválidas o usuario sin permisos."
						: ae.isValidationError() ? "Usuario y contraseña son obligatorios."
								: "Error al conectar con el servidor (" + ae.getStatusCode() + ").";
			} else {
				msg = "No se puede conectar con la API. Comprueba la configuración.";
			}
			showError(msg);
		});
	}

	private void showError(String msg) {
		lblError.setText(msg);
		lblError.setForeground(AppColors.DANGER);
	}

	private void setFormEnabled(boolean enabled) {
		txtUsername.setEnabled(enabled);
		txtPassword.setEnabled(enabled);
		btnLogin.setEnabled(enabled);
		btnLogin.setText(enabled ? "Iniciar sesión" : "Conectando…");
	}
}
