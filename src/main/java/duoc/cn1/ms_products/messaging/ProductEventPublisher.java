package duoc.cn1.ms_products.messaging;

import duoc.cn1.ms_products.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventPublisher {

	private final RabbitTemplate rabbitTemplate;
	private final TopicExchange eventsExchange;

	@Value("${rabbitmq.routing-keys.product-created}")
	private String productCreatedRoutingKey;

	public void publishProductCreated(ProductCreatedEvent event) {
		log.info("Publicando evento product.created para producto ID: {}", event.getProductId());
		rabbitTemplate.convertAndSend(eventsExchange.getName(), productCreatedRoutingKey, event);
		log.info("Evento product.created publicado correctamente");
	}
}
