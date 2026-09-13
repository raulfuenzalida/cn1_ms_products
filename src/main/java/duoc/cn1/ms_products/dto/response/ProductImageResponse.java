package duoc.cn1.ms_products.dto.response;

import duoc.cn1.ms_products.model.ImageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponse {

	private Long id;
	private ImageType imageType;
	private String s3Key;
	private String presignedUrl;
	private boolean active;
	private LocalDateTime createdAt;
}
