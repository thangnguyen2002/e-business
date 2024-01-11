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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final CategoryRepository categoryRepository;

    @Autowired
    private final ProductImageRepository productImageRepository;

    private static final String UPLOADS_FOLDER = "uploads";
    @Transactional
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
    public List<Product> findProductsByIds(List<Long> productIds) {
        return productRepository.findProductsByIds(productIds);
    }

//    @Override
//    public Page<ProductResponse> getAllProducts(PageRequest pageRequest) {
//        return productRepository.findAll(pageRequest).map(ProductResponse::fromProduct);
//    }

    @Override
    public Page<ProductResponse> getAllProducts(Long categoryId, String keyword, PageRequest pageRequest) {
        // Lấy danh sách sản phẩm theo trang(page) và giới hạn(limit) và categoryId (nếu có)
        //findAll(pageRequest) return 1 đối tượng Page chứa các kết quả dữ liệu theo trang
//         //Product -> ProductResponse
        //anh xa vs moi product ap dung method fromProduct tu ProductResponse
        Page<Product> productPage;
        productPage = productRepository.searchProducts(categoryId, keyword, pageRequest);
        return productPage.map(ProductResponse::fromProduct);
    }
    @Transactional
    @Override
    public Product updateProduct(Long productId, ProductDTO productDTO) throws Exception {
        Product existingProduct = getProductById(productId);

        Category existingCategory = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Cannot find category with id: " + productDTO.getCategoryId()));

        if(productDTO.getName() != null) {
            existingProduct.setName(productDTO.getName());
        }

        existingProduct.setCategory(existingCategory);
        if(productDTO.getPrice() >= 0) {
            existingProduct.setPrice(productDTO.getPrice());
        }
        if(productDTO.getDescription() != null) {
            existingProduct.setDescription(productDTO.getDescription());
        }
        if(productDTO.getThumbnail() != null) {
            existingProduct.setThumbnail(productDTO.getThumbnail());
        }

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
    @Transactional
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

    @Override
    public void deleteFile(String filename) throws IOException {
        // Đường dẫn đến thư mục chứa file
        Path uploadDir = Paths.get(UPLOADS_FOLDER);
        // Đường dẫn đầy đủ đến file cần xóa
        Path filePath = uploadDir.resolve(filename);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        } else {
            throw new FileNotFoundException("File not found: " + filename);
        }
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    public String storeFile(MultipartFile file) throws IOException { //hàm nyà return tên file sau khi đã lưu
        if (!isImageFile(file) || file.getOriginalFilename() == null) {
            throw new IOException("Invalid image format");
        }
        //đổi tên để tránh cái c bị ghi đè khi trùng tên và lưu vào th mục uploads
        String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())); //lay ten file goc
        // Thêm UUID vào trước tên file để đảm bảo tên file là duy nhất
        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
        // Đường dẫn đến thư mục mà bạn muốn lưu file
        java.nio.file.Path uploadDir = Paths.get(UPLOADS_FOLDER);
        // Kiểm tra và tạo thư mục nếu nó không tồn tại
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        // Đường dẫn đầy đủ đến file
        java.nio.file.Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
        // Sao chép file vào thư mục đích
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }
}
