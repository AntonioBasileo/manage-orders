package it.manage.orders.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.manage.orders.dto.OrderDTO;
import it.manage.orders.entity.ManageOrdersDeadLetter;
import it.manage.orders.entity.ManageOrdersUser;
import it.manage.orders.entity.Order;
import it.manage.orders.entity.Product;
import it.manage.orders.mapper.OrderMapper;
import it.manage.orders.repository.DeadLetterRepository;
import it.manage.orders.repository.OrderRepository;
import it.manage.orders.repository.ProductRepository;
import it.manage.orders.repository.UserRepository;
import it.manage.orders.utility.DltPayloadUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;


/**
 * Consumer Kafka per la ricezione e la gestione degli ordini.
 * <p>
 * Questa classe intercetta i messaggi provenienti dal topic Kafka configurato,
 * elabora il record Avro ricevuto e gestisce i messaggi finiti in dead letter.
 * </p>
 *
 * @author Antonio Basileo
 */
@Service("customKafkaListener")
@Slf4j
@RequiredArgsConstructor
public class CustomKafkaListener {

    private final ObjectMapper objectMapper;
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final DeadLetterRepository deadLetterRepository;
    private final UserRepository userRepository;

    public static final String LISTENER_ID = "manage-orders-listener";


    @KafkaListener(
        id = LISTENER_ID,
        idIsGroup = false,
        topics = "${spring.kafka.topics}",
        containerFactory = "manageOrdersListenerContainerFactory")
    public void consume(ConsumerRecord<String, OrderDTO> consumerRecord) {
        log.debug("consume({})", consumerRecord);
        log.debug("KEY:{}\tVALUE:{}", consumerRecord.key(), consumerRecord.value());

        Optional<OrderDTO> value = Optional.ofNullable(consumerRecord.value());

        if (value.isEmpty()) {
            log.warn("Received tombstone for key: '{}'", consumerRecord.key());
            return;
        }

        this.processOrder(value.get());
    }

    @DltHandler
    public void handleDltMessage(
        ConsumerRecord<?, ?> consumerRecord,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String receivedTopic,
        @Header(value = KafkaHeaders.ORIGINAL_TOPIC, required = false) String originalTopic,
        @Header(value = KafkaHeaders.ORIGINAL_PARTITION, required = false) Integer originalPartition,
        @Header(value = KafkaHeaders.ORIGINAL_OFFSET, required = false) Long originalOffset,
        @Header(value = KafkaHeaders.EXCEPTION_CAUSE_FQCN, required = false) String exceptionClass,
        @Header(value = KafkaHeaders.EXCEPTION_STACKTRACE, required = false) String exceptionMessage) {
        log.info("Message received on Dead Letter Topic. Info: receivedTopic='{}', key='{}', originalTopic='{}', originalPartition='{}', originalOffset='{}', exceptionClass='{}', exceptionMessage='{}'",
            receivedTopic,
            consumerRecord.key(),
            originalTopic,
            originalPartition,
            originalOffset,
            exceptionClass,
            exceptionMessage);

        deadLetterRepository.save(
            ManageOrdersDeadLetter.builder()
                .originalKey(deadLetterPayloadToString(consumerRecord.key()))
                .deadLetterMessage(deadLetterPayloadToString(consumerRecord.value()))
                .receivedTopic(receivedTopic)
                .originalTopic(originalTopic)
                .originalPartition(originalPartition)
                .originalOffset(originalOffset)
                .exceptionClass(exceptionClass)
                .exceptionMessage(exceptionMessage)
                .processed(false)
                .build());

        log.info("Dead letter message successfully saved on DB.");
    }

    private String deadLetterPayloadToString(Object payload) {
        switch (payload) {
            case null -> {
                return null;
            }
            case byte[] bytes -> {
                return DltPayloadUtils.normalizeDltPayload(new String(bytes, StandardCharsets.UTF_8));
            }
            case String str -> {
                return DltPayloadUtils.normalizeDltPayload(str);
            }
            default -> {
            }
        }

        try {
            return DltPayloadUtils.normalizeDltPayload(objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            log.warn("Unable to serialize DLT payload to JSON, falling back to toString: {}", e.getMessage());
            return DltPayloadUtils.normalizeDltPayload(payload.toString());
        }
    }

    private void processOrder(OrderDTO dto) {
        log.info("Processing order: {}", dto);

        toEntityOrder(dto, orderMapper, productRepository, orderRepository);
        log.info("Order processed successfully.");
    }

    public void toEntityOrder(OrderDTO dto, OrderMapper orderMapper, ProductRepository productRepository, OrderRepository orderRepository) {
        Order order = orderMapper.toEntity(dto);
        order.setManageOrdersUser((ManageOrdersUser) userRepository.findByUsername(dto.getUsername()).orElseThrow());

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
