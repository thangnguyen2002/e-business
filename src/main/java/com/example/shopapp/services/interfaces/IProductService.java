package com.example.shopapp.services.interfaces;

import com.example.shopapp.dtos.ProductDTO;
import com.example.shopapp.dtos.ProductImageDTO;
import com.example.shopapp.models.Product;
import com.example.shopapp.models.ProductImage;
import com.example.shopapp.responses.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IProductService {
    Product createProduct(ProductDTO productDTO) throws Exception;
    Product getProductById(Long productId) throws Exception;
    List<Product> findProductsByIds(List<Long> productIds);

//    Page<ProductResponse> getAllProducts(PageRequest pageRequest);
    Page<ProductResponse> getAllProducts(Long categoryId, String keyword, PageRequest pageRequest);

    Product updateProduct(Long productId, ProductDTO productDTO) throws Exception;
    void deleteProduct(Long productId);
    Boolean existsByName(String name);
    ProductImage createProductImage(
            Long productId,
            ProductImageDTO productImageDTO) throws Exception;

    void deleteFile(String filename) throws IOException;
    String storeFile(MultipartFile file) throws IOException;
}
