package local.alejandrogb.metricsserversdesktop.client.exception;

/**
 * Excepción base para errores de la capa de cliente HTTP.
 */
public class ApiException extends RuntimeException {

	private static final long serialVersionUID = 1488674644725256573L;
	private final int statusCode;

	public ApiException(String message) {
		super(message);
		this.statusCode = -1;
	}

	public ApiException(int statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}

	public ApiException(String message, Throwable cause) {
		super(message, cause);
		this.statusCode = -1;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public boolean isUnauthorized() {
		return statusCode == 401;
	}

	public boolean isNotFound() {
		return statusCode == 404;
	}

	public boolean isValidationError() {
		return statusCode == 422;
	}

	public boolean isServerError() {
		return statusCode >= 500;
	}
}
