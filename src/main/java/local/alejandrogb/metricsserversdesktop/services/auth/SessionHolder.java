package local.alejandrogb.metricsserversdesktop.services.auth;

import local.alejandrogb.metricsserversdesktop.client.api.ApiClient;
import local.alejandrogb.metricsserversdesktop.models.Session;

/**
 * Singleton que almacena la sesión del usuario autenticado. Se inicializa tras
 * un login correcto y se limpia al cerrar sesión.
 */
public class SessionHolder {

	private static final SessionHolder INSTANCE = new SessionHolder();

	private Session session;

	private SessionHolder() {
	}

	public static SessionHolder getInstance() {
		return INSTANCE;
	}

	public void setSession(Session session, String token) {
		this.session = session;
		ApiClient.setToken(token);
	}

	public void clear() {
		this.session = null;
		ApiClient.clearToken();
	}

	public Session getSession() {
		return session;
	}

	public boolean isLoggedIn() {
		return session != null && ApiClient.getToken() != null;
	}

	public String getUsername() {
		return session != null ? session.getUsername() : null;
	}

	public String getDisplayName() {
		return session != null ? session.getDisplayName() : null;
	}
}
