package duoc.cn1.ms_products.repository;

import duoc.cn1.ms_products.model.PriceStatus;
import duoc.cn1.ms_products.model.Product;
import duoc.cn1.ms_products.model.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

	Optional<Product> findByIdAndStatusAndPriceStatus(Long id, ProductStatus status, PriceStatus priceStatus);

	List<Product> findByPriceStatus(PriceStatus priceStatus);

	List<Product> findByIdFilament(Long idFilament);
}
