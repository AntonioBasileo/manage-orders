package it.manage.orders.controller;

import it.manage.orders.dto.ProductDTO;
import it.manage.orders.mapper.ProductMapper;
import it.manage.orders.service.ProductService;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Contract;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static it.manage.orders.utility.Constants.ROLE_ADMIN;
import static it.manage.orders.utility.Constants.ROLE_USER;

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
