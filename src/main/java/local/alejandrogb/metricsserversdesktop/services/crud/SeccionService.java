package local.alejandrogb.metricsserversdesktop.services.crud;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import local.alejandrogb.metricsserversdesktop.client.api.SeccionApiClient;
import local.alejandrogb.metricsserversdesktop.models.Seccion;

public class SeccionService {

	private final SeccionApiClient api = new SeccionApiClient();

	public List<Seccion> findAll() {
		return api.findAll();
	}

	public Seccion findById(int id) {
		return api.findById(id);
	}

	public int create(Seccion seccion) {
		Map<String, Object> resp = api.create(seccion);
		if (resp != null && resp.containsKey("id")) {
			Object id = resp.get("id");
			return id instanceof Number n ? n.intValue() : Integer.parseInt(id.toString());
		}
		return -1;
	}

	public void update(Seccion seccion) {
		Map<String, Object> fields = new LinkedHashMap<>();
		if (seccion.getNombre() != null)
			fields.put("nombre", seccion.getNombre());
		if (seccion.getDescripcion() != null)
			fields.put("descripcion", seccion.getDescripcion());
		if (!fields.isEmpty()) {
			api.patch(seccion.getId(), fields);
		}
	}

	public void delete(int id) {
		api.delete(id);
	}
}
