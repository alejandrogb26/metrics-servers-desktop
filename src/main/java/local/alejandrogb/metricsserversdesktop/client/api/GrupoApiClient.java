package local.alejandrogb.metricsserversdesktop.client.api;

import com.fasterxml.jackson.core.type.TypeReference;

import local.alejandrogb.metricsserversdesktop.models.BulkResult;
import local.alejandrogb.metricsserversdesktop.models.Grupo;

import java.util.List;

public class GrupoApiClient extends ApiClient {

	public List<Grupo> findAll() {
		return get("/grupos", new TypeReference<List<Grupo>>() {
		});
	}

	public Grupo findById(int id) {
		return get("/grupos/" + id, new TypeReference<Grupo>() {
		});
	}

	/** Crea uno o varios grupos. La API acepta lista. */
	public BulkResult create(List<Grupo> grupos) {
		return post("/grupos", grupos, new TypeReference<BulkResult>() {
		});
	}

	public Grupo patch(int id, Grupo grupo) {
		return patch("/grupos/" + id, grupo, new TypeReference<Grupo>() {
		});
	}

	public BulkResult delete(List<Integer> ids) {
		return deleteWithBody("/grupos", ids, new TypeReference<BulkResult>() {
		});
	}
}
