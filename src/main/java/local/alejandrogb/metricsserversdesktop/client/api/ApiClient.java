package local.alejandrogb.metricsserversdesktop.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import local.alejandrogb.metricsserversdesktop.client.config.AppConfig;
import local.alejandrogb.metricsserversdesktop.client.exception.ApiException;
import local.alejandrogb.metricsserversdesktop.models.PageResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Cliente HTTP central. Gestiona la URL base, el token JWT y la
 * serialización/deserialización Jackson.
 * <p>
 * Todos los api-clients específicos extienden esta clase.
 */
public class ApiClient {

	private static final Logger log = LoggerFactory.getLogger(ApiClient.class);

	protected static final ObjectMapper MAPPER = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private final HttpClient http;
	private final String baseUrl;

	/** Token JWT activo. Se establece tras un login correcto. */
	private static volatile String jwtToken;

	/**
	 * Cliente HTTP dedicado para descarga de imágenes (logos, avatares).
	 * Comparte la misma configuración SSL que el cliente de API para que los
	 * certificados autofirmados en entorno dev sean aceptados también por
	 * ImageIO / AvatarPanel, que no usan el HttpClient de instancia.
	 */
	private static volatile HttpClient imageClient;

	/** Descarga una URL como bytes usando el cliente SSL configurado. Devuelve {@code null} si falla. */
	public static byte[] downloadImageBytes(String url) {
		if (url == null || url.isBlank())
			return null;
		try {
			HttpClient client = getImageHttpClient();
			HttpRequest req = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.GET()
					.timeout(Duration.ofSeconds(10))
					.build();
			HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
			return resp.statusCode() >= 200 && resp.statusCode() < 300 ? resp.body() : null;
		} catch (Exception e) {
			return null;
		}
	}

	private static HttpClient getImageHttpClient() {
		if (imageClient == null) {
			synchronized (ApiClient.class) {
				if (imageClient == null) {
					HttpClient.Builder b = HttpClient.newBuilder()
							.connectTimeout(Duration.ofSeconds(10));
					if (AppConfig.getInstance().isSslTrustAll()) {
						b.sslContext(buildTrustAllSslContext()).sslParameters(buildTrustAllSslParams());
					}
					imageClient = b.build();
				}
			}
		}
		return imageClient;
	}

	public ApiClient() {
		AppConfig cfg = AppConfig.getInstance();
		this.baseUrl = cfg.getApiBaseUrl();
		HttpClient.Builder builder = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(cfg.getConnectTimeoutSeconds()));
		if (cfg.isSslTrustAll()) {
			log.warn("*** SSL trust-all ACTIVO: se aceptan certificados autofirmados. Solo para desarrollo. ***");
			builder.sslContext(buildTrustAllSslContext()).sslParameters(buildTrustAllSslParams());
		}
		this.http = builder.build();
	}

	// ── Token ─────────────────────────────────────────────────────────────

	public static void setToken(String token) {
		jwtToken = token;
	}

	public static void clearToken() {
		jwtToken = null;
	}

	public static String getToken() {
		return jwtToken;
	}

	// ── Request builders ──────────────────────────────────────────────────

	protected HttpRequest.Builder baseRequest(String path) {
		HttpRequest.Builder b = HttpRequest.newBuilder().uri(URI.create(baseUrl + path))
				.timeout(Duration.ofSeconds(AppConfig.getInstance().getReadTimeoutSeconds()))
				.header("Content-Type", "application/json").header("Accept", "application/json");
		if (jwtToken != null && !jwtToken.isBlank()) {
			b.header("Authorization", "Bearer " + jwtToken);
		}
		return b;
	}

	// ── Execute helpers ───────────────────────────────────────────────────

	protected <T> T get(String path, TypeReference<T> type) {
		HttpRequest req = baseRequest(path).GET().build();
		return execute(req, type);
	}

	/**
	 * Obtiene la primera página de un endpoint paginado y devuelve solo el array
	 * {@code data}. Usado exclusivamente para bootstrap en arranque (e.g. catálogo
	 * de permisos), no para listados de UI.
	 */
	protected <T> List<T> getDataPage(String path, int size, TypeReference<List<T>> itemType) {
		String sep = path.contains("?") ? "&" : "?";
		HttpRequest req = baseRequest(path + sep + "page=0&size=" + size).GET().build();
		try {
			log.debug("--> GET {} (paginado)", req.uri());
			HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
			logResponse(req, resp);
			checkStatus(resp);
			if (resp.body() == null || resp.body().isBlank())
				return List.of();
			JsonNode root = MAPPER.readTree(resp.body());
			JsonNode data = root.path("data");
			List<T> result = data.isArray() ? MAPPER.convertValue(data, itemType) : List.of();
			log.debug("    {} elementos en data[]", result.size());
			JsonNode hasNext = root.path("hasNext");
			if (hasNext.isBoolean() && hasNext.booleanValue()) {
				log.warn("Respuesta truncada en {} elementos; hay más páginas disponibles", result.size());
			}
			return result;
		} catch (ApiException e) {
			throw e;
		} catch (IOException | InterruptedException e) {
			throw wrap(e);
		}
	}

	/**
	 * Obtiene una página concreta de un endpoint paginado y devuelve el
	 * {@link PageResponse} completo con los metadatos de paginación.
	 *
	 * @param path     ruta relativa (sin query string de paginación)
	 * @param page     número de página (base 0)
	 * @param size     tamaño de página
	 * @param itemType TypeReference del tipo de elemento dentro de {@code data}
	 */
	protected <T> PageResponse<T> getPage(String path, int page, int size, TypeReference<List<T>> itemType) {
		String sep = path.contains("?") ? "&" : "?";
		HttpRequest req = baseRequest(path + sep + "page=" + page + "&size=" + size).GET().build();
		try {
			log.debug("--> GET {} (página {})", req.uri(), page);
			HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
			logResponse(req, resp);
			checkStatus(resp);
			if (resp.body() == null || resp.body().isBlank())
				return new PageResponse<>();
			JsonNode root = MAPPER.readTree(resp.body());
			JsonNode dataNode = root.path("data");
			List<T> items = dataNode.isArray() ? MAPPER.convertValue(dataNode, itemType) : List.of();

			PageResponse<T> result = new PageResponse<>();
			result.setData(items);
			result.setPage(root.path("page").asInt(0));
			result.setSize(root.path("size").asInt(size));
			result.setTotal(root.path("total").asInt(0));
			result.setTotalPages(root.path("totalPages").asInt(1));
			result.setHasNext(root.path("hasNext").asBoolean(false));

			log.debug("    {} elementos, total={}, página {}/{}", items.size(), result.getTotal(),
					result.getPage() + 1, result.getTotalPages());
			return result;
		} catch (ApiException e) {
			throw e;
		} catch (IOException | InterruptedException e) {
			throw wrap(e);
		}
	}

	protected <T> T post(String path, Object body, TypeReference<T> type) {
		String json = toJson(body);
		HttpRequest req = baseRequest(path).POST(HttpRequest.BodyPublishers.ofString(json)).build();
		return execute(req, type);
	}

	protected <T> T patch(String path, Object body, TypeReference<T> type) {
		String json = toJson(body);
		HttpRequest req = baseRequest(path).method("PATCH", HttpRequest.BodyPublishers.ofString(json)).build();
		return execute(req, type);
	}

	protected void delete(String path) {
		HttpRequest req = baseRequest(path).DELETE().build();
		executeVoid(req);
	}

	protected <T> T deleteWithBody(String path, Object body, TypeReference<T> type) {
		String json = toJson(body);
		HttpRequest req = baseRequest(path).method("DELETE", HttpRequest.BodyPublishers.ofString(json)).build();
		return execute(req, type);
	}

	/**
	 * Sube un fichero como {@code multipart/form-data} con el campo "file".
	 * Compatible con los endpoints {@code POST /servidor/{id}/foto} y
	 * {@code POST /usuario/foto} de la API.
	 *
	 * @param path ruta relativa del endpoint (e.g. {@code /servidor/3/foto})
	 * @param file fichero local a subir
	 * @param type tipo de retorno esperado
	 */
	protected <T> T postMultipart(String path, Path file, TypeReference<T> type) {
		try {
			String boundary = "----FormBoundary" + UUID.randomUUID().toString().replace("-", "");
			String fileName = file.getFileName().toString();
			String mimeType = guessMimeType(fileName);

			// Construir cuerpo multipart manualmente
			byte[] fileBytes = Files.readAllBytes(file);

			String partHeader = "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"file\"; filename=\""
					+ fileName + "\"\r\n" + "Content-Type: " + mimeType + "\r\n\r\n";
			String closing = "\r\n--" + boundary + "--\r\n";

			byte[] partHeaderBytes = partHeader.getBytes();
			byte[] closingBytes = closing.getBytes();

			byte[] body = new byte[partHeaderBytes.length + fileBytes.length + closingBytes.length];
			System.arraycopy(partHeaderBytes, 0, body, 0, partHeaderBytes.length);
			System.arraycopy(fileBytes, 0, body, partHeaderBytes.length, fileBytes.length);
			System.arraycopy(closingBytes, 0, body, partHeaderBytes.length + fileBytes.length, closingBytes.length);

			HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(baseUrl + path))
					.timeout(Duration.ofSeconds(AppConfig.getInstance().getReadTimeoutSeconds()))
					.header("Content-Type", "multipart/form-data; boundary=" + boundary)
					.header("Accept", "application/json").POST(HttpRequest.BodyPublishers.ofByteArray(body));

			if (jwtToken != null && !jwtToken.isBlank()) {
				builder.header("Authorization", "Bearer " + jwtToken);
			}

			return execute(builder.build(), type);

		} catch (IOException e) {
			throw new ApiException("Error leyendo fichero para subir: " + e.getMessage(), e);
		}
	}

	private String guessMimeType(String fileName) {
		String lower = fileName.toLowerCase();
		if (lower.endsWith(".png"))
			return "image/png";
		if (lower.endsWith(".jpg") || lower.endsWith(".jpeg"))
			return "image/jpeg";
		if (lower.endsWith(".gif"))
			return "image/gif";
		if (lower.endsWith(".webp"))
			return "image/webp";
		return "application/octet-stream";
	}

	// ── Internal ──────────────────────────────────────────────────────────

	private <T> T execute(HttpRequest req, TypeReference<T> type) {
		try {
			log.debug("--> {} {}", req.method(), req.uri());
			HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
			logResponse(req, resp);
			checkStatus(resp);
			if (resp.body() == null || resp.body().isBlank())
				return null;
			return MAPPER.readValue(resp.body(), type);
		} catch (ApiException e) {
			throw e;
		} catch (IOException | InterruptedException e) {
			throw wrap(e);
		}
	}

	private void executeVoid(HttpRequest req) {
		try {
			log.debug("--> {} {}", req.method(), req.uri());
			HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
			logResponse(req, resp);
			checkStatus(resp);
		} catch (ApiException e) {
			throw e;
		} catch (IOException | InterruptedException e) {
			throw wrap(e);
		}
	}

	/** Registra la respuesta HTTP en DEBUG. Nunca expone headers (contienen el JWT). */
	private void logResponse(HttpRequest req, HttpResponse<String> resp) {
		if (!log.isDebugEnabled())
			return;
		String body = resp.body();
		int len = body != null ? body.length() : 0;
		log.debug("<-- {} {} ({} chars)", resp.statusCode(), req.uri(), len);
		if (body != null && !body.isBlank() && resp.statusCode() >= 200 && resp.statusCode() < 300) {
			String preview = len > 500 ? body.substring(0, 500) + "…" : body;
			log.debug("    {}", preview);
		}
	}

	private void checkStatus(HttpResponse<String> resp) {
		int code = resp.statusCode();
		if (code >= 200 && code < 300)
			return;
		String body = resp.body() != null ? resp.body() : "";
		String msg = extractMessage(body, code);
		log.warn("HTTP {} en {}: {}", code, resp.uri(), msg);
		throw new ApiException(code, msg);
	}

	private String extractMessage(String body, int code) {
		if (body.isBlank())
			return "Error HTTP " + code;
		try {
			Map<?, ?> map = MAPPER.readValue(body, Map.class);
			if (map.containsKey("message"))
				return map.get("message").toString();
			if (map.containsKey("error"))
				return map.get("error").toString();
			if (map.containsKey("detail")) {
				Object detail = map.get("detail");
				if (detail instanceof String s)
					return s;
				if (detail instanceof List<?> list)
					return formatValidationErrors(list);
				return detail.toString();
			}
		} catch (Exception ignored) {
		}
		return body.length() > 200 ? body.substring(0, 200) : body;
	}

	/** Convierte la lista de errores 422 de FastAPI a un mensaje legible. */
	private static String formatValidationErrors(List<?> errors) {
		StringBuilder sb = new StringBuilder();
		for (Object err : errors) {
			if (err instanceof Map<?, ?> m) {
				Object msg = m.get("msg");
				Object loc = m.get("loc");
				if (msg != null) {
					if (loc instanceof List<?> locList && !locList.isEmpty())
						sb.append(locList.get(locList.size() - 1)).append(": ");
					sb.append(msg).append("; ");
				}
			}
		}
		String result = sb.toString().stripTrailing();
		if (result.endsWith(";"))
			result = result.substring(0, result.length() - 1).stripTrailing();
		return result.isEmpty() ? "Error de validación" : result;
	}

	private String toJson(Object obj) {
		try {
			return MAPPER.writeValueAsString(obj);
		} catch (Exception e) {
			throw new ApiException("Error serializando petición: " + e.getMessage(), e);
		}
	}

	// ── SSL helpers ───────────────────────────────────────────────────────

	/** Clasifica y envuelve excepciones de red con mensajes descriptivos. */
	private static ApiException wrap(Exception e) {
		if (e instanceof InterruptedException) {
			Thread.currentThread().interrupt();
		}
		if (isSSLError(e)) {
			log.error("Error SSL al conectar: {}", e.getMessage());
			return new ApiException(-1,
					"Error SSL: no se puede verificar el certificado. "
					+ "Activa api.ssl.trust-all=true en config.properties si es un entorno de desarrollo.");
		}
		if (e instanceof ConnectException) {
			return new ApiException(-1,
					"No se puede conectar con el servidor. Comprueba que la API esté activa y la URL en config.properties.");
		}
		log.warn("Error de red inesperado: {}", e.getMessage(), e);
		return new ApiException("Error de comunicación con la API: " + e.getMessage(), e);
	}

	private static boolean isSSLError(Throwable t) {
		while (t != null) {
			if (t instanceof SSLException)
				return true;
			t = t.getCause();
		}
		return false;
	}

	/** SSLContext que acepta cualquier certificado. Solo para desarrollo/pruebas. */
	private static SSLContext buildTrustAllSslContext() {
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, new TrustManager[] {
				new X509TrustManager() {
					public void checkClientTrusted(X509Certificate[] c, String a) {}
					public void checkServerTrusted(X509Certificate[] c, String a) {}
					public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
				}
			}, new SecureRandom());
			return ctx;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException("No se pudo crear SSLContext trust-all", e);
		}
	}

	/** SSLParameters que deshabilita la verificación de hostname. */
	private static SSLParameters buildTrustAllSslParams() {
		SSLParameters params = new SSLParameters();
		params.setEndpointIdentificationAlgorithm(""); // deshabilita verificación de hostname
		return params;
	}
}
