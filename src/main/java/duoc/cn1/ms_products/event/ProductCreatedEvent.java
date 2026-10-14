package duoc.cn1.ms_products.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductCreatedEvent {

	private Long productId;
	private String name;
	private String description;
	private String filamentName;
	private BigDecimal grams;
	private BigDecimal printingHours;
	private Integer price;
	private String status;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;

	public ProductCreatedEvent() {
	}

	public ProductCreatedEvent(
			Long productId,
			String name,
			String description,
			String filamentName,
			BigDecimal grams,
			BigDecimal printingHours,
			Integer price,
			String status,
			LocalDateTime createdAt) {
		this.productId = productId;
		this.name = name;
		this.description = description;
		this.filamentName = filamentName;
		this.grams = grams;
		this.printingHours = printingHours;
		this.price = price;
		this.status = status;
		this.createdAt = createdAt;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
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

	public String getFilamentName() {
		return filamentName;
	}

	public void setFilamentName(String filamentName) {
		this.filamentName = filamentName;
	}

	public BigDecimal getGrams() {
		return grams;
	}

	public void setGrams(BigDecimal grams) {
		this.grams = grams;
	}

	public BigDecimal getPrintingHours() {
		return printingHours;
	}

	public void setPrintingHours(BigDecimal printingHours) {
		this.printingHours = printingHours;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
