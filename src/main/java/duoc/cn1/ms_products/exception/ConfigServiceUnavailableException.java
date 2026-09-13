package duoc.cn1.ms_products.exception;

public class ConfigServiceUnavailableException extends RuntimeException {

	public ConfigServiceUnavailableException(String message) {
		super(message);
	}

	public ConfigServiceUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
