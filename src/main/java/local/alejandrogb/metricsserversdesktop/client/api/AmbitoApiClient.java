package local.alejandrogb.metricsserversdesktop.client.api;

import com.fasterxml.jackson.core.type.TypeReference;

import local.alejandrogb.metricsserversdesktop.models.Ambito;

import java.util.List;

public class AmbitoApiClient extends ApiClient {

	public List<Ambito> findAll() {
		return get("/ambitos", new TypeReference<List<Ambito>>() {
		});
	}

	public Ambito findById(int id) {
		return get("/ambitos/" + id, new TypeReference<Ambito>() {
		});
	}
}
