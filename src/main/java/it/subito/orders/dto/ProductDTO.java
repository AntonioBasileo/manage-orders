package it.subito.orders.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {
    private String code;
    private String description;
    private Long quantity;
    private BigDecimal price;
    private Integer vat;
}
