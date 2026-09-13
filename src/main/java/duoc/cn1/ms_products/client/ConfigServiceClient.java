package duoc.cn1.ms_products.client;

import duoc.cn1.ms_products.client.dto.FilamentResponse;
import duoc.cn1.ms_products.client.dto.PrintingConfigResponse;
import duoc.cn1.ms_products.exception.ConfigServiceUnavailableException;
import duoc.cn1.ms_products.exception.FilamentInactiveException;
import duoc.cn1.ms_products.exception.FilamentNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigServiceClient {

    private final RestTemplate restTemplate;

    @Value("${ms-config.base-url}")
    private String baseUrl;

    public FilamentResponse getFilamentById(Long id) {
        String url = baseUrl + "/api/v1/config/filaments/" + id;
        log.debug("Consultando filamento en ms-config: {}", url);

        try {
            ResponseEntity<FilamentResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createAuthenticatedEntity(),
                    FilamentResponse.class
            );

            FilamentResponse filament = response.getBody();

            if (filament == null) {
                throw new FilamentNotFoundException(id);
            }

            if (filament.getStatus() != FilamentResponse.FilamentStatus.ACTIVE) {
                throw new FilamentInactiveException(id);
            }

            return filament;

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Filamento no encontrado en ms-config: {}", id);
            throw new FilamentNotFoundException(id);

        } catch (HttpClientErrorException e) {
            log.error(
                    "Error cliente al consultar filamento en ms-config: {}",
                    e.getStatusCode()
            );
            throw new ConfigServiceUnavailableException(
                    "Error al comunicarse con ms-config: " + e.getStatusCode()
            );

        } catch (HttpServerErrorException e) {
            log.error(
                    "Error servidor en ms-config: {}",
                    e.getStatusCode()
            );
            throw new ConfigServiceUnavailableException(
                    "Error en ms-config: " + e.getStatusCode()
            );

        } catch (ResourceAccessException e) {
            log.error(
                    "Error de conexión con ms-config: {}",
                    e.getMessage()
            );
            throw new ConfigServiceUnavailableException(
                    "No es posible conectar con ms-config"
            );
        }
    }

    public PrintingConfigResponse getPrintingConfig() {
        String url = baseUrl + "/api/v1/config/printing";
        log.debug(
                "Consultando configuración de impresión en ms-config: {}",
                url
        );

        try {
            ResponseEntity<PrintingConfigResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            createAuthenticatedEntity(),
                            PrintingConfigResponse.class
                    );

            PrintingConfigResponse printingConfig = response.getBody();

            if (printingConfig == null) {
                throw new ConfigServiceUnavailableException(
                        "Configuración de impresión no disponible"
                );
            }

            return printingConfig;

        } catch (HttpClientErrorException e) {
            log.error(
                    "Error cliente al consultar configuración en ms-config: {}",
                    e.getStatusCode()
            );
            throw new ConfigServiceUnavailableException(
                    "Error al comunicarse con ms-config: " + e.getStatusCode()
            );

        } catch (HttpServerErrorException e) {
            log.error(
                    "Error servidor en ms-config: {}",
                    e.getStatusCode()
            );
            throw new ConfigServiceUnavailableException(
                    "Error en ms-config: " + e.getStatusCode()
            );

        } catch (ResourceAccessException e) {
            log.error(
                    "Error de conexión con ms-config: {}",
                    e.getMessage()
            );
            throw new ConfigServiceUnavailableException(
                    "No es posible conectar con ms-config"
            );
        }
    }

    private HttpEntity<Void> createAuthenticatedEntity() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            log.error(
                    "No existe un JWT autenticado disponible para llamar a ms-config"
            );
            throw new ConfigServiceUnavailableException(
                    "No existe un token de autenticación para comunicarse con ms-config"
            );
        }

        String accessToken = jwtAuthentication.getToken().getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        return new HttpEntity<>(headers);
    }
}