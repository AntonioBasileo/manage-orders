package it.subito.orders.consumer;

import it.subito.orders.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SubitoConsumerListener {

    @KafkaListener(topics = "${spring.kafka.consumer.topic}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "subitoListenerContainerFactory")
    public void listen(List<ConsumerRecord<String, Order>> messages) {
        messages.forEach(message -> log.info("Received message: {}", message));
    }
}
