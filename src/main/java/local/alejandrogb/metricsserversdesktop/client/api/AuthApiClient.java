package local.alejandrogb.metricsserversdesktop.client.api;

import com.fasterxml.jackson.core.type.TypeReference;

import local.alejandrogb.metricsserversdesktop.models.login.LoginRequest;
import local.alejandrogb.metricsserversdesktop.models.login.LoginResponse;

public class AuthApiClient extends ApiClient {

	public LoginResponse login(String username, String password) {
		LoginRequest req = new LoginRequest(username, password);
		return post("/auth/login", req, new TypeReference<LoginResponse>() {
		});
	}
}
