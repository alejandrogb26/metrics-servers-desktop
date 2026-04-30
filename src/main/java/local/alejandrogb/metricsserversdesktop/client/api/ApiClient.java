package local.alejandrogb.metricsserversdesktop.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import local.alejandrogb.metricsserversdesktop.client.config.AppConfig;
import local.alejandrogb.metricsserversdesktop.client.exception.ApiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
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

	public ApiClient() {
		AppConfig cfg = AppConfig.getInstance();
		this.baseUrl = cfg.getApiBaseUrl();
		this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(cfg.getConnectTimeoutSeconds())).build();
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
			log.debug("{} {}", req.method(), req.uri());
			HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
			checkStatus(resp);
			if (resp.body() == null || resp.body().isBlank())
				return null;
			return MAPPER.readValue(resp.body(), type);
		} catch (ApiException e) {
			throw e;
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ApiException("Error de comunicación con la API: " + e.getMessage(), e);
		}
	}

	private void executeVoid(HttpRequest req) {
		try {
			log.debug("{} {}", req.method(), req.uri());
			HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
			checkStatus(resp);
		} catch (ApiException e) {
			throw e;
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ApiException("Error de comunicación con la API: " + e.getMessage(), e);
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
		} catch (Exception ignored) {
		}
		return body.length() > 200 ? body.substring(0, 200) : body;
	}

	private String toJson(Object obj) {
		try {
			return MAPPER.writeValueAsString(obj);
		} catch (Exception e) {
			throw new ApiException("Error serializando petición: " + e.getMessage(), e);
		}
	}
}
