package local.alejandrogb.metricsserversdesktop.client.api;

import com.fasterxml.jackson.core.type.TypeReference;

import java.nio.file.Path;
import java.util.Map;

public class UsuarioApiClient extends ApiClient {

	/**
	 * Sube la foto de perfil del usuario autenticado. Equivale a POST /usuario/foto
	 * — la API identifica al usuario por el JWT.
	 *
	 * @param file fichero de imagen local
	 * @return mapa con {@code nombreArchivo} devuelto por la API
	 */
	public Map<String, Object> subirFoto(Path file) {
		return postMultipart("/usuario/foto", file, new TypeReference<Map<String, Object>>() {
		});
	}
}
