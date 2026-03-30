package it.manage.orders.config;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import it.manage.orders.dto.OrderDTO;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.messaging.converter.MessageConversionException;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Configurazione centralizzata della strategia di retry per i listener Kafka del modulo.
 * <p>
 * Crea una {@link RetryTopicConfiguration} basata su backoff esponenziale e numero massimo
 * di tentativi letti da configurazione ({@code spring.kafka.retry.*}).
 * I messaggi che esauriscono i tentativi vengono pubblicati sulla DLT tramite suffisso {@code -dlt}.
 *
 * @author Antonio Basileo
 */
@Configuration
public class KafkaRetryConfig {

    @Value("${spring.kafka.topics}")
    private String topicName;

    @Value("${spring.kafka.retry.max-attempts}")
    private int maxAttempts;

    @Value("${spring.kafka.retry.initial-interval-ms}")
    private long initialIntervalMs;

    @Value("${spring.kafka.retry.multiplier}")
    private double multiplier;

    @Value("${spring.kafka.retry.max-interval-ms}")
    private long maxIntervalMs;

    private List<Class<? extends Throwable>> retryableExceptions = List.of(
            NoSuchElementException.class,
            DeserializationException.class,
            SerializationException.class,
            MessageConversionException.class,
            ClassCastException.class,
            InvalidDefinitionException.class
    );


    /**
     * Costruisce la configurazione retry/DLT per la topic di ingresso degli ordini.
     * <p>
     * La configurazione applica:
     * <ul>
     *   <li>backoff esponenziale ({@code initialIntervalMs}, {@code multiplier}, {@code maxIntervalMs});</li>
     *   <li>Massimo tentativi complessivi ({@code maxAttempts});</li>
     *   <li>Suffissi topic di retry ({@code -retry}) e dead-letter ({@code -dlt});</li>
     *   <li>Esclusione dal retry per eccezioni non recuperabili.</li>
     * </ul>
     *
     * @param template template Kafka usato dal framework per pubblicare record su topic retry/DLT
     * @return configurazione retry topic applicata alla topic indicata da {@code app.topics.orders.inputName}
     */
    @Bean
    public RetryTopicConfiguration manageOrdersRetryConfig(@Qualifier("kafkaTemplate") KafkaTemplate<String, OrderDTO> template) {
        return RetryTopicConfigurationBuilder
                .newInstance()
                .dltHandlerMethod("customKafkaListener", "handleDltMessage")
                .exponentialBackoff(initialIntervalMs, multiplier, maxIntervalMs)
                .maxAttempts(maxAttempts)
                .includeTopic(topicName)
                .notRetryOn(retryableExceptions)
                .retryTopicSuffix("-retry")
                .dltSuffix("-dlt")
                .create(template);
    }
}
