package local.alejandrogb.metricsserversdesktop.ui.component;

import local.alejandrogb.metricsserversdesktop.client.api.ApiClient;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/**
 * Panel circular que muestra la foto de perfil del usuario. Si la URL no está
 * disponible, muestra las iniciales del nombre.
 */
public class AvatarPanel extends JPanel {

	private static final long serialVersionUID = 5678853647437724792L;
	private BufferedImage photo;
	private String initials = "?";
	private final int size;

	public AvatarPanel(int size) {
		this.size = size;
		setPreferredSize(new Dimension(size, size));
		setMinimumSize(new Dimension(size, size));
		setMaximumSize(new Dimension(size, size));
		setOpaque(false);
	}

	/** Carga la foto desde una URL de forma asíncrona. */
	public void loadFromUrl(String url, String displayName) {
		setInitials(displayName);
		if (url == null || url.isBlank()) {
			repaint();
			return;
		}
		SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
			@Override
			protected BufferedImage doInBackground() throws Exception {
				byte[] bytes = ApiClient.downloadImageBytes(url);
				return bytes != null ? ImageIO.read(new ByteArrayInputStream(bytes)) : null;
			}

			@Override
			protected void done() {
				try {
					photo = get();
				} catch (Exception ignored) {
					photo = null;
				}
				repaint();
			}
		};
		worker.execute();
	}

	public void setInitials(String displayName) {
		if (displayName == null || displayName.isBlank()) {
			initials = "?";
			return;
		}
		String[] parts = displayName.trim().split("\\s+");
		if (parts.length >= 2) {
			initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
		} else {
			initials = displayName.substring(0, Math.min(2, displayName.length())).toUpperCase();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Ellipse2D circle = new Ellipse2D.Float(0, 0, size, size);
		g2.setClip(circle);

		if (photo != null) {
			// Escala y centra manteniendo aspecto
			int iw = photo.getWidth(), ih = photo.getHeight();
			float scale = (float) size / Math.min(iw, ih);
			int dw = (int) (iw * scale), dh = (int) (ih * scale);
			int ox = (size - dw) / 2, oy = (size - dh) / 2;
			g2.drawImage(photo, ox, oy, dw, dh, null);
		} else {
			// Fondo degradado + iniciales
			GradientPaint gp = new GradientPaint(0, 0, new Color(0x2C7BE5), size, size, new Color(0x6F42C1));
			g2.setPaint(gp);
			g2.fillOval(0, 0, size, size);
			g2.setClip(null);
			g2.setColor(Color.WHITE);
			g2.setFont(g2.getFont().deriveFont(Font.BOLD, size * 0.36f));
			FontMetrics fm = g2.getFontMetrics();
			int tx = (size - fm.stringWidth(initials)) / 2;
			int ty = (size - fm.getHeight()) / 2 + fm.getAscent();
			g2.drawString(initials, tx, ty);
		}
		g2.dispose();
	}
}
