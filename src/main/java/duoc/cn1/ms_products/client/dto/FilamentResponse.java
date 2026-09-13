package duoc.cn1.ms_products.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilamentResponse {

	private Long id;
	private String name;
	private String color;
	@JsonProperty("pricePerKg")
	private BigDecimal pricePerKg;
	private FilamentStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public enum FilamentStatus {
		ACTIVE,
		INACTIVE
	}
}
