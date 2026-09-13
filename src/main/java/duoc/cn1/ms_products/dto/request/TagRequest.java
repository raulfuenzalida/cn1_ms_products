package duoc.cn1.ms_products.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Asigna un tag existente por ID o crea/asocia uno por nombre.
 * El nombre se normaliza a minúsculas sin espacios extremos.
 */
@Data
public class TagRequest {

	private Long idTag;

	@Size(max = 100, message = "El nombre del tag no puede superar 100 caracteres")
	private String name;
}
