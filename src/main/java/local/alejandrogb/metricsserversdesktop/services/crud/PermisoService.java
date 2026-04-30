package local.alejandrogb.metricsserversdesktop.services.crud;

import java.util.List;

import local.alejandrogb.metricsserversdesktop.client.api.PermisoApiClient;
import local.alejandrogb.metricsserversdesktop.models.Permiso;

public class PermisoService {

	private final PermisoApiClient api = new PermisoApiClient();

	public List<Permiso> findAll() {
		return api.findAll();
	}

	public Permiso findById(int id) {
		return api.findById(id);
	}
}
