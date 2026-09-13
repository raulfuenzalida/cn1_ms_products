package duoc.cn1.ms_products.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Permite registrar una clave S3 existente cuando no se envía un archivo.
 */
@Data
public class ImageUploadRequest {

	@NotBlank(message = "La clave S3 es obligatoria cuando no se envía un archivo")
	private String s3Key;
}
