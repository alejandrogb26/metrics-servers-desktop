package local.alejandrogb.metricsserversdesktop.ui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import local.alejandrogb.metricsserversdesktop.client.api.UsuarioApiClient;
import local.alejandrogb.metricsserversdesktop.models.Grupo;
import local.alejandrogb.metricsserversdesktop.models.Session;
import local.alejandrogb.metricsserversdesktop.services.auth.SessionHolder;
import local.alejandrogb.metricsserversdesktop.ui.component.AvatarPanel;
import local.alejandrogb.metricsserversdesktop.ui.util.AppColors;
import local.alejandrogb.metricsserversdesktop.ui.util.SwingUtils;

/**
 * Barra superior de la aplicacion. Muestra: logo/nombre de la app, nombre del
 * usuario, grupo, badge superadmin y foto (clicable para cambiarla).
 */
public class HeaderPanel extends JPanel {

	private static final long serialVersionUID = 8755075413586683638L;

	public HeaderPanel() {
		setLayout(new BorderLayout());
		setBackground(AppColors.HEADER_BG);
		setBorder(new EmptyBorder(8, 16, 8, 16));
		setPreferredSize(new Dimension(0, 60));

		// ── Izquierda: nombre de la app ───────────────────────────────────
		JLabel appName = new JLabel("Metrics Manager");
		appName.setFont(appName.getFont().deriveFont(Font.BOLD, 16f));
		appName.setForeground(AppColors.HEADER_FG);
		add(appName, BorderLayout.WEST);

		// ── Derecha: info del usuario ─────────────────────────────────────
		JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		userPanel.setOpaque(false);

		Session session = SessionHolder.getInstance().getSession();
		if (session != null) {
			Grupo grupo = session.getGrupo();

			// Badge superadmin
			if (grupo != null && Boolean.TRUE.equals(grupo.isSuperAdmin())) {
				JLabel badge = new JLabel("SUPERADMIN");
				badge.setFont(badge.getFont().deriveFont(Font.BOLD, 10f));
				badge.setForeground(AppColors.SUPERADMIN_FG);
				badge.setBackground(AppColors.SUPERADMIN_BADGE);
				badge.setOpaque(true);
				badge.setBorder(new EmptyBorder(2, 6, 2, 6));
				userPanel.add(badge);
			}

			// Datos textuales (nombre + grupo)
			JPanel textInfo = new JPanel(new GridLayout(2, 1, 0, 0));
			textInfo.setOpaque(false);

			JLabel lblName = new JLabel(
					session.getDisplayName() != null ? session.getDisplayName() : session.getUsername());
			lblName.setFont(lblName.getFont().deriveFont(Font.BOLD, 13f));
			lblName.setForeground(AppColors.HEADER_FG);
			lblName.setHorizontalAlignment(SwingConstants.RIGHT);

			JLabel lblGroup = new JLabel(grupo != null ? grupo.getNombre() : "—");
			lblGroup.setFont(lblGroup.getFont().deriveFont(11f));
			lblGroup.setForeground(new Color(0xAABBCC));
			lblGroup.setHorizontalAlignment(SwingConstants.RIGHT);

			textInfo.add(lblName);
			textInfo.add(lblGroup);
			userPanel.add(textInfo);

			// Avatar — clicable para subir foto
			AvatarPanel avatar = new AvatarPanel(40);
			avatar.loadFromUrl(session.getUrlFoto(), session.getDisplayName());
			avatar.setToolTipText("Haz clic para cambiar tu foto de perfil");
			avatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			avatar.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mouseClicked(java.awt.event.MouseEvent e) {
					doSubirFotoUsuario(avatar, session);
				}
			});
			userPanel.add(avatar);
		}

		add(userPanel, BorderLayout.EAST);
	}

	// ── Subida de foto de perfil del usuario ──────────────────────────────

	private void doSubirFotoUsuario(AvatarPanel avatar, Session session) {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Seleccionar foto de perfil");
		fc.setFileFilter(
				new FileNameExtensionFilter("Imagenes (PNG, JPG, GIF, WEBP)", "png", "jpg", "jpeg", "gif", "webp"));
		if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;

		File file = fc.getSelectedFile();

		SwingUtils.runAsync(() -> new UsuarioApiClient().subirFoto(file.toPath()), result -> {
			// Recargar el avatar con la nueva URL si la API la devuelve,
			// o forzar recarga desde la URL actual
			String nuevaUrl = result != null && result.containsKey("urlFoto") ? result.get("urlFoto").toString()
					: session.getUrlFoto();
			avatar.loadFromUrl(nuevaUrl, session.getDisplayName());
			SwingUtils.showInfo(this, "Foto de perfil actualizada correctamente.");
		}, err -> SwingUtils.showError(this, "Error al subir la foto: " + err.getMessage()));
	}
}
