package duoc.cn1.ms_products.controller;

import duoc.cn1.ms_products.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products/{productId}/tags")
@RequiredArgsConstructor
@Tag(name = "Tags de Productos", description = "API de gestión de asociación de tags a productos")
public class ProductTagController {

	private final TagService tagService;

	@PostMapping("/{tagId}")
	@Operation(summary = "Agregar tag a producto", description = "Asocia un tag existente a un producto")
	public ResponseEntity<Void> addTagToProduct(
			@PathVariable Long productId,
			@PathVariable Long tagId) {
		tagService.addTagToProduct(productId, tagId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{tagId}")
	@Operation(summary = "Remover tag de producto", description = "Desasocia un tag de un producto")
	public ResponseEntity<Void> removeTagFromProduct(
			@PathVariable Long productId,
			@PathVariable Long tagId) {
		tagService.removeTagFromProduct(productId, tagId);
		return ResponseEntity.noContent().build();
	}
}
