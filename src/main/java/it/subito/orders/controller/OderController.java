package it.subito.orders.controller;

import it.subito.orders.dto.OrderDTO;
import it.subito.orders.mapper.OrderMapper;
import it.subito.orders.service.OrderService;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static it.subito.orders.utility.Constants.ROLE_USER;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OderController {

    private final OrderMapper orderMapper;
    private final OrderService orderService;


    @RolesAllowed(ROLE_USER)
    @PostMapping("/send-order")
    public ResponseEntity<String> sendOrder(@RequestBody OrderDTO order) {
        orderService.sendOrder(orderMapper.toEntity(order));

        return ResponseEntity.ok("Order sent successfully");
    }

    @RolesAllowed(ROLE_USER)
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderDTO>> getMyOrders() {
        return ResponseEntity.ok(orderMapper.toDto(orderService.getOrdersForAuthenticatedUser()));
    }
}
