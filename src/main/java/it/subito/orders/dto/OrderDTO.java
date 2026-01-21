package it.subito.orders.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class OrderDTO {
    private Long id;
    private Set<ProductDTO> products;
    private String status;
    private Integer vat;
    private BigDecimal totalAmount;
}
