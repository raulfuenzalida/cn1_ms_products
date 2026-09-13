package duoc.cn1.ms_products.service;

import duoc.cn1.ms_products.client.ConfigServiceClient;
import duoc.cn1.ms_products.client.dto.FilamentResponse;
import duoc.cn1.ms_products.client.dto.PrintingConfigResponse;
import duoc.cn1.ms_products.dto.request.ProductCreateRequest;
import duoc.cn1.ms_products.dto.request.ProductUpdateRequest;
import duoc.cn1.ms_products.dto.response.ProductPriceResponse;
import duoc.cn1.ms_products.dto.response.ProductPublicResponse;
import duoc.cn1.ms_products.dto.response.ProductResponse;
import duoc.cn1.ms_products.dto.response.RecalculationResultResponse;
import duoc.cn1.ms_products.exception.ProductNotFoundException;
import duoc.cn1.ms_products.model.PriceStatus;
import duoc.cn1.ms_products.model.Product;
import duoc.cn1.ms_products.model.ProductStatus;
import duoc.cn1.ms_products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final ConfigServiceClient configServiceClient;

	@Transactional
	public ProductResponse createProduct(ProductCreateRequest request) {
		log.info("Creando producto: {}", request.getName());

		FilamentResponse filament = configServiceClient.getFilamentById(request.getIdFilament());
		PrintingConfigResponse printingConfig = configServiceClient.getPrintingConfig();

		PriceCalculator.PriceBreakdown breakdown = PriceCalculator.calculate(
			request.getFilamentGrams(),
			filament.getPricePerKg(),
			request.getPrintingHours(),
			printingConfig.getPrinterConsumptionKwh(),
			printingConfig.getElectricityPriceKwh(),
			request.getProfitPercentage()
		);

		Product product = new Product();
		product.setName(request.getName());
		product.setDescription(request.getDescription());
		product.setIdFilament(request.getIdFilament());
		product.setFilamentGrams(request.getFilamentGrams());
		product.setPrintingHours(request.getPrintingHours());
		product.setProfitPercentage(request.getProfitPercentage());

		product.setFilamentPriceSnapshot(filament.getPricePerKg());
		product.setElectricityPriceSnapshot(printingConfig.getElectricityPriceKwh());
		product.setConsumptionKwhSnapshot(printingConfig.getPrinterConsumptionKwh());

		product.setMaterialCost(breakdown.materialCost());
		product.setElectricityCost(breakdown.electricityCost());
		product.setBaseCost(breakdown.baseCost());
		product.setProfitAmount(breakdown.profitAmount());
		product.setFinalPrice(breakdown.finalPrice());

		product.setStatus(ProductStatus.INACTIVE);
		product.setPriceStatus(PriceStatus.CURRENT);

		Product saved = productRepository.save(product);
		log.info("Producto creado con ID: {}", saved.getId());

		return mapToResponse(saved);
	}

	@Transactional(readOnly = true)
	public ProductResponse getProductById(Long id) {
		Product product = productRepository.findById(id)
			.orElseThrow(() -> new ProductNotFoundException(id));
		return mapToResponse(product);
	}

	@Transactional(readOnly = true)
	public ProductPublicResponse getPublicProductById(Long id) {
		Product product = productRepository.findByIdAndStatusAndPriceStatus(id, ProductStatus.ACTIVE, PriceStatus.CURRENT)
			.orElseThrow(() -> new ProductNotFoundException(id));
		return mapToPublicResponse(product);
	}

	@Transactional
	public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
		log.info("Actualizando producto: {}", id);

		Product product = productRepository.findById(id)
			.orElseThrow(() -> new ProductNotFoundException(id));

		if (request.getName() != null) {
			product.setName(request.getName());
		}
		if (request.getDescription() != null) {
			product.setDescription(request.getDescription());
		}
		if (request.getProfitPercentage() != null) {
			product.setProfitPercentage(request.getProfitPercentage());
			recalculatePrice(product);
		}

		Product saved = productRepository.save(product);
		return mapToResponse(saved);
	}

	@Transactional
	public ProductResponse updateProductStatus(Long id, ProductStatus status) {
		log.info("Actualizando estado del producto {} a {}", id, status);

		Product product = productRepository.findById(id)
			.orElseThrow(() -> new ProductNotFoundException(id));

		product.setStatus(status);
		Product saved = productRepository.save(product);

		return mapToResponse(saved);
	}

	@Transactional
	public RecalculationResultResponse recalculateProduct(Long id) {
		log.info("Recalculando precio del producto: {}", id);

		Product product = productRepository.findById(id)
			.orElseThrow(() -> new ProductNotFoundException(id));

		int previousPrice = product.getFinalPrice().intValue();
		recalculatePrice(product);

		product.setPriceStatus(PriceStatus.CURRENT);
		product.setStatus(ProductStatus.INACTIVE);

		Product saved = productRepository.save(product);

		return new RecalculationResultResponse(
			saved.getId(),
			previousPrice,
			saved.getFinalPrice().intValue(),
			saved.getStatus(),
			saved.getPriceStatus(),
			1,
			0
		);
	}

	@Transactional
	public RecalculationResultResponse recalculateOutdatedProducts() {
		log.info("Recalculando todos los productos con precio desactualizado");

		List<Product> outdatedProducts = productRepository.findByPriceStatus(PriceStatus.OUTDATED);
		int recalculedCount = 0;

		for (Product product : outdatedProducts) {
			try {
				recalculatePrice(product);
				product.setPriceStatus(PriceStatus.CURRENT);
				product.setStatus(ProductStatus.INACTIVE);
				productRepository.save(product);
				recalculedCount++;
			} catch (Exception e) {
				log.error("Error al recalcular producto {}: {}", product.getId(), e.getMessage());
			}
		}

		return new RecalculationResultResponse(
			null,
			0,
			0,
			null,
			null,
			recalculedCount,
			outdatedProducts.size()
		);
	}

	@Transactional(readOnly = true)
	public Page<ProductPublicResponse> getPublicCatalog(Specification<Product> spec, Pageable pageable) {
		Specification<Product> publicSpec = Specification.where(spec)
			.and((root, query, cb) -> cb.equal(root.get("status"), ProductStatus.ACTIVE))
			.and((root, query, cb) -> cb.equal(root.get("priceStatus"), PriceStatus.CURRENT));

		return productRepository.findAll(publicSpec, pageable).map(this::mapToPublicResponse);
	}

	@Transactional(readOnly = true)
	public Page<ProductResponse> getAdminProducts(Specification<Product> spec, Pageable pageable) {
		return productRepository.findAll(spec, pageable).map(this::mapToResponse);
	}

	@Transactional
	public void markProductsAsOutdatedByFilament(Long idFilament) {
		log.info("Marcando productos como OUTDATED por cambio en filamento: {}", idFilament);

		List<Product> products = productRepository.findByIdFilament(idFilament);
		for (Product product : products) {
			if (product.getPriceStatus() == PriceStatus.CURRENT) {
				product.setPriceStatus(PriceStatus.OUTDATED);
				product.setStatus(ProductStatus.INACTIVE);
				productRepository.save(product);
			}
		}
	}

	private void recalculatePrice(Product product) {
		FilamentResponse filament = configServiceClient.getFilamentById(product.getIdFilament());
		PrintingConfigResponse printingConfig = configServiceClient.getPrintingConfig();

		PriceCalculator.PriceBreakdown breakdown = PriceCalculator.calculate(
			product.getFilamentGrams(),
			filament.getPricePerKg(),
			product.getPrintingHours(),
			printingConfig.getPrinterConsumptionKwh(),
			printingConfig.getElectricityPriceKwh(),
			product.getProfitPercentage()
		);

		product.setFilamentPriceSnapshot(filament.getPricePerKg());
		product.setElectricityPriceSnapshot(printingConfig.getElectricityPriceKwh());
		product.setConsumptionKwhSnapshot(printingConfig.getPrinterConsumptionKwh());

		product.setMaterialCost(breakdown.materialCost());
		product.setElectricityCost(breakdown.electricityCost());
		product.setBaseCost(breakdown.baseCost());
		product.setProfitAmount(breakdown.profitAmount());
		product.setFinalPrice(breakdown.finalPrice());
	}

	private ProductResponse mapToResponse(Product product) {
		ProductPriceResponse price = new ProductPriceResponse(
			product.getFilamentPriceSnapshot(),
			product.getElectricityPriceSnapshot(),
			product.getConsumptionKwhSnapshot(),
			product.getMaterialCost(),
			product.getElectricityCost(),
			product.getBaseCost(),
			product.getProfitAmount(),
			product.getFinalPrice()
		);

		return new ProductResponse(
			product.getId(),
			product.getName(),
			product.getDescription(),
			product.getIdFilament(),
			product.getFilamentGrams(),
			product.getPrintingHours(),
			product.getProfitPercentage(),
			price,
			product.getStatus(),
			product.getPriceStatus(),
			null,
			null,
			product.getCreatedAt(),
			product.getUpdatedAt()
		);
	}

	private ProductPublicResponse mapToPublicResponse(Product product) {
		return new ProductPublicResponse(
			product.getId(),
			product.getName(),
			product.getDescription(),
			product.getFinalPrice(),
			null,
			null
		);
	}
}
