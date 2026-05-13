package local.alejandrogb.metricsserversdesktop.services.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import local.alejandrogb.metricsserversdesktop.client.api.AuthApiClient;
import local.alejandrogb.metricsserversdesktop.models.login.LoginResponse;

public class AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthService.class);

	private final AuthApiClient apiClient = new AuthApiClient();
	private final SessionHolder sessionHolder = SessionHolder.getInstance();

	/**
	 * Realiza el login contra la API y, si es correcto, guarda la sesión.
	 *
	 * @param username sAMAccountName del usuario AD
	 * @param password contraseña
	 * @return LoginResponse con el token y la sesión
	 * @throws local.alejandrogb.metricsserversdesktop.client.exception.ApiException si las
	 *                                                                    credenciales
	 *                                                                    son
	 *                                                                    incorrectas
	 *                                                                    o la API
	 *                                                                    no
	 *                                                                    responde
	 */
	public LoginResponse login(String username, String password) {
		log.info("Intentando login para el usuario '{}'", username);
		LoginResponse resp = apiClient.login(username, password);
		sessionHolder.setSession(resp.getSession(), resp.getToken());
		log.info("Login correcto para '{}' (grupo: {})", username,
				resp.getSession().getGrupo() != null ? resp.getSession().getGrupo().getNombre() : "—");
		return resp;
	}

	public void logout() {
		log.info("Cerrando sesión para '{}'", sessionHolder.getUsername());
		try {
			apiClient.logout();
			log.debug("POST /auth/logout OK — JTI revocado en Redis");
		} catch (Exception e) {
			log.warn("No se pudo revocar el token en el backend (logout local de todas formas): {}", e.getMessage());
		}
		sessionHolder.clear();
	}

}
