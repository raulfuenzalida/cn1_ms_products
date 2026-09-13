package duoc.cn1.ms_products.exception;

public class FilamentInactiveException extends RuntimeException {

	public FilamentInactiveException(Long id) {
		super("El filamento con ID " + id + " está inactivo y no puede usarse para calcular un producto");
	}
}
