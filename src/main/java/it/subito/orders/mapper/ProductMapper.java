// ProductMapper.java
package it.subito.orders.mapper;

import it.subito.orders.entity.Product;
import it.subito.orders.dto.ProductDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDTO toDto(Product product);

    Product toEntity(ProductDTO productDTO);

    List<ProductDTO> toDto(List<Product> product);

    List<Product> toEntity(List<ProductDTO> productDTO);
}
