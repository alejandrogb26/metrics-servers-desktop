package local.alejandrogb.metricsserversdesktop.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkResult {

	private int total;
	private int ok;
	private int failed;
	private List<String> errors;

	public BulkResult() {
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getOk() {
		return ok;
	}

	public void setOk(int ok) {
		this.ok = ok;
	}

	public int getFailed() {
		return failed;
	}

	public void setFailed(int failed) {
		this.failed = failed;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}

	public String getSummary() {
		return String.format("Total: %d | OK: %d | Fallidos: %d", total, ok, failed);
	}

	@Override
	public String toString() {
		return getSummary();
	}
}
