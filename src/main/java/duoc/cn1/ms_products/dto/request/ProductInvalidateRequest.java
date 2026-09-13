package duoc.cn1.ms_products.dto.request;

import lombok.Data;

/**
 * Invalidación protegida de precios cuando cambia ms-config.
 * Si {@code filamentId} está presente, solo se marcan productos de ese filamento.
 * Si está ausente, se invalidan todos los productos vigentes (cambio energético).
 */
@Data
public class ProductInvalidateRequest {

	private Long filamentId;
}
