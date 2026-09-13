package duoc.cn1.ms_products.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponse {

	private List<ProductPublicResponse> items;
	private int total;
}
