package it.manage.orders.scheduling;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.manage.orders.consumer.CustomKafkaListener;
import it.manage.orders.dto.OrderDTO;
import it.manage.orders.entity.ManageOrdersDeadLetter;
import it.manage.orders.mapper.OrderMapper;
import it.manage.orders.repository.DeadLetterRepository;
import it.manage.orders.repository.OrderRepository;
import it.manage.orders.repository.ProductRepository;
import it.manage.orders.utility.DltPayloadUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servizio applicativo che riprocessa periodicamente i messaggi finiti in dead letter e salvati su database.
 * <p>
 * Il servizio lavora sui record della tabella {@code t_manage_orders_dead_letter},
 * popolata dal consumer principale quando un messaggio viene instradato sulla DLT e gestito
 * dal relativo {@code @DltHandler}. Il payload persistito viene letto come JSON e ricostruito
 * in un {@link OrderDTO}, quindi rielaborato come se fosse un messaggio ricevuto correttamente
 * dal flusso principale.
 * <p>
 * La selezione dei record da trattare avviene secondo questi criteri:
 * <ul>
 *   <li>Il record non deve risultare ancora processato ({@code processed = false});</li>
 *   <li>Il numero di tentativi di riprocessamento deve essere inferiore al limite configurato;</li>
 *   <li>Il numero massimo di record per singola esecuzione è limitato da property.</li>
 * </ul>
 * <p>
 * Il job è schedulato a intervallo fisso e viene eseguito in transazione: le modifiche allo
 * stato dei record JPA caricati dal repository vengono quindi persistite automaticamente a fine
 * esecuzione tramite dirty checking. In caso di successo il record viene marcato come processato;
 * in caso di errore viene incrementato il contatore dei retry e viene memorizzato l'ultimo errore.
 * <p>
 * Proprietà configurabili principali:
 * <ul>
 *   <li>{@code spring.kafka.dlt.reprocessing.interval-ms}: intervallo tra due esecuzioni del job;</li>
 *   <li>{@code spring.kafka.dlt.reprocessing.max-per-run}: numero massimo di record trattati per esecuzione;</li>
 *   <li>{@code spring.kafka.dlt.reprocessing.max-attempts}: numero massimo di tentativi di riprocessamento per record.</li>
 * </ul>
 *
 * @author Antonio Basileo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DltReprocessingService {

    private final ObjectMapper objectMapper;
    private final DeadLetterRepository deadLetterRepository;
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Value("${spring.kafka.dlt.reprocessing.max-per-run:100}")
    private int maxPerRun;

    @Value("${spring.kafka.dlt.reprocessing.max-attempts:5}")
    private int maxAttempts;


    /**
     * Esegue il riprocessamento periodico dei record DLT ancora pendenti.
     * <p>
     * Il metodo viene invocato automaticamente dallo scheduler Spring con due tempi distinti:
     * <ul>
     *   <li>La prima esecuzione parte dopo {@code spring.kafka.dlt.reprocessing.initial-delay-ms};</li>
     *   <li>Le esecuzioni successive usano {@code fixedDelay} ({@code spring.kafka.dlt.reprocessing.interval-ms})</li>
     * </ul>
     * A ogni run:
     * <ol>
     *   <li>Estrae dal database un batch di record non processati con {@code retryCount < maxAttempts};</li>
     *   <li>Deserializza il contenuto JSON in {@link OrderDTO};</li>
     *   <li>Invoca il processo di business;</li>
     *   <li>Se l'elaborazione va a buon fine, marca il record come processato e pulisce l'ultimo errore;</li>
     *   <li>Se l'elaborazione fallisce, incrementa il contatore dei tentativi e salva il messaggio di errore.</li>
     * </ol>
     */
    @Transactional
    @Scheduled(fixedDelayString = "${spring.kafka.dlt.reprocessing.interval-ms:600000}",
            initialDelayString = "${spring.kafka.dlt.reprocessing.initial-delay-ms:300000}")
    public void reprocessPendingMessages() {
        log.info("DLT reprocessing: inizio esecuzione.");
        List<ManageOrdersDeadLetter> pending = deadLetterRepository
                .findByProcessedFalseAndRetryCountLessThan(maxAttempts, PageRequest.of(0, maxPerRun));

        if (pending.isEmpty()) {
            log.info("DLT reprocessing: nessun messaggio da processare.");
            return;
        }

        log.info("DLT reprocessing: trovati {} messaggi da riprocessare.", pending.size());

        for (ManageOrdersDeadLetter dl : pending) {
            try {
                String normalizedPayload = DltPayloadUtils.normalizeDltPayload(
                        DltPayloadUtils.unescapeIfDoubleEncoded(dl.getDeadLetterMessage()));
                dl.setDeadLetterMessage(normalizedPayload);

                OrderDTO dto = objectMapper.readValue(normalizedPayload, OrderDTO.class);
                log.info("DLT record id={} in attesa di riprocessamento.", dl.getId());

                CustomKafkaListener.toEntityOrder(dto, orderMapper, productRepository, orderRepository);

                dl.setProcessed(true);
                dl.setLastReprocessingError(null);
                log.info("DLT record id={} riprocessato con successo.", dl.getId());
            } catch (Exception e) {
                int newCount = dl.getRetryCount() + 1;

                dl.setRetryCount(newCount);
                dl.setLastReprocessingError(DltPayloadUtils.sanitizePersistableText(e.getMessage()));
                dl.setExceptionClass(DltPayloadUtils.sanitizePersistableText(e.getClass().getName()));

                log.error("DLT record id={} fallito al tentativo {}/{}: {}",
                        dl.getId(), newCount, maxAttempts, DltPayloadUtils.sanitizePersistableText(e.getMessage()));
            }
        }
    }
}
