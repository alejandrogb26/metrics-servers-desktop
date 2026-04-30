package local.alejandrogb.metricsserversdesktop.services.crud;

import java.util.List;

import local.alejandrogb.metricsserversdesktop.client.api.GrupoApiClient;
import local.alejandrogb.metricsserversdesktop.models.BulkResult;
import local.alejandrogb.metricsserversdesktop.models.Grupo;

public class GrupoService {

	private final GrupoApiClient api = new GrupoApiClient();

	public List<Grupo> findAll() {
		return api.findAll();
	}

	public Grupo findById(int id) {
		return api.findById(id);
	}

	public BulkResult create(Grupo grupo) {
		return api.create(List.of(grupo));
	}

	public Grupo update(int id, Grupo grupo) {
		return api.patch(id, grupo);
	}

	public BulkResult delete(int id) {
		return api.delete(List.of(id));
	}

	public BulkResult deleteBulk(List<Integer> ids) {
		return api.delete(ids);
	}
}
