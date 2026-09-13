package duoc.cn1.ms_products.exception;

public class ProductNotFoundException extends RuntimeException {

	public ProductNotFoundException(Long id) {
		super("No se encontró el producto solicitado con ID: " + id);
	}
}
