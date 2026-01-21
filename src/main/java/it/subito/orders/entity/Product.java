package it.subito.orders.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    private String code;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    private Long quantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private Integer vat;
}
