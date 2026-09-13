package duoc.cn1.ms_products.exception;

public class FilamentNotFoundException extends RuntimeException {

	public FilamentNotFoundException(Long id) {
		super("No se encontró el filamento solicitado con ID: " + id);
	}
}
