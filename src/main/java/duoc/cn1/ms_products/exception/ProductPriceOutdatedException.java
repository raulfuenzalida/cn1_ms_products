package duoc.cn1.ms_products.exception;

public class ProductPriceOutdatedException extends RuntimeException {

	public ProductPriceOutdatedException(Long id) {
		super("El precio del producto con ID " + id + " está desactualizado. Debe recalcularse antes de activarlo");
	}
}
