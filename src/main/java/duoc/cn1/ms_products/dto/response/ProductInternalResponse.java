package duoc.cn1.ms_products.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Vista interna para comunicación entre microservicios.
 * Expone los datos de filamento necesarios para el consumo de inventario,
 * sin exponer el desglose de costos del precio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductInternalResponse {

	private Long id;
	private String name;
	private BigDecimal finalPrice;
	private Long idFilament;
	private BigDecimal filamentGrams;
}
