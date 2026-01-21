package it.subito.orders.service;

import it.subito.orders.entity.Product;
import it.subito.orders.repository.ProductRepository;
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
 * @author antonio-basileo_Alten
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
