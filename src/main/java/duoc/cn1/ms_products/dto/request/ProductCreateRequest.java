package duoc.cn1.ms_products.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateRequest {

	@NotBlank(message = "El nombre del producto es obligatorio")
	private String name;

	private String description;

	@NotNull(message = "El identificador de filamento es obligatorio")
	private Long idFilament;

	@NotNull(message = "Los gramos de filamento son obligatorios")
	@DecimalMin(value = "0.01", message = "Los gramos de filamento deben ser mayores que cero")
	private BigDecimal filamentGrams;

	@NotNull(message = "Las horas de impresión son obligatorias")
	@DecimalMin(value = "0.01", message = "Las horas de impresión deben ser mayores que cero")
	private BigDecimal printingHours;

	@NotNull(message = "El porcentaje de ganancia es obligatorio")
	@DecimalMin(value = "0.00", message = "El porcentaje de ganancia no puede ser negativo")
	private BigDecimal profitPercentage;
}
