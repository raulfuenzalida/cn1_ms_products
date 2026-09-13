package duoc.cn1.ms_products.service;

import duoc.cn1.ms_products.dto.request.ImageUploadRequest;
import duoc.cn1.ms_products.dto.response.ProductImageResponse;
import duoc.cn1.ms_products.exception.ImageNotFoundException;
import duoc.cn1.ms_products.exception.ProductNotFoundException;
import duoc.cn1.ms_products.model.ImageType;
import duoc.cn1.ms_products.model.Product;
import duoc.cn1.ms_products.model.ProductImage;
import duoc.cn1.ms_products.repository.ProductImageRepository;
import duoc.cn1.ms_products.repository.ProductRepository;
import duoc.cn1.ms_products.storage.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageService {

	private final ProductImageRepository productImageRepository;
	private final ProductRepository productRepository;
	private final S3Service s3Service;

	@Transactional
	public ProductImageResponse uploadImage(Long productId, ImageType imageType, ImageUploadRequest request) {
		log.info("Subiendo imagen para producto {} tipo {}", productId, imageType);

		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new ProductNotFoundException(productId));

		String s3Key = s3Service.uploadFileFromKey(request.getS3Key());

		productImageRepository.findByProductAndImageTypeAndActiveTrue(product, imageType)
			.ifPresent(existingImage -> {
				existingImage.setActive(false);
				productImageRepository.save(existingImage);
			});

		ProductImage productImage = new ProductImage();
		productImage.setProduct(product);
		productImage.setImageType(imageType);
		productImage.setS3Key(s3Key);
		productImage.setActive(true);

		ProductImage saved = productImageRepository.save(productImage);
		return mapToResponse(saved);
	}

	@Transactional(readOnly = true)
	public List<ProductImageResponse> getProductImages(Long productId) {
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new ProductNotFoundException(productId));

		return productImageRepository.findByProductAndActiveTrue(product).stream()
			.map(this::mapToResponse)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<ProductImageResponse> getImageHistory(Long productId) {
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new ProductNotFoundException(productId));

		return productImageRepository.findByProductOrderByCreatedAtDesc(product).stream()
			.map(this::mapToResponse)
			.collect(Collectors.toList());
	}

	@Transactional
	public ProductImageResponse restoreImage(Long productId, Long imageId) {
		log.info("Restaurando imagen {} para producto {}", imageId, productId);

		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new ProductNotFoundException(productId));

		ProductImage imageToRestore = productImageRepository.findByIdAndProduct(imageId, product)
			.orElseThrow(() -> new ImageNotFoundException(imageId));

		productImageRepository.findByProductAndImageTypeAndActiveTrue(product, imageToRestore.getImageType())
			.ifPresent(existingImage -> {
				existingImage.setActive(false);
				productImageRepository.save(existingImage);
			});

		imageToRestore.setActive(true);
		ProductImage saved = productImageRepository.save(imageToRestore);

		return mapToResponse(saved);
	}

	@Transactional
	public void deleteImage(Long productId, Long imageId) {
		log.info("Eliminando imagen {} del producto {}", imageId, productId);

		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new ProductNotFoundException(productId));

		ProductImage image = productImageRepository.findByIdAndProduct(imageId, product)
			.orElseThrow(() -> new ImageNotFoundException(imageId));

		image.setActive(false);
		productImageRepository.save(image);
	}

	private ProductImageResponse mapToResponse(ProductImage productImage) {
		String presignedUrl = s3Service.generatePresignedUrl(productImage.getS3Key(), 15);

		return new ProductImageResponse(
			productImage.getId(),
			productImage.getImageType(),
			productImage.getS3Key(),
			presignedUrl,
			productImage.isActive(),
			productImage.getCreatedAt()
		);
	}
}
