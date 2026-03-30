package it.manage.orders.config;

import it.manage.orders.dto.OrderDTO;
import it.manage.orders.utility.CustomDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.util.List;
import java.util.Map;

/**
 * Configurazione centralizzata del consumer Kafka del servizio manage-orders.
 * <p>
 * Questa classe ha il compito di trasformare le proprietà dichiarate su configurazione Spring
 * (namespace {@code spring.kafka.*} e {@code app.kafka.*}) nei bean runtime utilizzati dai
 * listener Kafka dell'applicazione.
 * In particolare:
 * <ul>
 *   <li>Parte dalle proprietà standard esposte da {@link KafkaProperties};</li>
 *   <li>Applica alcuni override espliciti per consumer group, bootstrap servers, polling e commit;</li>
 *   <li>Configura la deserializzazione Avro in modalità {@link GenericRecord};</li>
 *   <li>Espone la {@link ConcurrentKafkaListenerContainerFactory} referenziata dal listener
 *       {@code @KafkaListener} di {@code CustomKafkaListener}.</li>
 * </ul>
 * <p>
 * La gestione dei retry non blocking e della dead letter topic non è definita qui, ma nella
 * configurazione dedicata {@code KafkaRetryConfig}. Questa classe si occupa invece del solo
 * comportamento del consumer principale e del relativo container Spring Kafka.
 *
 * @author Antonio Basileo
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;
    private final CustomDeserializer customDeserializer;

    @Value("${spring.kafka.bootstrap-servers}")
    private List<String> kafkaBootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.reconnect-backoff-ms}")
    private long reconnectBackoffMs;

    @Value("${spring.kafka.consumer.reconnect-backoff-max-ms}")
    private long reconnectBackoffMaxMs;

    @Value("${spring.kafka.consumer.socket-connection-setup-timeout-ms}")
    private long socketConnectionSetupTimeoutMs;

    @Value("${spring.kafka.consumer.socket-connection-setup-timeout-max-ms}")
    private long socketConnectionSetupTimeoutMaxMs;

    @Value("${spring.kafka.consumer.max-poll-records}")
    private int maxPollRecords;

    @Value("${spring.kafka.consumer.concurrency}")
    private int concurrency;


    /**
     * Costruisce la mappa finale delle proprietà del consumer Kafka.
     * <p>
     * La configurazione parte da {@link KafkaProperties#buildConsumerProperties()},
     * quindi eredita tutto ciò che è stato definito in {@code application.yaml}; successivamente
     * vengono forzate o completate alcune proprietà operative richieste dal servizio.
     * Tra le più rilevanti:
     * <ul>
     *   <li>{@code group.id} e {@code bootstrap.servers};</li>
     *   <li>Parametri di riconnessione e timeout socket verso il cluster;</li>
     *   <li>{@code max.poll.records};</li>
     *   <li>{@code enable.auto.commit=false}, per demandare il commit della posizione al flusso gestito da Spring Kafka;</li>
     *   <li>{@code auto.offset.reset=earliest}, usato quando per il gruppo non esiste ancora un offset valido;</li>
     *   <li>{@link ErrorHandlingDeserializer} come wrapper dei deserializer reali;</li>
     *   <li>{@code KafkaAvroDeserializer} come deserializer concreto di key e value.</li>
     * </ul>
     * <p>
     * L'uso di {@link ErrorHandlingDeserializer} consente di intercettare correttamente gli errori
     * di deserializzazione e delegarne la gestione alla strategia configurata in Spring Kafka,
     * evitando che il polling si blocchi con una {@code SerializationException} non gestita.
     * <p>
     * Sia la chiave che il valore vengono deserializzati come {@link GenericRecord}; ciò implica
     * che il consumer si aspetta payload Avro compatibili con lo Schema Registry configurato.
     *
     * @return mappa finale delle proprietà usata dal {@link ConsumerFactory}
     */
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();

        props.put("reconnect.backoff.ms", reconnectBackoffMs);
        props.put("reconnect.backoff.max.ms", reconnectBackoffMaxMs);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put("socket.connection.setup.timeout.ms", socketConnectionSetupTimeoutMs);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, String.valueOf(maxPollRecords));
        props.put("socket.connection.setup.timeout.max.ms", socketConnectionSetupTimeoutMaxMs);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return props;
    }

    /**
     * Crea il {@link ConcurrentKafkaListenerContainerFactory} utilizzato dai listener del modulo.
     * <p>
     * Il bean viene pubblicato con nome esplicito
     * {@code manageOrdersListenerContainerFactory} ed è referenziato dal metodo
     * {@code consume(...)} del consumer principale tramite l'attributo
     * {@code containerFactory} di {@code @KafkaListener}.
     * <p>
     * Configurazione applicata:
     * <ul>
     *   <li>{@code concurrency}: numero di thread consumer in parallelo;</li>
     *   <li>{@code batchListener=false}: il listener riceve un record alla volta, non liste di record;</li>
     *   <li>{@link ConsumerFactory} costruita sulle proprietà restituite da {@link #consumerConfigs()}.</li>
     * </ul>
     * <p>
     * Aumentare la concorrenza consente a più partizioni di essere elaborate in parallelo, ma non
     * aumenta il parallelismo oltre il numero di partizioni assegnabili al consumer group.
     * L'ordine dei messaggi resta garantito solo all'interno della singola partizione.
     *
     * @return factory configurata per listener che consumano chiavi e valori Avro come {@link GenericRecord}
     */
    @Bean("manageOrdersListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, OrderDTO> listenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(concurrency);
        factory.setBatchListener(false);
        factory.setConsumerFactory(consumerFactory(consumerConfigs()));

        return factory;
    }

    /**
     * Crea la {@link ConsumerFactory} da cui Spring Kafka istanzia i consumer runtime.
     *
     * @param props proprietà già finalizzate del consumer, comprensive di bootstrap servers,
     *              group id, timeout e deserializer key/value
     * @return factory configurata per deserializzare messaggi Avro come {@link GenericRecord}
     */
    @Bean
    public DefaultKafkaConsumerFactory<String, OrderDTO> consumerFactory(Map<String, Object> props) {
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), customDeserializer);
    }
}
