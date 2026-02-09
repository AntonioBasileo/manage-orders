package it.manage.orders.consumer;

import it.manage.orders.entity.Order;
import it.manage.orders.entity.Product;
import it.manage.orders.repository.OrderRepository;
import it.manage.orders.repository.ProductRepository;
import it.manage.orders.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Listener Kafka personalizzato per la ricezione e la gestione degli ordini.
 * <p>
 * Questa classe intercetta i messaggi provenienti dal topic Kafka configurato,
 * associa l'utente autenticato all'ordine ricevuto e lo salva nel repository.
 * </p>
 *
 * <ul>
 *   <li>Utilizza {@link AuthService} per ottenere l'utente autenticato.</li>
 *   <li>Salva gli ordini tramite {@link OrderRepository}.</li>
 *   <li>Logga i messaggi ricevuti per tracciabilità.</li>
 * </ul>
 *
 * @author antonio-basileo_Alten
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomKafkaListener {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;


    /**
     * Listener Kafka per la ricezione batch di ordini.
     * <p>
     * Per ogni messaggio ricevuto, associa l'utente autenticato all'ordine
     * e lo salva nel database.
     * </p>
     *
     * @param messages lista di record Kafka contenenti ordini
     */
    @Transactional(rollbackFor = Exception.class)
    @KafkaListener(topics = "${spring.kafka.consumer.topic}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "listenerContainerFactory")
    public void listen(List<ConsumerRecord<String, Order>> messages) {
        for (ConsumerRecord<String, Order> message : messages) {
            log.info("Received message from kafka producer: {}", message);

            Order order = message.value();

            for (Product product : order.getProducts()) {
                String productCode = product.getCode();
                Product productEntity = productRepository.findByCode(productCode).orElseThrow();
                Long discount = productEntity.getDiscount();

                if (discount < product.getQuantity() || discount == 0) {
                    throw new IllegalArgumentException(String.format("Product %s sold out", productCode));
                }

                productEntity.setDiscount(productEntity.getDiscount() - product.getQuantity());
            }

            orderRepository.save(order);
        }
    }
}
