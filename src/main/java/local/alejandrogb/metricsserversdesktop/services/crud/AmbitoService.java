package local.alejandrogb.metricsserversdesktop.services.crud;

import java.util.List;

import local.alejandrogb.metricsserversdesktop.client.api.AmbitoApiClient;
import local.alejandrogb.metricsserversdesktop.models.Ambito;

public class AmbitoService {

	private final AmbitoApiClient api = new AmbitoApiClient();

	public List<Ambito> findAll() {
		return api.findAll();
	}

	public Ambito findById(int id) {
		return api.findById(id);
	}
}
