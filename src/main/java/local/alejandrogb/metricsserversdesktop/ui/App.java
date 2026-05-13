package local.alejandrogb.metricsserversdesktop.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.FlatLightLaf;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

import local.alejandrogb.metricsserversdesktop.client.config.AppConfig;
import local.alejandrogb.metricsserversdesktop.ui.login.LoginFrame;

/**
 * Punto de entrada principal de la aplicación. Configura FlatLaf como Look&Feel
 * y lanza el LoginFrame en el EDT.
 */
public class App {

	private static final Logger log = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		// ── Modo debug ────────────────────────────────────────────────────────
		// Debe configurarse antes de cualquier llamada a log, para que los
		// niveles sean correctos desde el primer mensaje.
		configureLogging();

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

	/**
	 * Configura el nivel de log de la app según {@code AppConfig.isDebug()}.
	 * <ul>
	 * <li>DEBUG → todas las peticiones HTTP, respuestas y errores detallados.</li>
	 * <li>INFO  → solo mensajes operativos (arranque, login, errores de usuario).</li>
	 * </ul>
	 * Fuentes de configuración (primera con valor gana):
	 * <ol>
	 * <li>Variable de entorno {@code DEBUG=true}</li>
	 * <li>Propiedad {@code debug=true} en config.properties</li>
	 * <li>Valor por defecto: {@code false}</li>
	 * </ol>
	 */
	private static void configureLogging() {
		AppConfig cfg = AppConfig.getInstance();
		LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
		ch.qos.logback.classic.Logger appLogger = ctx.getLogger("local.alejandrogb");

		if (cfg.isDebug()) {
			appLogger.setLevel(Level.DEBUG);
			// El banner se imprime DESPUÉS de establecer el nivel para que sea visible
			Logger boot = LoggerFactory.getLogger(App.class);
			boot.debug("╔══════════════════════════════════════════╗");
			boot.debug("║         MODO DEBUG ACTIVADO              ║");
			boot.debug("║  Logging detallado de HTTP y servicios   ║");
			boot.debug("║  Configura debug=false para producción   ║");
			boot.debug("╚══════════════════════════════════════════╝");
			boot.debug("  api.base.url     = {}", cfg.getApiBaseUrl());
			boot.debug("  api.ssl.trust-all= {}", cfg.isSslTrustAll());
			boot.debug("  connect.timeout  = {}s", cfg.getConnectTimeoutSeconds());
			boot.debug("  read.timeout     = {}s", cfg.getReadTimeoutSeconds());
		} else {
			appLogger.setLevel(Level.INFO);
		}
	}
}
