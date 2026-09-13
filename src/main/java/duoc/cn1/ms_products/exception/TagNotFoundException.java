package duoc.cn1.ms_products.exception;

public class TagNotFoundException extends RuntimeException {

	public TagNotFoundException(Long id) {
		super("No se encontró el tag solicitado con ID: " + id);
	}
}
