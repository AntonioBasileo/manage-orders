package it.subito.orders.config;

import it.subito.orders.entity.Order;
import it.subito.orders.utility.CustomSerializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServer;


    public Map<String, Object> props() {
        Map<String, Object> props = kafkaProperties.buildProducerProperties();

        //PRODUCER PROPS
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CustomSerializer.class);

        return props;
    }

    @Bean
    public ProducerFactory<String, Order> producerFactory() {
        return new DefaultKafkaProducerFactory<>(props());
    }

    @Bean
    @Qualifier("kafkaTemplate")
    public KafkaTemplate<String, Order> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
