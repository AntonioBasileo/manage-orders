package it.manage.orders.consumer;

import it.auth.security.starter.service.AuthService;
import it.manage.orders.dto.OrderDTO;
import it.manage.orders.entity.Order;
import it.manage.orders.entity.Product;
import it.manage.orders.mapper.OrderMapper;
import it.manage.orders.repository.OrderRepository;
import it.manage.orders.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
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
 * @author Antonio Basileo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomKafkaListener {

    private final OrderMapper orderMapper;
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
    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topicPartitions = @TopicPartition(topic = "${spring.kafka.consumer.topic}", partitions = {"0"}),
            containerFactory = "listenerContainerFactory")
    public void listen(List<ConsumerRecord<String, OrderDTO>> messages) {
        for (ConsumerRecord<String, OrderDTO> message : messages) {
            log.info("Received message from kafka producer: {}", message);

            Order order = orderMapper.toEntity(message.value());

            for (Product product : order.getProducts()) {
                String productCode = product.getCode();
                Product productEntity = productRepository.findByCode(productCode).orElseThrow();
                Long discount = productEntity.getDiscount();

                if (discount == 0) {
                    throw new IllegalArgumentException(String.format("Product %s sold out", productCode));
                }

                if (discount < product.getQuantity()) {
                    throw new IllegalArgumentException(String.format("Product %s has only %d left in stock", productCode, discount - product.getQuantity()));
                }

                productEntity.setDiscount(productEntity.getDiscount() - product.getQuantity());
            }

            orderRepository.save(order);
        }
    }
}
