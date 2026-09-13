package duoc.cn1.ms_products.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {

	private String name;

	private String description;

	private BigDecimal filamentGrams;

	@DecimalMin(value = "0.01", message = "Los gramos de filamento deben ser mayores que cero")
	private BigDecimal printingHours;

	@DecimalMin(value = "0.00", message = "El porcentaje de ganancia no puede ser negativo")
	private BigDecimal profitPercentage;
}
