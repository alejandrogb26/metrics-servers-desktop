package local.alejandrogb.metricsserversdesktop.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Mapa de permisos devuelto por la API en el campo {@code permisos} de la sesión y de cada grupo.
 * <p>
 * La API devuelve nombres compuestos (e.g. {@code "AUDIT_SERV"}) en lugar de IDs enteros,
 * por lo que todos los valores son {@code String}.  Las claves de {@code sections} son IDs
 * de sección; Jackson los deserializa desde las claves de cadena del JSON ({@code "2"} → {@code 2}).
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PermissionMap {

    /** Permisos globales: lista de nombres compuestos como {@code "MODIFY_SERV"}. */
    @JsonProperty("globalPerms")
    private List<String> global;

    /** Permisos por sección: seccionId → lista de nombres compuestos. */
    private Map<Integer, List<String>> sections;

    public PermissionMap() {
    }

    public PermissionMap(List<String> global, Map<Integer, List<String>> sections) {
        this.global = global;
        this.sections = sections;
    }

    public List<String> getGlobal() {
        return global;
    }

    public void setGlobal(List<String> global) {
        this.global = global;
    }

    public Map<Integer, List<String>> getSections() {
        return sections;
    }

    public void setSections(Map<Integer, List<String>> sections) {
        this.sections = sections;
    }

    @Override
    public String toString() {
        return "PermissionMap{global=" + global + ", sections=" + sections + '}';
    }
}
