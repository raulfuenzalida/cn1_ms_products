package duoc.cn1.ms_products.dto.response;

import duoc.cn1.ms_products.model.PriceStatus;
import duoc.cn1.ms_products.model.ProductStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RecalculationResultResponse {

	private Long productId;
	private Integer previousPrice;
	private Integer newPrice;
	private ProductStatus status;
	private PriceStatus priceStatus;

	private Integer recalculatedCount;
	private Integer totalOutdated;

	public RecalculationResultResponse(Long productId, Integer previousPrice, Integer newPrice,
			ProductStatus status, PriceStatus priceStatus) {
		this.productId = productId;
		this.previousPrice = previousPrice;
		this.newPrice = newPrice;
		this.status = status;
		this.priceStatus = priceStatus;
	}

	public RecalculationResultResponse(Long productId, Integer previousPrice, Integer newPrice,
			ProductStatus status, PriceStatus priceStatus, Integer recalculatedCount, Integer totalOutdated) {
		this.productId = productId;
		this.previousPrice = previousPrice;
		this.newPrice = newPrice;
		this.status = status;
		this.priceStatus = priceStatus;
		this.recalculatedCount = recalculatedCount;
		this.totalOutdated = totalOutdated;
	}
}
