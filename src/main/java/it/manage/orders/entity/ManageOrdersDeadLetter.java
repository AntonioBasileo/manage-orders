package it.manage.orders.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Table(name = "t_manage_orders_dead_letter")
@DynamicUpdate
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class ManageOrdersDeadLetter implements java.io.Serializable {

    private static final long serialVersionUID = 8135399503529349265L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dead_letter", nullable = false)
    private Long id;

    @Column(name = "chiave_kafka", columnDefinition = "TEXT")
    private String originalKey;

    @Column(name = "received_topic", nullable = false, columnDefinition = "TEXT")
    private String receivedTopic;

    @Column(name = "messaggio_kafka_json", columnDefinition = "TEXT")
    private String deadLetterMessage;

    @Column(name = "topic_iniziale", columnDefinition = "TEXT")
    private String originalTopic;

    @Column(name = "partizione_iniziale")
    private Integer originalPartition;

    @Column(name = "offset_iniziale")
    private Long originalOffset;

    @Column(name = "classe_eccezione", columnDefinition = "TEXT")
    private String exceptionClass;

    @Column(name = "messaggio_eccezione", columnDefinition = "TEXT")
    private String exceptionMessage;

    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "ultimo_errore_reprocessing", columnDefinition = "TEXT")
    private String lastReprocessingError;

    @Column(name = "processed")
    private Boolean processed;

    @CreationTimestamp
    @Column(name = "data_creazione", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "data_ultimo_aggiornamento")
    private LocalDateTime updatedAt;
}
