// ProductMapper.java
package it.manage.orders.mapper;

import it.manage.orders.entity.Product;
import it.manage.orders.dto.ProductDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDTO toDto(Product product);

    Product toEntity(ProductDTO productDTO);

    List<ProductDTO> toDto(List<Product> product);

    List<Product> toEntity(List<ProductDTO> productDTO);
}
