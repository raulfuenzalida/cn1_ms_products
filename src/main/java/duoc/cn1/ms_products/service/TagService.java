package duoc.cn1.ms_products.service;

import duoc.cn1.ms_products.dto.request.TagRequest;
import duoc.cn1.ms_products.dto.response.TagResponse;
import duoc.cn1.ms_products.exception.DuplicateTagException;
import duoc.cn1.ms_products.exception.ProductNotFoundException;
import duoc.cn1.ms_products.exception.TagNotFoundException;
import duoc.cn1.ms_products.model.Product;
import duoc.cn1.ms_products.model.Tag;
import duoc.cn1.ms_products.repository.ProductRepository;
import duoc.cn1.ms_products.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

	private final TagRepository tagRepository;
	private final ProductRepository productRepository;

	@Transactional
	public TagResponse createTag(TagRequest request) {
		log.info("Creando tag: {}", request.getName());

		String normalizedName = normalizeName(request.getName());

		if (tagRepository.findByName(normalizedName).isPresent()) {
			throw new DuplicateTagException("Ya existe un tag con el nombre: " + request.getName());
		}

		Tag tag = new Tag();
		tag.setName(normalizedName);
		Tag saved = tagRepository.save(tag);

		return mapToResponse(saved);
	}

	@Transactional(readOnly = true)
	public List<TagResponse> getAllTags() {
		return tagRepository.findAll().stream()
			.map(this::mapToResponse)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public TagResponse getTagById(Long id) {
		Tag tag = tagRepository.findById(id)
			.orElseThrow(() -> new TagNotFoundException(id));
		return mapToResponse(tag);
	}

	@Transactional
	public void deleteTag(Long id) {
		log.info("Eliminando tag: {}", id);

		Tag tag = tagRepository.findById(id)
			.orElseThrow(() -> new TagNotFoundException(id));

		tagRepository.delete(tag);
	}

	@Transactional
	public void addTagToProduct(Long productId, Long tagId) {
		log.info("Agregando tag {} al producto {}", tagId, productId);

		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new ProductNotFoundException(productId));

		Tag tag = tagRepository.findById(tagId)
			.orElseThrow(() -> new TagNotFoundException(tagId));

		if (!product.getTags().contains(tag)) {
			product.getTags().add(tag);
			productRepository.save(product);
		}
	}

	@Transactional
	public void removeTagFromProduct(Long productId, Long tagId) {
		log.info("Removiendo tag {} del producto {}", tagId, productId);

		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new ProductNotFoundException(productId));

		Tag tag = tagRepository.findById(tagId)
			.orElseThrow(() -> new TagNotFoundException(tagId));

		if (product.getTags().contains(tag)) {
			product.getTags().remove(tag);
			productRepository.save(product);
		}
	}

	private String normalizeName(String name) {
		return name.trim().toLowerCase();
	}

	private TagResponse mapToResponse(Tag tag) {
		return new TagResponse(tag.getId(), tag.getName());
	}
}
