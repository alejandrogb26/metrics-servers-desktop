package local.alejandrogb.metricsserversdesktop.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Grupo {

	private int id;
	private String nombre;
	private String dn;
	// api-py devuelve "superadmin" (todo minúsculas)
	@JsonProperty("superadmin")
	private Boolean superAdmin;
	private PermissionMap permisos;

	public Grupo() {
	}

	public Grupo(int id, String nombre, String dn, Boolean superAdmin) {
		this.id = id;
		this.nombre = nombre;
		this.dn = dn;
		this.superAdmin = superAdmin;
	}

	public Grupo(int id, String nombre, String dn, Boolean superAdmin, PermissionMap permisos) {
		this(id, nombre, dn, superAdmin);
		this.permisos = permisos;
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

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public Boolean isSuperAdmin() {
		return superAdmin;
	}

	public void setSuperAdmin(Boolean superAdmin) {
		this.superAdmin = superAdmin;
	}

	public PermissionMap getPermisos() {
		return permisos;
	}

	public void setPermisos(PermissionMap permisos) {
		this.permisos = permisos;
	}

	@Override
	public String toString() {
		return nombre != null ? nombre : "Grupo #" + id;
	}
}
