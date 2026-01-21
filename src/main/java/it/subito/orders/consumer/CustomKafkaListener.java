package it.subito.orders.consumer;

import it.subito.orders.entity.Order;
import it.subito.orders.repository.OrderRepository;
import it.subito.orders.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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


    /**
     * Listener Kafka per la ricezione batch di ordini.
     * <p>
     * Per ogni messaggio ricevuto, associa l'utente autenticato all'ordine
     * e lo salva nel database.
     * </p>
     *
     * @param messages lista di record Kafka contenenti ordini
     */
    @KafkaListener(topics = "${spring.kafka.consumer.topic}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "subitoListenerContainerFactory")
    public void listen(List<ConsumerRecord<String, Order>> messages) {
        messages.forEach(message -> {
            log.info("Received message from kafka producer: {}", message);
            orderRepository.save(message.value());
        });
    }
}
