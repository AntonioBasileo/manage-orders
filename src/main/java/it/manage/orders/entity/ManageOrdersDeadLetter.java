package it.manage.orders.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Table
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
    @Column(nullable = false)
    private Long id;

    @Column
    private String originalKey;

    @Column(nullable = false)
    private String receivedTopic;

    @Column(columnDefinition = "LONGTEXT")
    private String deadLetterMessage;

    @Column
    private String originalTopic;

    @Column
    private Integer originalPartition;

    @Column
    private Long originalOffset;

    @Column
    private String exceptionClass;

    @Column
    private String exceptionMessage;

    @Builder.Default
    @Column(nullable = false)
    private Integer retryCount = 0;

    @Column
    private String lastReprocessingError;

    @Column
    private Boolean processed;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
