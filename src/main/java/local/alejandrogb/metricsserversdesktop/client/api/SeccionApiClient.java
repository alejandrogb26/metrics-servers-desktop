package local.alejandrogb.metricsserversdesktop.client.api;

import com.fasterxml.jackson.core.type.TypeReference;

import local.alejandrogb.metricsserversdesktop.models.Seccion;

import java.util.List;
import java.util.Map;

public class SeccionApiClient extends ApiClient {

	public List<Seccion> findAll() {
		return get("/seccion", new TypeReference<List<Seccion>>() {
		});
	}

	public Seccion findById(int id) {
		return get("/seccion/" + id, new TypeReference<Seccion>() {
		});
	}

	public Map<String, Object> create(Seccion seccion) {
		return post("/seccion", seccion, new TypeReference<Map<String, Object>>() {
		});
	}

	public void patch(int id, Map<String, Object> fields) {
		patch("/seccion/" + id, fields, new TypeReference<Void>() {
		});
	}

	public void delete(int id) {
		delete("/seccion/" + id);
	}
}
