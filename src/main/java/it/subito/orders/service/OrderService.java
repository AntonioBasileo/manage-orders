package it.subito.orders.service;

import it.subito.orders.entity.Order;
import it.subito.orders.entity.Product;
import it.subito.orders.repository.OrderRepository;
import it.subito.orders.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Servizio per la gestione degli ordini.
 * <p>
 * Questa classe fornisce metodi per inviare ordini tramite Kafka.
 * </p>
 *
 * <ul>
 *   <li>Invia ordini al topic Kafka configurato.</li>
 * </ul>
 *
 * @author antonio-basileo_Alten
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final AuthService authService;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Order> kafkaTemplate;


    /**
     * Invia un ordine al topic Kafka.
     *
     * @param order l'ordine da inviare
     */
    public void sendOrder(Order order) {
        order.setAppUser(authService.getAuthenticatedUser());

        Set<Product> products = order.getProducts();

        for (Product product : products) {
            if (product.getQuantity() == 0)
                throw new IllegalArgumentException(String.format("Product %s sold out", product.getCode()));

            product.setQuantity(product.getQuantity() - 1);

            productRepository.save(product);
        }

        products.forEach(product -> product.setQuantity(product.getQuantity() - 1));
        kafkaTemplate.send("topic-orders", UUID.randomUUID().toString(), order);
    }

    public List<Order> getOrdersForAuthenticatedUser() {
        return orderRepository.findByAppUserUsername(authService.getAuthenticatedUser().getUsername());
    }
}
