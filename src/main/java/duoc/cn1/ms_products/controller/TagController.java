package duoc.cn1.ms_products.controller;

import duoc.cn1.ms_products.dto.request.TagRequest;
import duoc.cn1.ms_products.dto.response.TagResponse;
import duoc.cn1.ms_products.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "API de gestión de tags de productos")
public class TagController {

	private final TagService tagService;

	@GetMapping
	@Operation(summary = "Obtener todos los tags", description = "Retorna una lista con todos los tags registrados")
	public ResponseEntity<List<TagResponse>> getAllTags() {
		return ResponseEntity.ok(tagService.getAllTags());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Obtener tag por ID", description = "Retorna los detalles de un tag específico")
	public ResponseEntity<TagResponse> getTagById(@PathVariable Long id) {
		return ResponseEntity.ok(tagService.getTagById(id));
	}

	@PostMapping
	@Operation(summary = "Crear tag", description = "Crea un nuevo tag con los datos proporcionados")
	public ResponseEntity<TagResponse> createTag(@Valid @RequestBody TagRequest request) {
		TagResponse response = tagService.createTag(request);
		return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Eliminar tag", description = "Elimina un tag existente")
	public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
		tagService.deleteTag(id);
		return ResponseEntity.noContent().build();
	}
}
