package local.alejandrogb.metricsserversdesktop.ui.component;

import local.alejandrogb.metricsserversdesktop.client.api.ApiClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TableCellRenderer que muestra una miniatura de imagen cargada de forma
 * asíncrona desde una URL.
 * <p>
 * Las imágenes se cachean en memoria. Si la URL es nula o la carga falla, se
 * muestra un placeholder con un icono genérico.
 * </p>
 */
public class ImageCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	/** Altura de la fila cuando hay imágenes. */
	public static final int ROW_HEIGHT = 48;
	/** Anchura de la columna de imagen. */
	public static final int COL_WIDTH = 56;

	private static final int IMG_SIZE = 40;

	/** Caché URL → imagen escalada (o PLACEHOLDER si falló). */
	private static final Map<String, ImageIcon> CACHE = new ConcurrentHashMap<>();
	private static final ImageIcon PLACEHOLDER = buildPlaceholder();

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setText(null);

		String url = value instanceof String s ? s : null;

		if (url == null || url.isBlank()) {
			label.setIcon(PLACEHOLDER);
			return label;
		}

		ImageIcon cached = CACHE.get(url);
		if (cached != null) {
			label.setIcon(cached == PLACEHOLDER ? PLACEHOLDER : cached);
			return label;
		}

		// Primera vez: mostrar placeholder y cargar en background
		label.setIcon(PLACEHOLDER);
		loadAsync(url, table);
		return label;
	}

	private void loadAsync(String url, JTable table) {
		// Marcar en caché con placeholder para evitar múltiples workers
		CACHE.put(url, PLACEHOLDER);

		new SwingWorker<ImageIcon, Void>() {
			@Override
			protected ImageIcon doInBackground() {
				try {
					byte[] bytes = ApiClient.downloadImageBytes(url);
					if (bytes == null)
						return PLACEHOLDER;
					BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
					if (img == null)
						return PLACEHOLDER;
					Image scaled = img.getScaledInstance(IMG_SIZE, IMG_SIZE, Image.SCALE_SMOOTH);
					return new ImageIcon(scaled);
				} catch (Exception e) {
					return PLACEHOLDER;
				}
			}

			@Override
			protected void done() {
				try {
					ImageIcon icon = get();
					CACHE.put(url, icon);
					// Forzar repintado de la tabla para mostrar la imagen cargada
					table.repaint();
				} catch (Exception ignored) {
				}
			}
		}.execute();
	}

	/** Construye un icono gris genérico como placeholder. */
	private static ImageIcon buildPlaceholder() {
		BufferedImage img = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(new Color(0xDDDDDD));
		g.fillRoundRect(0, 0, IMG_SIZE, IMG_SIZE, 8, 8);
		g.setColor(new Color(0xAAAAAA));
		// Dibujar icono de imagen simplificado
		int m = 8;
		g.drawRoundRect(m, m, IMG_SIZE - 2 * m, IMG_SIZE - 2 * m, 4, 4);
		int cx = IMG_SIZE / 2, cy = IMG_SIZE / 2;
		g.fillOval(cx - 4, cy - 6, 8, 8);
		int[] px = { m + 2, cx - 5, cx + 5, IMG_SIZE - m - 2 };
		int[] py = { IMG_SIZE - m - 2, cy + 2, cy + 4, IMG_SIZE - m - 2 };
		g.fillPolygon(px, py, 4);
		g.dispose();
		return new ImageIcon(img);
	}
}
