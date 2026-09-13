package duoc.cn1.ms_products.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Vista pública del producto. No expone costos internos, gramos, kWh ni margen.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPublicResponse {

	private Long id;
	private String name;
	private String description;
	private BigDecimal finalPrice;
	private List<TagResponse> tags;
	private List<ProductImageResponse> images;
}
