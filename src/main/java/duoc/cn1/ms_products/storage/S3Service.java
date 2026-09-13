package duoc.cn1.ms_products.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

	@Value("${aws.region}")
	private String region;

	@Value("${aws.s3.bucket}")
	private String bucketName;

	private S3Client s3Client;

	private S3Client getS3Client() {
		if (s3Client == null) {
			s3Client = S3Client.builder()
				.region(Region.of(region))
				.build();
		}
		return s3Client;
	}

	public String uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
		log.info("Subiendo archivo a S3: bucket={}, key={}", bucketName, key);

		try {
			PutObjectRequest putRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(key)
				.contentType(contentType)
				.build();

			getS3Client().putObject(putRequest, RequestBody.fromInputStream(inputStream, contentLength));
			log.info("Archivo subido exitosamente: {}", key);
			return key;
		} catch (Exception e) {
			log.error("Error al subir archivo a S3: {}", e.getMessage());
			throw new RuntimeException("Error al subir archivo a S3", e);
		}
	}

	public String uploadFileFromKey(String key) {
		log.info("Registrando clave S3 existente: bucket={}, key={}", bucketName, key);

		try {
			if (!objectExists(key)) {
				throw new IllegalArgumentException("El objeto no existe en S3: " + key);
			}
			return key;
		} catch (Exception e) {
			log.error("Error al verificar archivo en S3: {}", e.getMessage());
			throw new RuntimeException("Error al verificar archivo en S3", e);
		}
	}

	public void deleteFile(String key) {
		log.info("Eliminando archivo de S3: bucket={}, key={}", bucketName, key);

		try {
			DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
				.bucket(bucketName)
				.key(key)
				.build();

			getS3Client().deleteObject(deleteRequest);
			log.info("Archivo eliminado exitosamente: {}", key);
		} catch (Exception e) {
			log.error("Error al eliminar archivo de S3: {}", e.getMessage());
			throw new RuntimeException("Error al eliminar archivo de S3", e);
		}
	}

	public String generatePresignedUrl(String key, int expirationMinutes) {
		log.info("Generando URL presignada: bucket={}, key={}", bucketName, key);

		try {
			GetUrlRequest getUrlRequest = GetUrlRequest.builder()
				.bucket(bucketName)
				.key(key)
				.build();

			return getS3Client().utilities().getUrl(getUrlRequest).toExternalForm();
		} catch (Exception e) {
			log.error("Error al generar URL presignada: {}", e.getMessage());
			throw new RuntimeException("Error al generar URL presignada", e);
		}
	}

	private boolean objectExists(String key) {
		try {
			HeadObjectRequest headRequest = HeadObjectRequest.builder()
				.bucket(bucketName)
				.key(key)
				.build();

			getS3Client().headObject(headRequest);
			return true;
		} catch (NoSuchKeyException e) {
			return false;
		} catch (Exception e) {
			log.error("Error al verificar existencia del objeto: {}", e.getMessage());
			throw new RuntimeException("Error al verificar existencia del objeto", e);
		}
	}
}
