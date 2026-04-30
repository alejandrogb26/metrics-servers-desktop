package local.alejandrogb.metricsserversdesktop.models.login;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import local.alejandrogb.metricsserversdesktop.models.Session;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {

	private String token;
	private String tokenType;
	private long expiresIn;
	private Session session;

	public LoginResponse() {
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(long expiresIn) {
		this.expiresIn = expiresIn;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
