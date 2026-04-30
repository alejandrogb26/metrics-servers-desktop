package local.alejandrogb.metricsserversdesktop.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Session {

	private String username;
	private String displayName;
	private String email;
	private Grupo grupo;
	private PermissionMap<String> permisos;
	private String urlFoto;

	public Session() {
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Grupo getGrupo() {
		return grupo;
	}

	public void setGrupo(Grupo grupo) {
		this.grupo = grupo;
	}

	public PermissionMap<String> getPermisos() {
		return permisos;
	}

	public void setPermisos(PermissionMap<String> permisos) {
		this.permisos = permisos;
	}

	public String getUrlFoto() {
		return urlFoto;
	}

	public void setUrlFoto(String urlFoto) {
		this.urlFoto = urlFoto;
	}

	@Override
	public String toString() {
		return "Session{username='" + username + "', displayName='" + displayName + "', grupo=" + grupo + '}';
	}
}
