package duoc.cn1.ms_products.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvalidationResultResponse {

	private int affectedProducts;
	private String message;
}
