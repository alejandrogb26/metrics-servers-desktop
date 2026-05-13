package local.alejandrogb.metricsserversdesktop.services.crud;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import local.alejandrogb.metricsserversdesktop.client.api.ServicioApiClient;
import local.alejandrogb.metricsserversdesktop.models.Servicio;

public class ServicioService {

	private final ServicioApiClient api = new ServicioApiClient();

	public List<Servicio> findAll() {
		return api.findAll();
	}

	public Servicio findById(int id) {
		return api.findById(id);
	}

	public int create(Servicio servicio) {
		Map<String, Object> resp = api.create(servicio);
		if (resp != null && resp.containsKey("id")) {
			Object id = resp.get("id");
			return id instanceof Number n ? n.intValue() : Integer.parseInt(id.toString());
		}
		return -1;
	}

	public void update(Servicio servicio) {
		Map<String, Object> fields = new LinkedHashMap<>();
		if (servicio.getNombre() != null)
			fields.put("nombre", servicio.getNombre());
		if (!fields.isEmpty()) {
			api.patch(servicio.getId(), fields);
		}
	}

	/** Sube o reemplaza el logo de un servicio. POST /servicio/{id}/logo. */
	public Map<String, Object> subirLogo(int id, Path file) {
		return api.subirLogo(id, file);
	}

	public void delete(int id) {
		api.delete(id);
	}
}
