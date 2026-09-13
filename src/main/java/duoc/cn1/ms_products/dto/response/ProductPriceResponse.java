package duoc.cn1.ms_products.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPriceResponse {

	private BigDecimal filamentPriceSnapshot;
	private BigDecimal electricityPriceSnapshot;
	private BigDecimal consumptionKwhSnapshot;
	private BigDecimal materialCost;
	private BigDecimal electricityCost;
	private BigDecimal baseCost;
	private BigDecimal profitAmount;
	private BigDecimal finalPrice;
}
