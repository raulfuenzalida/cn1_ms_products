package duoc.cn1.ms_products.exception;

public class DuplicateTagException extends RuntimeException {

	public DuplicateTagException(String name) {
		super("Ya existe un tag con el nombre: " + name);
	}

	public DuplicateTagException(Long productId, Long tagId) {
		super("El producto " + productId + " ya tiene asignado el tag " + tagId);
	}
}
