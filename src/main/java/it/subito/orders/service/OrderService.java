package it.subito.orders.service;

import it.subito.orders.entity.Order;
import it.subito.orders.entity.Product;
import it.subito.orders.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Order> kafkaTemplate;


    public void sendOrder(Order order) {
        kafkaTemplate.send("topic-orders", "chiave", order);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
