package duoc.cn1.ms_products.controller;

import duoc.cn1.ms_products.dto.request.ImageUploadRequest;
import duoc.cn1.ms_products.dto.response.ProductImageResponse;
import duoc.cn1.ms_products.model.ImageType;
import duoc.cn1.ms_products.service.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/images")
@RequiredArgsConstructor
@Tag(name = "Imágenes de Productos", description = "API de gestión de imágenes de productos")
public class ProductImageController {

	private final ProductImageService productImageService;

	@PostMapping("/{type}")
	@Operation(summary = "Subir imagen", description = "Sube o registra una imagen para un producto")
	public ResponseEntity<ProductImageResponse> uploadImage(
			@PathVariable Long productId,
			@PathVariable ImageType type,
			@Valid @RequestBody ImageUploadRequest request) {
		ProductImageResponse response = productImageService.uploadImage(productId, type, request);
		return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
	}

	@GetMapping
	@Operation(summary = "Obtener imágenes activas", description = "Retorna las imágenes activas de un producto")
	public ResponseEntity<List<ProductImageResponse>> getProductImages(@PathVariable Long productId) {
		return ResponseEntity.ok(productImageService.getProductImages(productId));
	}

	@GetMapping("/history")
	@Operation(summary = "Obtener historial de imágenes", description = "Retorna todo el historial de imágenes de un producto")
	public ResponseEntity<List<ProductImageResponse>> getImageHistory(@PathVariable Long productId) {
		return ResponseEntity.ok(productImageService.getImageHistory(productId));
	}

	@PostMapping("/{imageId}/restore")
	@Operation(summary = "Restaurar imagen", description = "Restaura una imagen histórica y la activa nuevamente")
	public ResponseEntity<ProductImageResponse> restoreImage(
			@PathVariable Long productId,
			@PathVariable Long imageId) {
		return ResponseEntity.ok(productImageService.restoreImage(productId, imageId));
	}

	@DeleteMapping("/{imageId}")
	@Operation(summary = "Eliminar imagen", description = "Desactiva una imagen de un producto")
	public ResponseEntity<Void> deleteImage(
			@PathVariable Long productId,
			@PathVariable Long imageId) {
		productImageService.deleteImage(productId, imageId);
		return ResponseEntity.noContent().build();
	}
}
