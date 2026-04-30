package local.alejandrogb.metricsserversdesktop.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Permiso {

	private int id;
	private String nombre;
	private String descripcion;
	private Ambito ambito;

	public Permiso() {
	}

	public Permiso(int id, String nombre, String descripcion, Ambito ambito) {
		this.id = id;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.ambito = ambito;
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

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Ambito getAmbito() {
		return ambito;
	}

	public void setAmbito(Ambito ambito) {
		this.ambito = ambito;
	}

	@Override
	public String toString() {
		return nombre != null ? nombre : "Permiso #" + id;
	}
}
