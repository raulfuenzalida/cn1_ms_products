package duoc.cn1.ms_products.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {

	private String name;

	private String description;

	@Positive(message = "El ID del filamento debe ser mayor que cero")
	private Long idFilament;

	@DecimalMin(
		value = "0.01",
		message = "Los gramos de filamento deben ser mayores que cero"
	)
	private BigDecimal filamentGrams;

	@DecimalMin(
		value = "0.01",
		message = "Las horas de impresión deben ser mayores que cero"
	)
	private BigDecimal printingHours;

	@DecimalMin(
		value = "0.00",
		message = "El porcentaje de ganancia no puede ser negativo"
	)
	private BigDecimal profitPercentage;
}