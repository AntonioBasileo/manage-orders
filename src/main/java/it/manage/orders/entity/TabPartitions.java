package it.manage.orders.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TabPartitions {

    @Id
    private String partitionKey;

    private String tableName;

    @CreationTimestamp
    private Long creationDate;
}
