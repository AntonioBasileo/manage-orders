package it.subito.orders.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDTO {
    private String code;
    private String description;
    private Long quantity;
    private Long discount;
    private BigDecimal price;
    private Integer vat;
}
