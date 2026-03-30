package it.manage.orders.service;

import it.auth.security.starter.service.AuthService;
import it.manage.orders.dto.OrderDTO;
import it.manage.orders.entity.Order;
import it.manage.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Servizio per la gestione degli ordini.
 * <p>
 * Questa classe fornisce metodi per inviare ordini tramite Kafka.
 * </p>
 *
 * <ul>
 *   <li>Invia ordini al topic Kafka configurato.</li>
 * </ul>
 *
 * @author Antonio Basileo
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final AuthService authService;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderDTO> kafkaTemplate;

    @Value("${spring.kafka.topics}")
    private String kafkaTopic;


    /**
     * Invia un ordine al topic Kafka su una partizione specifica.
     *
     * @param order l'ordine da inviare
     */
    public void sendOrder(OrderDTO order) {
        String key = UUID.randomUUID().toString();
        order.setUsername(authService.getAuthenticatedUser().getUsername());

        ProducerRecord<String, OrderDTO> record = new ProducerRecord<>(
                kafkaTopic,
                0,
                key,
                order
        );

        kafkaTemplate.send(record);
    }

    /**
     * Recupera tutti gli ordini dell'utente autenticato.
     *
     * @return lista degli ordini dell'utente autenticato
     */
    public java.util.List<Order> getOrdersForAuthenticatedUser() {
        return orderRepository.findByManageOrdersUserUsername(authService.getAuthenticatedUser().getUsername());
    }
}
