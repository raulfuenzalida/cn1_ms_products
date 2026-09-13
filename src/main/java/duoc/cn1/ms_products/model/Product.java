package duoc.cn1.ms_products.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_product")
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description", length = 2000)
	private String description;

	@Column(name = "id_filament", nullable = false)
	private Long idFilament;

	@Column(name = "filament_grams", nullable = false, precision = 10, scale = 2)
	private BigDecimal filamentGrams;

	@Column(name = "printing_hours", nullable = false, precision = 10, scale = 2)
	private BigDecimal printingHours;

	@Column(name = "profit_percentage", nullable = false, precision = 7, scale = 2)
	private BigDecimal profitPercentage;

	@Column(name = "filament_price_snapshot", nullable = false, precision = 12, scale = 4)
	private BigDecimal filamentPriceSnapshot;

	@Column(name = "electricity_price_snapshot", nullable = false, precision = 12, scale = 4)
	private BigDecimal electricityPriceSnapshot;

	@Column(name = "consumption_kwh_snapshot", nullable = false, precision = 12, scale = 4)
	private BigDecimal consumptionKwhSnapshot;

	@Column(name = "material_cost", nullable = false, precision = 12, scale = 4)
	private BigDecimal materialCost;

	@Column(name = "electricity_cost", nullable = false, precision = 12, scale = 4)
	private BigDecimal electricityCost;

	@Column(name = "base_cost", nullable = false, precision = 12, scale = 4)
	private BigDecimal baseCost;

	@Column(name = "profit_amount", nullable = false, precision = 12, scale = 4)
	private BigDecimal profitAmount;

	@Column(name = "final_price", nullable = false, precision = 12, scale = 0)
	private BigDecimal finalPrice;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private ProductStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "price_status", nullable = false, length = 20)
	private PriceStatus priceStatus;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@ManyToMany
	@JoinTable(
		name = "product_tags",
		joinColumns = @JoinColumn(name = "id_product"),
		inverseJoinColumns = @JoinColumn(name = "id_tag")
	)
	private Set<Tag> tags = new HashSet<>();

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = false)
	private Set<ProductImage> images = new HashSet<>();

	public Product() {
	}

	@PrePersist
	public void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getIdFilament() {
		return idFilament;
	}

	public void setIdFilament(Long idFilament) {
		this.idFilament = idFilament;
	}

	public BigDecimal getFilamentGrams() {
		return filamentGrams;
	}

	public void setFilamentGrams(BigDecimal filamentGrams) {
		this.filamentGrams = filamentGrams;
	}

	public BigDecimal getPrintingHours() {
		return printingHours;
	}

	public void setPrintingHours(BigDecimal printingHours) {
		this.printingHours = printingHours;
	}

	public BigDecimal getProfitPercentage() {
		return profitPercentage;
	}

	public void setProfitPercentage(BigDecimal profitPercentage) {
		this.profitPercentage = profitPercentage;
	}

	public BigDecimal getFilamentPriceSnapshot() {
		return filamentPriceSnapshot;
	}

	public void setFilamentPriceSnapshot(BigDecimal filamentPriceSnapshot) {
		this.filamentPriceSnapshot = filamentPriceSnapshot;
	}

	public BigDecimal getElectricityPriceSnapshot() {
		return electricityPriceSnapshot;
	}

	public void setElectricityPriceSnapshot(BigDecimal electricityPriceSnapshot) {
		this.electricityPriceSnapshot = electricityPriceSnapshot;
	}

	public BigDecimal getConsumptionKwhSnapshot() {
		return consumptionKwhSnapshot;
	}

	public void setConsumptionKwhSnapshot(BigDecimal consumptionKwhSnapshot) {
		this.consumptionKwhSnapshot = consumptionKwhSnapshot;
	}

	public BigDecimal getMaterialCost() {
		return materialCost;
	}

	public void setMaterialCost(BigDecimal materialCost) {
		this.materialCost = materialCost;
	}

	public BigDecimal getElectricityCost() {
		return electricityCost;
	}

	public void setElectricityCost(BigDecimal electricityCost) {
		this.electricityCost = electricityCost;
	}

	public BigDecimal getBaseCost() {
		return baseCost;
	}

	public void setBaseCost(BigDecimal baseCost) {
		this.baseCost = baseCost;
	}

	public BigDecimal getProfitAmount() {
		return profitAmount;
	}

	public void setProfitAmount(BigDecimal profitAmount) {
		this.profitAmount = profitAmount;
	}

	public BigDecimal getFinalPrice() {
		return finalPrice;
	}

	public void setFinalPrice(BigDecimal finalPrice) {
		this.finalPrice = finalPrice;
	}

	public ProductStatus getStatus() {
		return status;
	}

	public void setStatus(ProductStatus status) {
		this.status = status;
	}

	public PriceStatus getPriceStatus() {
		return priceStatus;
	}

	public void setPriceStatus(PriceStatus priceStatus) {
		this.priceStatus = priceStatus;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public Set<ProductImage> getImages() {
		return images;
	}

	public void setImages(Set<ProductImage> images) {
		this.images = images;
	}
}
