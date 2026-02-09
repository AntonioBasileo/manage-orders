package it.manage.orders.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.consumer.topic}")
    private String kafkaTopic;

    /**
     * Crea il topic Kafka per gli ordini.
     * <p>
     * Configurazione:
     * - 1 partizione (sufficiente per un ambiente locale/test)
     * - 1 replication factor (compatibile con un singolo broker Kafka)
     * </p>
     *
     * @return NewTopic configurato per gli ordini
     */
    @Bean
    public NewTopic ordersTopic() {
        return new NewTopic(kafkaTopic, 2, (short) 1);
    }
}
