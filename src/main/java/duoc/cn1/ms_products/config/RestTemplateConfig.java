package duoc.cn1.ms_products.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

	@Value("${ms-config.connect-timeout-ms:3000}")
	private int connectTimeout;

	@Value("${ms-config.read-timeout-ms:5000}")
	private int readTimeout;

	@Bean
	public RestTemplate restTemplate() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(connectTimeout);
		factory.setReadTimeout(readTimeout);
		return new RestTemplate(factory);
	}
}
