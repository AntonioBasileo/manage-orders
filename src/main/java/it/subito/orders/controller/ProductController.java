package it.subito.orders.controller;

import it.subito.orders.dto.ProductDTO;
import it.subito.orders.mapper.ProductMapper;
import it.subito.orders.service.ProductService;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static it.subito.orders.utility.Constants.ROLE_USER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductMapper productMapper;
    private final ProductService productService;


    @RolesAllowed(ROLE_USER)
    @GetMapping("/get-all")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productMapper.toDto(productService.getAllProducts()));
    }
}
