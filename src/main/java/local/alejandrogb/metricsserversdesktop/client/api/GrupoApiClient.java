package local.alejandrogb.metricsserversdesktop.client.api;

import com.fasterxml.jackson.core.type.TypeReference;

import local.alejandrogb.metricsserversdesktop.client.exception.ApiException;
import local.alejandrogb.metricsserversdesktop.models.BulkResult;
import local.alejandrogb.metricsserversdesktop.models.Grupo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GrupoApiClient extends ApiClient {

	public List<Grupo> findAll() {
		return getDataPage("/grupos", 100, new TypeReference<List<Grupo>>() {
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

	/**
	 * Crea un grupo incluyendo permisos iniciales en una sola llamada.
	 * Construye el body como Map para enviar globalPerms e sections como enteros,
	 * que es lo que espera GrupoCreate[int] en api-py.
	 */
	public BulkResult createWithPermisos(Grupo grupo, List<Integer> globalIds,
			Map<Integer, List<Integer>> sectionIds) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("nombre", grupo.getNombre());
		if (grupo.getDn() != null && !grupo.getDn().isBlank()) {
			body.put("dn", grupo.getDn());
		}
		body.put("superadmin", Boolean.TRUE.equals(grupo.isSuperAdmin()));

		if (!globalIds.isEmpty() || !sectionIds.isEmpty()) {
			Map<String, Object> permisos = new LinkedHashMap<>();
			if (!globalIds.isEmpty()) {
				permisos.put("globalPerms", globalIds);
			}
			if (!sectionIds.isEmpty()) {
				Map<String, Object> sections = new LinkedHashMap<>();
				for (Map.Entry<Integer, List<Integer>> e : sectionIds.entrySet()) {
					sections.put(String.valueOf(e.getKey()), e.getValue());
				}
				permisos.put("sections", sections);
			}
			body.put("permisos", permisos);
		}

		return post("/grupos", List.of(body), new TypeReference<BulkResult>() {
		});
	}

	/** PATCH /grupos/{id} — solo acepta {"nombre": "..."}. */
	public Grupo patch(int id, String nombre) {
		return patch("/grupos/" + id, Map.of("nombre", nombre), new TypeReference<Grupo>() {
		});
	}

	/** PATCH /grupos/{id}/superadmin — endpoint específico para cambiar el flag. */
	public void patchSuperAdmin(int id, boolean superAdmin) {
		patch("/grupos/" + id + "/superadmin", Map.of("superadmin", superAdmin), new TypeReference<Grupo>() {
		});
	}

	/** Modifica permisos globales del grupo de forma incremental. PATCH /grupos/{id}/permisos/global */
	public Grupo patchGlobalPermisos(int grupoId, List<Integer> add, List<Integer> remove) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("add", add != null ? add : List.of());
		body.put("remove", remove != null ? remove : List.of());
		return patch("/grupos/" + grupoId + "/permisos/global", body, new TypeReference<Grupo>() {
		});
	}

	/** Modifica permisos de una sección del grupo de forma incremental. PATCH /grupos/{id}/permisos/secciones/{seccionId} */
	public Grupo patchSeccionPermisos(int grupoId, int seccionId, List<Integer> add, List<Integer> remove) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("add", add != null ? add : List.of());
		body.put("remove", remove != null ? remove : List.of());
		return patch("/grupos/" + grupoId + "/permisos/secciones/" + seccionId, body, new TypeReference<Grupo>() {
		});
	}

	/** api-py solo tiene DELETE /grupos/{id}. Llamadas individuales, resultado sintetizado. */
	public BulkResult delete(List<Integer> ids) {
		BulkResult result = new BulkResult();
		result.setTotal(ids.size());
		List<String> errors = new ArrayList<>();
		int ok = 0;
		for (int id : ids) {
			try {
				delete("/grupos/" + id);
				ok++;
			} catch (ApiException e) {
				errors.add("Grupo #" + id + ": " + e.getMessage());
			}
		}
		result.setOk(ok);
		result.setFailed(ids.size() - ok);
		result.setErrors(errors);
		return result;
	}
}
