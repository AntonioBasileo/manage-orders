package it.manage.orders.config;

import it.manage.orders.dto.OrderDTO;
import it.manage.orders.entity.Order;
import it.manage.orders.utility.CustomSerializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.List;
import java.util.Map;

/**
 * Configurazione centralizzata del producer Kafka del servizio manage-orders.
 * <p>
 * Il producer è utilizzato esclusivamente dal {@code retryableTopicKafkaTemplate}, referenziato
 * da Spring Kafka per pubblicare messaggi sui topic di retry e sulla dead letter topic (DLT)
 * gestiti da {@link KafkaRetryConfig}.
 * <p>
 * Le proprietà di base (serializzatori Avro, schema registry URL) vengono ereditate da
 * {@code spring.kafka.producer.*} tramite {@link KafkaProperties#buildProducerProperties()};
 * su di esse vengono poi applicati gli override espliciti richiesti dal servizio.
 *
 * @author Antonio Basileo
 */
@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;
    private final CustomSerializer customSerializer;

    @Value("${spring.kafka.bootstrap-servers}")
    private List<String> kafkaBootstrapServers;

    /**
     * Costruisce la mappa finale delle proprietà del producer Kafka.
     * <p>
     * La configurazione parte da {@link KafkaProperties#buildProducerProperties()},
     * quindi eredita serializzatori e schema registry URL dichiarati in {@code application.yaml};
     * successivamente viene forzato l'unico override operativo richiesto:
     * <ul>
     *   <li>{@code bootstrap.servers}: indirizzo esplicito del cluster.</li>
     * </ul>
     * Le proprietà {@code acks}, {@code retries} e {@code delivery.timeout.ms} sono dichiarate
     * direttamente in {@code application.yaml} sotto {@code spring.kafka.producer.properties}.
     *
     * @return mappa finale delle proprietà usata dal {@link ProducerFactory}
     */
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = kafkaProperties.buildProducerProperties();

        //PRODUCER PROPS
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return props;
    }

    /**
     * Crea e configura il producer factory per Kafka.
     *
     * @return factory configurata per la serializzazione di {@link Order}
     */
    @Bean
    public ProducerFactory<String, OrderDTO> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs(), new StringSerializer(), customSerializer);
    }

    /**
     * Crea e configura il KafkaTemplate per l'invio dei messaggi.
     *
     * @return template configurato per l'invio di {@link Order}
     */
    @Bean
    @Qualifier("kafkaTemplate")
    public KafkaTemplate<String, OrderDTO> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
