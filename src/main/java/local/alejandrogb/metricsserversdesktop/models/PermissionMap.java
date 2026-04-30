package local.alejandrogb.metricsserversdesktop.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PermissionMap<T> {

	private List<T> global;
	private Map<Integer, List<T>> sections;

	public PermissionMap() {
	}

	public PermissionMap(List<T> global, Map<Integer, List<T>> sections) {
		this.global = global;
		this.sections = sections;
	}

	public List<T> getGlobal() {
		return global;
	}

	public void setGlobal(List<T> global) {
		this.global = global;
	}

	public Map<Integer, List<T>> getSections() {
		return sections;
	}

	public void setSections(Map<Integer, List<T>> sections) {
		this.sections = sections;
	}

	@Override
	public String toString() {
		return "PermissionMap{global=" + global + ", sections=" + sections + '}';
	}
}
