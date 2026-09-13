package duoc.cn1.ms_products.dto.response;

import duoc.cn1.ms_products.model.PriceStatus;
import duoc.cn1.ms_products.model.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

	private Long id;
	private String name;
	private String description;
	private Long idFilament;
	private BigDecimal filamentGrams;
	private BigDecimal printingHours;
	private BigDecimal profitPercentage;
	private ProductPriceResponse price;
	private ProductStatus status;
	private PriceStatus priceStatus;
	private List<TagResponse> tags;
	private List<ProductImageResponse> activeImages;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
