package duoc.cn1.ms_products.controller;

import duoc.cn1.ms_products.dto.request.ProductCreateRequest;
import duoc.cn1.ms_products.dto.request.ProductStatusUpdateRequest;
import duoc.cn1.ms_products.dto.request.ProductUpdateRequest;
import duoc.cn1.ms_products.dto.response.ProductPublicResponse;
import duoc.cn1.ms_products.dto.response.ProductResponse;
import duoc.cn1.ms_products.dto.response.RecalculationResultResponse;
import duoc.cn1.ms_products.model.ProductSort;
import duoc.cn1.ms_products.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Productos Públicos", description = "API pública de catálogo de productos")
public class ProductController {

	private final ProductService productService;

	@GetMapping
	@Operation(summary = "Obtener catálogo público", description = "Retorna productos activos con precio actual")
	public ResponseEntity<Page<ProductPublicResponse>> getPublicCatalog(
			@RequestParam(required = false) String name,
			@RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(required = false) String sortBy,
			@RequestParam(required = false) String sortOrder,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {

		Specification<duoc.cn1.ms_products.model.Product> spec = buildPublicCatalogSpec(name, minPrice, maxPrice);
		Pageable pageable = buildPageable(sortBy, sortOrder, page, size);

		Page<ProductPublicResponse> result = productService.getPublicCatalog(spec, pageable);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Obtener producto público por ID", description = "Retorna un producto activo con precio actual")
	public ResponseEntity<ProductPublicResponse> getPublicProductById(@PathVariable Long id) {
		return ResponseEntity.ok(productService.getPublicProductById(id));
	}

	@GetMapping("/admin")
	@Operation(summary = "Obtener todos los productos (admin)", description = "Retorna todos los productos sin filtrar por estado")
	public ResponseEntity<Page<ProductResponse>> getAdminProducts(
			@RequestParam(required = false) String name,
			@RequestParam(required = false) String sortBy,
			@RequestParam(required = false) String sortOrder,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {

		Specification<duoc.cn1.ms_products.model.Product> spec = buildAdminSpec(name);
		Pageable pageable = buildPageable(sortBy, sortOrder, page, size);

		return ResponseEntity.ok(productService.getAdminProducts(spec, pageable));
	}

	@GetMapping("/admin/{id}")
	@Operation(summary = "Obtener producto por ID (admin)", description = "Retorna un producto sin filtrar por estado")
	public ResponseEntity<ProductResponse> getAdminProductById(@PathVariable Long id) {
		return ResponseEntity.ok(productService.getProductById(id));
	}

	@PostMapping
	@Operation(summary = "Crear producto", description = "Crea un nuevo producto calculando su precio automáticamente")
	public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
		ProductResponse response = productService.createProduct(request);
		return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Actualizar producto", description = "Actualiza los datos de un producto existente")
	public ResponseEntity<ProductResponse> updateProduct(
			@PathVariable Long id,
			@Valid @RequestBody ProductUpdateRequest request) {
		return ResponseEntity.ok(productService.updateProduct(id, request));
	}

	@PatchMapping("/{id}/status")
	@Operation(summary = "Actualizar estado del producto", description = "Actualiza el estado (ACTIVE/INACTIVE) de un producto")
	public ResponseEntity<ProductResponse> updateProductStatus(
			@PathVariable Long id,
			@Valid @RequestBody ProductStatusUpdateRequest request) {
		return ResponseEntity.ok(productService.updateProductStatus(id, request.getStatus()));
	}

	@PostMapping("/{id}/recalculate")
	@Operation(summary = "Recalcular precio del producto", description = "Recalcula el precio de un producto con configuración actual")
	public ResponseEntity<RecalculationResultResponse> recalculateProduct(@PathVariable Long id) {
		return ResponseEntity.ok(productService.recalculateProduct(id));
	}

	@PostMapping("/recalculate-outdated")
	@Operation(summary = "Recalcular productos desactualizados", description = "Recalcula todos los productos con precio OUTDATED")
	public ResponseEntity<RecalculationResultResponse> recalculateOutdatedProducts() {
		return ResponseEntity.ok(productService.recalculateOutdatedProducts());
	}

	private Specification<duoc.cn1.ms_products.model.Product> buildPublicCatalogSpec(String name, BigDecimal minPrice, BigDecimal maxPrice) {
		Specification<duoc.cn1.ms_products.model.Product> spec = Specification.where(null);

		if (name != null && !name.isBlank()) {
			spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
		}

		if (minPrice != null) {
			spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("finalPrice"), minPrice));
		}

		if (maxPrice != null) {
			spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("finalPrice"), maxPrice));
		}

		return spec;
	}

	private Specification<duoc.cn1.ms_products.model.Product> buildAdminSpec(String name) {
		Specification<duoc.cn1.ms_products.model.Product> spec = Specification.where(null);

		if (name != null && !name.isBlank()) {
			spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
		}

		return spec;
	}

	private Pageable buildPageable(String sortBy, String sortOrder, int page, int size) {
		Sort sort = Sort.unsorted();

		if (sortBy != null && !sortBy.isBlank()) {
			Sort.Direction direction = Sort.Direction.ASC;
			if (sortOrder != null && sortOrder.equalsIgnoreCase("DESC")) {
				direction = Sort.Direction.DESC;
			}

			String fieldName = sortBy.toLowerCase();
			if (fieldName.equals("name") || fieldName.equals("price")) {
				if (fieldName.equals("price")) {
					fieldName = "finalPrice";
				}
				sort = Sort.by(direction, fieldName);
			}
		}

		return PageRequest.of(page, size, sort);
	}
}
