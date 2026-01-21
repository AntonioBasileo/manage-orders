// src/main/java/it/subito/orders/mapper/OrderMapper.java
package it.subito.orders.mapper;

import it.subito.orders.dto.OrderDTO;
import it.subito.orders.entity.Order;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = ProductMapper.class)
public interface OrderMapper {
    OrderDTO toDto(Order order);

    Order toEntity(OrderDTO orderDTO);

    List<OrderDTO> toDto(List<Order> order);

    List<Order> toEntity(List<OrderDTO> orderDTO);
}
