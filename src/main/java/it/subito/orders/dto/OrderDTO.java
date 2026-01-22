package it.subito.orders.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {
    private Long id;
    private Set<ProductDTO> products;
    private String status;
    private Integer vat;
    private BigDecimal totalAmount;
}
