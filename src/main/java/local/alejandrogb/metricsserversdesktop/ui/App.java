package local.alejandrogb.metricsserversdesktop.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.FlatLightLaf;

import local.alejandrogb.metricsserversdesktop.ui.login.LoginFrame;

/**
 * Punto de entrada principal de la aplicación. Configura FlatLaf como Look&Feel
 * y lanza el LoginFrame en el EDT.
 */
public class App {

	private static final Logger log = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		// Propiedades del sistema para mejor renderizado en HiDPI
		System.setProperty("sun.java2d.uiScale.enabled", "true");
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");

		// Configurar FlatLaf antes de crear cualquier componente Swing
		try {
			// Ajustes globales de FlatLaf
			UIManager.put("defaultFont", new javax.swing.plaf.FontUIResource("Segoe UI", java.awt.Font.PLAIN, 13));

			FlatLightLaf.setup();

			// Personalizaciones adicionales del tema
			UIManager.put("Button.arc", 6);
			UIManager.put("Component.arc", 6);
			UIManager.put("TextComponent.arc", 6);
			UIManager.put("Table.showHorizontalLines", true);
			UIManager.put("Table.showVerticalLines", false);
			UIManager.put("TableHeader.height", 32);
			UIManager.put("ScrollBar.thumbArc", 999);
			UIManager.put("ScrollBar.width", 10);

		} catch (Exception e) {
			log.warn("No se pudo aplicar FlatLaf, usando Look&Feel por defecto: {}", e.getMessage());
		}

		// Arrancar en el Event Dispatch Thread
		SwingUtilities.invokeLater(() -> {
			log.info("Iniciando Metrics Manager");
			LoginFrame frame = new LoginFrame();
			frame.setVisible(true);
		});
	}
}
