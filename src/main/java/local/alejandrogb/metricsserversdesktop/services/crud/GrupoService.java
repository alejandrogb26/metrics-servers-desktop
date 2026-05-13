package local.alejandrogb.metricsserversdesktop.services.crud;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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

	/** Crea un grupo con permisos iniciales en una sola llamada a POST /grupos. */
	public BulkResult createWithPermisos(Grupo grupo, List<Integer> globalIds,
			Map<Integer, List<Integer>> sectionIds) {
		if (globalIds.isEmpty() && sectionIds.isEmpty()) {
			return api.create(List.of(grupo));
		}
		return api.createWithPermisos(grupo, globalIds, sectionIds);
	}

	/**
	 * Actualiza solo los campos que cambiaron respecto al original.
	 * <ul>
	 * <li>{@code nombre} → PATCH /grupos/{id}</li>
	 * <li>{@code superadmin} → PATCH /grupos/{id}/superadmin</li>
	 * <li>{@code dn} → no soportado por api-py; se ignora silenciosamente.</li>
	 * </ul>
	 */
	public void update(int id, Grupo original, Grupo updated) {
		if (!Objects.equals(original.getNombre(), updated.getNombre())) {
			api.patch(id, updated.getNombre());
		}
		if (!Objects.equals(original.isSuperAdmin(), updated.isSuperAdmin())) {
			api.patchSuperAdmin(id, Boolean.TRUE.equals(updated.isSuperAdmin()));
		}
	}

	public void updateGlobalPermisos(int grupoId, List<Integer> add, List<Integer> remove) {
		api.patchGlobalPermisos(grupoId, add, remove);
	}

	public void updateSeccionPermisos(int grupoId, int seccionId, List<Integer> add, List<Integer> remove) {
		api.patchSeccionPermisos(grupoId, seccionId, add, remove);
	}

	public BulkResult delete(int id) {
		return api.delete(List.of(id));
	}

	public BulkResult deleteBulk(List<Integer> ids) {
		return api.delete(ids);
	}
}
