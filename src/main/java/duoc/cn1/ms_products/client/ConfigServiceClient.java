package duoc.cn1.ms_products.client;

import duoc.cn1.ms_products.client.dto.FilamentResponse;
import duoc.cn1.ms_products.client.dto.PrintingConfigResponse;
import duoc.cn1.ms_products.exception.ConfigServiceUnavailableException;
import duoc.cn1.ms_products.exception.FilamentInactiveException;
import duoc.cn1.ms_products.exception.FilamentNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigServiceClient {

	private final RestTemplate restTemplate;

	@Value("${ms-config.base-url}")
	private String baseUrl;

	@Value("${ms-config.connect-timeout-ms:3000}")
	private int connectTimeout;

	@Value("${ms-config.read-timeout-ms:5000}")
	private int readTimeout;

	public FilamentResponse getFilamentById(Long id) {
		String url = baseUrl + "/api/v1/config/filaments/" + id;
		log.debug("Consultando filamento en ms-config: {}", url);

		try {
			FilamentResponse response = restTemplate.getForObject(url, FilamentResponse.class);
			if (response == null) {
				throw new FilamentNotFoundException(id);
			}
			if (response.getStatus() != FilamentResponse.FilamentStatus.ACTIVE) {
				throw new FilamentInactiveException(id);
			}
			return response;
		} catch (HttpClientErrorException.NotFound e) {
			log.warn("Filamento no encontrado en ms-config: {}", id);
			throw new FilamentNotFoundException(id);
		} catch (HttpClientErrorException e) {
			log.error("Error cliente al consultar filamento en ms-config: {}", e.getStatusCode());
			throw new ConfigServiceUnavailableException("Error al comunicarse con ms-config: " + e.getStatusCode());
		} catch (HttpServerErrorException e) {
			log.error("Error servidor en ms-config: {}", e.getStatusCode());
			throw new ConfigServiceUnavailableException("Error en ms-config: " + e.getStatusCode());
		} catch (ResourceAccessException e) {
			log.error("Error de conexión con ms-config: {}", e.getMessage());
			throw new ConfigServiceUnavailableException("No es posible conectar con ms-config");
		}
	}

	public PrintingConfigResponse getPrintingConfig() {
		String url = baseUrl + "/api/v1/config/printing";
		log.debug("Consultando configuración de impresión en ms-config: {}", url);

		try {
			PrintingConfigResponse response = restTemplate.getForObject(url, PrintingConfigResponse.class);
			if (response == null) {
				throw new ConfigServiceUnavailableException("Configuración de impresión no disponible");
			}
			return response;
		} catch (HttpClientErrorException e) {
			log.error("Error cliente al consultar configuración en ms-config: {}", e.getStatusCode());
			throw new ConfigServiceUnavailableException("Error al comunicarse con ms-config: " + e.getStatusCode());
		} catch (HttpServerErrorException e) {
			log.error("Error servidor en ms-config: {}", e.getStatusCode());
			throw new ConfigServiceUnavailableException("Error en ms-config: " + e.getStatusCode());
		} catch (ResourceAccessException e) {
			log.error("Error de conexión con ms-config: {}", e.getMessage());
			throw new ConfigServiceUnavailableException("No es posible conectar con ms-config");
		}
	}
}
