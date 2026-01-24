package it.subito.orders.controller;

import it.subito.orders.dto.ProductDTO;
import it.subito.orders.mapper.ProductMapper;
import it.subito.orders.service.ProductService;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Contract;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static it.subito.orders.utility.Constants.ROLE_ADMIN;
import static it.subito.orders.utility.Constants.ROLE_USER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductMapper productMapper;
    private final ProductService productService;


    @Contract(value = "-> !null")
    @RolesAllowed({ROLE_USER, ROLE_ADMIN})
    @GetMapping(value = "/get-all", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productMapper.toDto(productService.getAllProducts()));
    }

    @RolesAllowed(ROLE_ADMIN)
    @Contract(value = "!null -> !null")
    @PostMapping(value = "/add-product", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ProductDTO> addProduct(@RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productMapper.toDto(productService.addProduct(productMapper.toEntity(productDTO))));
    }
}
