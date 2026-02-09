package it.manage.orders.service;

import it.manage.orders.entity.Product;
import it.manage.orders.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servizio per la gestione dei prodotti.
 * <p>
 * Questa classe fornisce metodi per recuperare la lista dei prodotti disponibili
 * tramite il repository dedicato.
 * </p>
 *
 * <ul>
 *   <li>Recupera tutti i prodotti dal repository.</li>
 * </ul>
 *
 * @author Antonio Basileo
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;


    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product addProduct(Product product) {
        return productRepository.save(product);
    }
}
