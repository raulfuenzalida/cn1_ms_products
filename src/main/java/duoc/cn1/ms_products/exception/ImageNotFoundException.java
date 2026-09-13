package duoc.cn1.ms_products.exception;

public class ImageNotFoundException extends RuntimeException {

	public ImageNotFoundException(Long id) {
		super("No se encontró la imagen solicitada con ID: " + id);
	}
}
