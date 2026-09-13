package duoc.cn1.ms_products.dto.request;

import duoc.cn1.ms_products.model.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductStatusUpdateRequest {

	@NotNull(message = "El estado del producto es obligatorio")
	private ProductStatus status;
}
