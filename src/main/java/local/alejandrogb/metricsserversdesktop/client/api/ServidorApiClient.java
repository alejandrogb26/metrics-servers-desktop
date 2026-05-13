package local.alejandrogb.metricsserversdesktop.client.api;

import com.fasterxml.jackson.core.type.TypeReference;

import local.alejandrogb.metricsserversdesktop.models.BulkResult;
import local.alejandrogb.metricsserversdesktop.models.PageResponse;
import local.alejandrogb.metricsserversdesktop.models.servidor.Servidor;
import local.alejandrogb.metricsserversdesktop.models.servidor.ServidorDTO;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ServidorApiClient extends ApiClient {

	public List<Servidor> findAll() {
		return getDataPage("/servidor", 100, new TypeReference<List<Servidor>>() {
		});
	}

	public PageResponse<Servidor> findPage(int page, int size) {
		return getPage("/servidor", page, size, new TypeReference<List<Servidor>>() {
		});
	}

	public Servidor findById(int id) {
		return get("/servidor/" + id, new TypeReference<Servidor>() {
		});
	}

	/** Crea uno o varios servidores en lote. Equivale a POST /servidor/bulk */
	public BulkResult createBulk(List<ServidorDTO> servidores) {
		return post("/servidor/bulk", servidores, new TypeReference<BulkResult>() {
		});
	}

	/** Actualiza parcialmente un servidor (PATCH). */
	public void patch(int id, Map<String, Object> fields) {
		patch("/servidor/" + id, fields, new TypeReference<Void>() {
		});
	}

	/** Elimina un único servidor. */
	public void delete(int id) {
		delete("/servidor/" + id);
	}

	/** Elimina varios servidores en lote. api-py: POST /servidor/bulk-delete */
	public BulkResult deleteBulk(List<Integer> ids) {
		return post("/servidor/bulk-delete", ids, new TypeReference<BulkResult>() {
		});
	}

	/** Añade servicios a un servidor. */
	public Map<String, Object> addServicios(int servidorId, List<Integer> servicioIds) {
		return post("/servidor/" + servidorId + "/servicios", servicioIds, new TypeReference<Map<String, Object>>() {
		});
	}

	/** Elimina servicios de un servidor. api-py: DELETE /{id}/servicios?ids=1&ids=2 */
	public void removeServicios(int servidorId, List<Integer> servicioIds) {
		if (servicioIds == null || servicioIds.isEmpty())
			return;
		StringBuilder url = new StringBuilder("/servidor/").append(servidorId).append("/servicios?ids=")
				.append(servicioIds.get(0));
		for (int i = 1; i < servicioIds.size(); i++) {
			url.append("&ids=").append(servicioIds.get(i));
		}
		delete(url.toString());
	}

	/**
	 * Sube la foto de un servidor. Equivale a POST /servidor/{id}/foto.
	 *
	 * @param id   ID interno del servidor
	 * @param file fichero de imagen local
	 * @return mapa con {@code nombreArchivo} devuelto por la API
	 */
	public Map<String, Object> subirFoto(int id, Path file) {
		return postMultipart("/servidor/" + id + "/foto", file, new TypeReference<Map<String, Object>>() {
		});
	}
}
