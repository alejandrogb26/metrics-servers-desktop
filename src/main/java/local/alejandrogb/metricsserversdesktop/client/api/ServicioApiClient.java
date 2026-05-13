package local.alejandrogb.metricsserversdesktop.client.api;

import com.fasterxml.jackson.core.type.TypeReference;

import local.alejandrogb.metricsserversdesktop.models.Servicio;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ServicioApiClient extends ApiClient {

	public List<Servicio> findAll() {
		return getDataPage("/servicio", 100, new TypeReference<List<Servicio>>() {
		});
	}

	public Servicio findById(int id) {
		return get("/servicio/" + id, new TypeReference<Servicio>() {
		});
	}

	/** api-py: solo acepta {"nombre": "..."} — extra="forbid". */
	public Map<String, Object> create(Servicio servicio) {
		return post("/servicio", Map.of("nombre", servicio.getNombre()), new TypeReference<Map<String, Object>>() {
		});
	}

	/** Sube el logo de un servicio. POST /servicio/{id}/logo multipart. */
	public Map<String, Object> subirLogo(int id, Path file) {
		return postMultipart("/servicio/" + id + "/logo", file, new TypeReference<Map<String, Object>>() {
		});
	}

	public void patch(int id, Map<String, Object> fields) {
		patch("/servicio/" + id, fields, new TypeReference<Void>() {
		});
	}

	public void delete(int id) {
		delete("/servicio/" + id);
	}
}
