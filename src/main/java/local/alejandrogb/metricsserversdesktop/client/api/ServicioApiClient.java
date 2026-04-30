package local.alejandrogb.metricsserversdesktop.client.api;

import com.fasterxml.jackson.core.type.TypeReference;

import local.alejandrogb.metricsserversdesktop.models.Servicio;

import java.util.List;
import java.util.Map;

public class ServicioApiClient extends ApiClient {

	public List<Servicio> findAll() {
		return get("/servicio", new TypeReference<List<Servicio>>() {
		});
	}

	public Servicio findById(int id) {
		return get("/servicio/" + id, new TypeReference<Servicio>() {
		});
	}

	public Map<String, Object> create(Servicio servicio) {
		return post("/servicio", servicio, new TypeReference<Map<String, Object>>() {
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
