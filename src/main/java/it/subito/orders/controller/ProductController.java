package it.subito.orders.controller;

import it.subito.orders.entity.Product;
import it.subito.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final OrderService orderService;

    @GetMapping("/get-all")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(orderService.getAllProducts());
    }
}
