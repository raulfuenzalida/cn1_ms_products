package duoc.cn1.ms_products.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrintingConfigResponse {

	private Long id;
	@JsonProperty("electricityPriceKwh")
	private BigDecimal electricityPriceKwh;
	@JsonProperty("printerConsumptionKwh")
	private BigDecimal printerConsumptionKwh;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
