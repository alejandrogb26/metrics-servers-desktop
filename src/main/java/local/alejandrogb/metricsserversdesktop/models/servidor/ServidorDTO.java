package local.alejandrogb.metricsserversdesktop.models.servidor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServidorDTO {

	private String serverId;
	private String dns;
	private int seccion;
	private List<Integer> servicios;

	public ServidorDTO() {
	}

	public ServidorDTO(String serverId, String dns, int seccion, List<Integer> servicios) {
		this.serverId = serverId;
		this.dns = dns;
		this.seccion = seccion;
		this.servicios = servicios;
	}

	public static ServidorDTO fromServidor(Servidor s) {
		ServidorDTO dto = new ServidorDTO();
		dto.setServerId(s.getServerId());
		dto.setDns(s.getDns());
		dto.setSeccion(s.getSeccion());
		dto.setServicios(s.getServicios());
		return dto;
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
}
