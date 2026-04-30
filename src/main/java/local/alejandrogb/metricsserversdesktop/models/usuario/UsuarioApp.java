package local.alejandrogb.metricsserversdesktop.models.usuario;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsuarioApp {

	private int id;
	private String adObjectId;
	private String username;
	private String fotoPerfil;

	public UsuarioApp() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAdObjectId() {
		return adObjectId;
	}

	public void setAdObjectId(String adObjectId) {
		this.adObjectId = adObjectId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFotoPerfil() {
		return fotoPerfil;
	}

	public void setFotoPerfil(String fotoPerfil) {
		this.fotoPerfil = fotoPerfil;
	}
}
