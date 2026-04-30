package local.alejandrogb.metricsserversdesktop.client.api;

import com.fasterxml.jackson.core.type.TypeReference;

import local.alejandrogb.metricsserversdesktop.models.Permiso;

import java.util.List;

public class PermisoApiClient extends ApiClient {

	public List<Permiso> findAll() {
		return get("/permisos", new TypeReference<List<Permiso>>() {
		});
	}

	public Permiso findById(int id) {
		return get("/permisos/" + id, new TypeReference<Permiso>() {
		});
	}
}
