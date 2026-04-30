package local.alejandrogb.metricsserversdesktop.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Servicio {

	private int id;
	private String nombre;
	private String logo;
	private String urlLogo;

	public Servicio() {
	}

	public Servicio(int id, String nombre, String logo) {
		this.id = id;
		this.nombre = nombre;
		this.logo = logo;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getUrlLogo() {
		return urlLogo;
	}

	public void setUrlLogo(String urlLogo) {
		this.urlLogo = urlLogo;
	}

	@Override
	public String toString() {
		return nombre != null ? nombre : "Servicio #" + id;
	}
}
