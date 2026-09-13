package duoc.cn1.ms_products.repository;

import duoc.cn1.ms_products.model.ImageType;
import duoc.cn1.ms_products.model.Product;
import duoc.cn1.ms_products.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

	List<ProductImage> findByProductOrderByCreatedAtDesc(Product product);

	List<ProductImage> findByProductAndActiveTrue(Product product);

	Optional<ProductImage> findByProductAndImageTypeAndActiveTrue(Product product, ImageType imageType);

	Optional<ProductImage> findByIdAndProduct(Long id, Product product);
}
