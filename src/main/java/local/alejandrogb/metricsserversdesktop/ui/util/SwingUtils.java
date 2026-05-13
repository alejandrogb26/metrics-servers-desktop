package local.alejandrogb.metricsserversdesktop.ui.util;

import javax.swing.*;
import java.awt.*;

/**
 * Utilidades Swing de uso general en toda la aplicación.
 */
public final class SwingUtils {

	private SwingUtils() {
	}

	/** Centra una ventana respecto a otra (o en pantalla si parent es null). */
	public static void centerOn(Window window, Window parent) {
		if (parent == null) {
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			window.setLocation((screen.width - window.getWidth()) / 2, (screen.height - window.getHeight()) / 2);
		} else {
			window.setLocationRelativeTo(parent);
		}
	}

	/** Muestra un diálogo de error modal. */
	public static void showError(Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	/** Muestra un diálogo de información modal. */
	public static void showInfo(Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "Información", JOptionPane.INFORMATION_MESSAGE);
	}

	/** Muestra un diálogo de confirmación y devuelve true si el usuario acepta. */
	public static boolean confirm(Component parent, String message) {
		int r = JOptionPane.showConfirmDialog(parent, message, "Confirmación", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		return r == JOptionPane.YES_OPTION;
	}

	/**
	 * Crea un JLabel con estilo de título de sección (fuente más grande, negrita).
	 */
	public static JLabel sectionTitle(String text) {
		JLabel lbl = new JLabel(text);
		lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 15f));
		return lbl;
	}

	/**
	 * Crea una barra de herramientas estándar sin borde flotante.
	 */
	public static JToolBar createToolBar() {
		JToolBar tb = new JToolBar();
		tb.setFloatable(false);
		tb.setBorderPainted(false);
		return tb;
	}

	/**
	 * Crea un botón de acción con texto y tooltip opcionales.
	 */
	public static JButton actionButton(String text, String tooltip) {
		JButton btn = new JButton(text);
		if (tooltip != null)
			btn.setToolTipText(tooltip);
		return btn;
	}

	/**
	 * Construye un GridBagConstraints preconfigurado para un campo de formulario.
	 *
	 * @param gridx   columna
	 * @param gridy   fila
	 * @param fill    constante GridBagConstraints.FILL_*
	 * @param weightx peso horizontal
	 */
	public static GridBagConstraints gbc(int gridx, int gridy, int fill, double weightx) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = gridx;
		c.gridy = gridy;
		c.fill = fill;
		c.weightx = weightx;
		c.insets = new Insets(4, 6, 4, 6);
		c.anchor = GridBagConstraints.WEST;
		return c;
	}

	/** GBC con gridwidth personalizado. */
	public static GridBagConstraints gbc(int gridx, int gridy, int fill, double weightx, int gridwidth) {
		GridBagConstraints c = gbc(gridx, gridy, fill, weightx);
		c.gridwidth = gridwidth;
		return c;
	}

	/**
	 * Escala una ImageIcon a las dimensiones indicadas manteniendo suavizado.
	 */
	public static ImageIcon scaleIcon(ImageIcon icon, int w, int h) {
		Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
		return new ImageIcon(scaled);
	}

	/**
	 * Carga un ImageIcon desde el classpath. Devuelve null si no existe.
	 */
	public static ImageIcon loadIcon(String resourcePath) {
		try {
			var url = SwingUtils.class.getResource(resourcePath);
			return url != null ? new ImageIcon(url) : null;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Envuelve una llamada en SwingWorker para no bloquear el EDT.
	 *
	 * @param background lógica de fondo (llamada de red, etc.)
	 * @param onDone     se ejecuta en EDT cuando background termina sin error
	 * @param onError    se ejecuta en EDT con el error si background lanza
	 *                   excepción
	 */
	public static <T> SwingWorker<T, Void> runAsync(ThrowingSupplier<T> background,
			java.util.function.Consumer<T> onDone, java.util.function.Consumer<Throwable> onError) {

		SwingWorker<T, Void> worker = new SwingWorker<T, Void>() {
			@Override
			protected T doInBackground() throws Exception {
				return background.get();
			}

			@Override
			protected void done() {
				try {
					onDone.accept(get());
				} catch (java.util.concurrent.ExecutionException ee) {
					onError.accept(ee.getCause() != null ? ee.getCause() : ee);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					onError.accept(ie);
				}
			}
		};
		worker.execute();
		return worker;
	}

	@FunctionalInterface
	public interface ThrowingSupplier<T> {
		T get() throws Exception;
	}
}
