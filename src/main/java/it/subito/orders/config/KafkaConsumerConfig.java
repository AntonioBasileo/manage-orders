package it.subito.orders.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.subito.orders.entity.Order;
import it.subito.orders.utility.CustomeDeserializer;
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

@Configuration
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KafkaConsumerConfig {

    private final ObjectMapper objectMapper;
    private final KafkaProperties kafkaProperties;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServer;


    public Map<String, Object> props() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();

        //CONSUMER PROPS
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CustomeDeserializer.class);

        return props;
    }

    @Bean("subitoListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Order> subitoListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Order> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(subitoConsumerFactory(props()));
        factory.setConcurrency(3);
        factory.setBatchListener(true);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000, 3)));

        return factory;
    }

    @Bean
    public ConsumerFactory<String, Order> subitoConsumerFactory(Map<String, Object> props) {
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new CustomeDeserializer());
    }
}
