package duoc.cn1.ms_products.controller;

import duoc.cn1.ms_products.dto.request.ProductInvalidateRequest;
import duoc.cn1.ms_products.dto.response.InvalidationResultResponse;
import duoc.cn1.ms_products.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products/internal")
@RequiredArgsConstructor
@Tag(name = "Invalidación de Productos", description = "API interna para invalidación de precios por cambios de configuración")
public class ProductInvalidationController {

	private final ProductService productService;

	@PostMapping("/invalidate")
	@Operation(summary = "Invalidar productos por filamento", description = "Marca productos como OUTDATED por cambio en filamento")
	public ResponseEntity<InvalidationResultResponse> invalidateProductsByFilament(
			@Valid @RequestBody ProductInvalidateRequest request) {
		productService.markProductsAsOutdatedByFilament(request.getFilamentId());
		InvalidationResultResponse response = new InvalidationResultResponse(
			0,
			"Productos invalidados exitosamente para filamento: " + request.getFilamentId()
		);
		return ResponseEntity.ok(response);
	}
}
