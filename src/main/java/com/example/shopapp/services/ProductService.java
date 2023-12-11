package com.example.shopapp.services;

import com.example.shopapp.dtos.ProductDTO;
import com.example.shopapp.dtos.ProductImageDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.exceptions.InvalidParamException;
import com.example.shopapp.models.Category;
import com.example.shopapp.models.Product;
import com.example.shopapp.models.ProductImage;
import com.example.shopapp.repositories.CategoryRepository;
import com.example.shopapp.repositories.ProductImageRepository;
import com.example.shopapp.repositories.ProductRepository;
import com.example.shopapp.responses.ProductResponse;
import com.example.shopapp.services.interfaces.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final CategoryRepository categoryRepository;

    @Autowired
    private final ProductImageRepository productImageRepository;

    @Override
    public Product createProduct(ProductDTO productDTO) throws Exception {
        Category existingCategory = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new DataNotFoundException("Cannot find category with id: " + productDTO.getCategoryId()));

        Product newProduct = Product
                .builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .thumbnail(productDTO.getThumbnail())
                .description(productDTO.getDescription())
                .category(existingCategory)
                .build();
        return productRepository.save(newProduct);
    }

    @Override
    public Product getProductById(Long productId) throws Exception {
        return productRepository.findById(productId).orElseThrow(() ->
                new DataNotFoundException("Cannot find product with id =" + productId));
    }

    @Override
    public Page<ProductResponse> getAllProducts(PageRequest pageRequest) {
        // Lấy danh sách sản phẩm theo trang(page) và giới hạn(limit)
        //findAll(pageRequest) return 1 đối tượng Page chứa các kết quả dữ liệu theo trang
        return productRepository.findAll(pageRequest).map(ProductResponse::fromProduct); //Product -> ProductResponse
        //anh xa vs moi product ap dung method fromProduct tu ProductResponse
    }

    @Override
    public Product updateProduct(Long productId, ProductDTO productDTO) throws Exception {
        Product existingProduct = getProductById(productId);

        Category existingCategory = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Cannot find category with id: " + productDTO.getCategoryId()));

        existingProduct.setCategory(existingCategory);
        existingProduct.setName(productDTO.getName());
        existingProduct.setThumbnail(productDTO.getThumbnail());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setDescription(productDTO.getDescription());
        return productRepository.save(existingProduct);//dang loi tao id moi
    }

    @Override
    public void deleteProduct(Long productId) {
        Optional<Product> existingProduct = productRepository.findById(productId);
        existingProduct.ifPresent(productRepository::delete); // =voi productRepository.delete(existingProduct)
        //:: la tham chieu den method delete() cua productRepository
    }

    @Override
    public Boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    @Override
    public ProductImage createProductImage(Long productId, ProductImageDTO productImageDTO) throws Exception {
        Product existingProduct = getProductById(productId);
        ProductImage newProductImage = ProductImage
                .builder()
                .product(existingProduct)
                .imageUrl(productImageDTO.getImageUrl())
                .build();
        //Ko cho insert quá 5 ảnh cho 1 sản phẩm
        //size() ktra so luong ban ghi cua product_id trong table product_image duoi database
        int size = productImageRepository.findByProductId(productId).size();
        if (size > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
            throw new InvalidParamException("Number of images must be <= " + ProductImage.MAXIMUM_IMAGES_PER_PRODUCT);
        }
        return productImageRepository.save(newProductImage);
    }
}
