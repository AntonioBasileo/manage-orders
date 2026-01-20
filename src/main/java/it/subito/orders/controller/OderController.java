package it.subito.orders.controller;

import it.subito.orders.entity.Order;
import it.subito.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OderController {

    private final OrderService orderService;


    @PostMapping("/sendOrder")
    public ResponseEntity<String> sendOrder(@RequestBody Order order) {
        log.info("Received order: {}", order);
        orderService.sendOrder(order);

        return ResponseEntity.ok("Order sent successfully");
    }

}
