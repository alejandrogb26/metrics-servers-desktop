package local.alejandrogb.metricsserversdesktop.services.crud;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import local.alejandrogb.metricsserversdesktop.client.api.ServidorApiClient;
import local.alejandrogb.metricsserversdesktop.models.BulkResult;
import local.alejandrogb.metricsserversdesktop.models.PageResponse;
import local.alejandrogb.metricsserversdesktop.models.servidor.Servidor;
import local.alejandrogb.metricsserversdesktop.models.servidor.ServidorDTO;

public class ServidorService {

	private final ServidorApiClient api = new ServidorApiClient();

	public List<Servidor> findAll() {
		return api.findAll();
	}

	public PageResponse<Servidor> findPage(int page, int size) {
		return api.findPage(page, size);
	}

	public Servidor findById(int id) {
		return api.findById(id);
	}

	/** Crea un servidor a través del endpoint bulk con un único elemento. */
	public BulkResult create(ServidorDTO dto) {
		return api.createBulk(List.of(dto));
	}

	/** Creación masiva desde JSON. */
	public BulkResult createBulk(List<ServidorDTO> dtos) {
		return api.createBulk(dtos);
	}

	/**
	 * Actualiza los campos editables de un servidor mediante PATCH. hostname,
	 * prettyOs, arch y kernel los gestiona la API automáticamente y no se envían.
	 */
	public void update(Servidor servidor) {
		Map<String, Object> fields = new LinkedHashMap<>();
		if (servidor.getServerId() != null)
			fields.put("serverId", servidor.getServerId());
		if (servidor.getDns() != null)
			fields.put("dns", servidor.getDns());
		if (servidor.getSeccion() != 0)
			fields.put("seccionId", servidor.getSeccion());
		if (!fields.isEmpty()) {
			api.patch(servidor.getId(), fields);
		}
	}

	public void delete(int id) {
		api.delete(id);
	}

	public BulkResult deleteBulk(List<Integer> ids) {
		return api.deleteBulk(ids);
	}

	/**
	 * Sube la imagen de un servidor. Delega en POST /servidor/{id}/foto.
	 *
	 * @param id   ID interno del servidor
	 * @param file fichero de imagen local
	 * @return mapa con {@code nombreArchivo} devuelto por la API
	 */
	public java.util.Map<String, Object> subirFoto(int id, java.nio.file.Path file) {
		return api.subirFoto(id, file);
	}

	public void setServicios(int servidorId, List<Integer> nuevosIds, List<Integer> quitarIds) {
		if (nuevosIds != null && !nuevosIds.isEmpty()) {
			api.addServicios(servidorId, nuevosIds);
		}
		if (quitarIds != null && !quitarIds.isEmpty()) {
			api.removeServicios(servidorId, quitarIds);
		}
	}
}
