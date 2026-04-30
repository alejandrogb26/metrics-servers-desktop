package local.alejandrogb.metricsserversdesktop.models.servidor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Servidor {

	private int id;
	private String serverId;
	private String dns;
	private String hostname;
	private String prettyOs;
	private String arch;
	private String kernel;
	private String imagen;
	private String imagenUrl;
	private int seccion;
	private List<Integer> servicios;

	public Servidor() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getDns() {
		return dns;
	}

	public void setDns(String dns) {
		this.dns = dns;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getPrettyOs() {
		return prettyOs;
	}

	public void setPrettyOs(String prettyOs) {
		this.prettyOs = prettyOs;
	}

	public String getArch() {
		return arch;
	}

	public void setArch(String arch) {
		this.arch = arch;
	}

	public String getKernel() {
		return kernel;
	}

	public void setKernel(String kernel) {
		this.kernel = kernel;
	}

	public String getImagen() {
		return imagen;
	}

	public void setImagen(String imagen) {
		this.imagen = imagen;
	}

	public String getImagenUrl() {
		return imagenUrl;
	}

	public void setImagenUrl(String imagenUrl) {
		this.imagenUrl = imagenUrl;
	}

	public int getSeccion() {
		return seccion;
	}

	public void setSeccion(int seccion) {
		this.seccion = seccion;
	}

	public List<Integer> getServicios() {
		return servicios;
	}

	public void setServicios(List<Integer> servicios) {
		this.servicios = servicios;
	}

	@Override
	public String toString() {
		return serverId != null ? serverId : "Servidor #" + id;
	}
}
