package com.example.shopapp.services.interfaces;

import com.example.shopapp.dtos.ProductDTO;
import com.example.shopapp.dtos.ProductImageDTO;
import com.example.shopapp.models.Product;
import com.example.shopapp.models.ProductImage;
import com.example.shopapp.responses.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IProductService {
    Product createProduct(ProductDTO productDTO) throws Exception;
    Product getProductById(Long productId) throws Exception;
    Page<ProductResponse> getAllProducts(PageRequest pageRequest);
    Product updateProduct(Long productId, ProductDTO productDTO) throws Exception;
    void deleteProduct(Long productId);
    Boolean existsByName(String name);
    ProductImage createProductImage(
            Long productId,
            ProductImageDTO productImageDTO) throws Exception;
}
