package local.alejandrogb.metricsserversdesktop.client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Carga la configuración desde config.properties en el classpath. Singleton de
 * acceso estático para toda la app.
 */
public class AppConfig {

	private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
	private static final String CONFIG_FILE = "config.properties";

	private static final AppConfig INSTANCE = new AppConfig();
	private final Properties props = new Properties();

	private AppConfig() {
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
			if (is != null) {
				props.load(is);
				log.info("Configuración cargada desde {}", CONFIG_FILE);
			} else {
				log.warn("{} no encontrado en classpath, usando valores por defecto", CONFIG_FILE);
			}
		} catch (IOException e) {
			log.error("Error leyendo {}: {}", CONFIG_FILE, e.getMessage());
		}
	}

	public static AppConfig getInstance() {
		return INSTANCE;
	}

	public String get(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

	public String getApiBaseUrl() {
		return get("api.base.url", "http://localhost:8080/api");
	}

	public int getConnectTimeoutSeconds() {
		return Integer.parseInt(get("api.connect.timeout", "10"));
	}

	public int getReadTimeoutSeconds() {
		return Integer.parseInt(get("api.read.timeout", "30"));
	}

	/** true → HttpClient acepta cualquier certificado (solo desarrollo/pruebas). */
	public boolean isSslTrustAll() {
		return Boolean.parseBoolean(get("api.ssl.trust-all", "false"));
	}

	/**
	 * true → nivel de log DEBUG activo para toda la app.
	 * <p>
	 * Prioridad: variable de entorno {@code DEBUG} > propiedad {@code debug} en
	 * config.properties > {@code false} por defecto.
	 * </p>
	 */
	public boolean isDebug() {
		String envDebug = System.getenv("DEBUG");
		if (envDebug != null && !envDebug.isBlank()) {
			return Boolean.parseBoolean(envDebug);
		}
		return Boolean.parseBoolean(get("debug", "false"));
	}
}
