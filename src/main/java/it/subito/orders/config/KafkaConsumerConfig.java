package it.subito.orders.config;

import it.subito.orders.entity.Order;
import it.subito.orders.utility.CustomDeserializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

/**
 * Configurazione del consumer Kafka per l'applicazione.
 * <p>
 * Questa classe definisce le proprietà e i bean necessari per la ricezione di messaggi Kafka,
 * inclusa la deserializzazione personalizzata degli ordini e la gestione degli errori.
 * </p>
 *
 * <ul>
 *   <li>Imposta le proprietà del consumer tramite i valori di configurazione.</li>
 *   <li>Configura il listener container factory per la ricezione batch e la gestione della concorrenza.</li>
 *   <li>Utilizza {@link CustomDeserializer} per deserializzare i messaggi in oggetti {@link Order}.</li>
 * </ul>
 *
 * @author antonio-basileo_Alten
 */
@Configuration
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServer;


    /**
     * Restituisce le proprietà di configurazione del consumer Kafka.
     *
     * @return mappa delle proprietà del consumer
     */
    public Map<String, Object> props() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();

        //CONSUMER PROPS
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CustomDeserializer.class);

        return props;
    }

    /**
     * Crea e configura il listener container factory per Kafka.
     *
     * @return factory configurata per la ricezione di messaggi {@link Order}
     */
    @Bean("subitoListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Order> subitoListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Order> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(subitoConsumerFactory(props()));
        factory.setConcurrency(3);
        factory.setBatchListener(true);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000, 3)));

        return factory;
    }

    /**
     * Crea e configura il consumer factory per Kafka.
     *
     * @param props proprietà del consumer
     * @return factory configurata per la deserializzazione di {@link Order}
     */
    @Bean
    public ConsumerFactory<String, Order> subitoConsumerFactory(Map<String, Object> props) {
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new CustomDeserializer());
    }
}
