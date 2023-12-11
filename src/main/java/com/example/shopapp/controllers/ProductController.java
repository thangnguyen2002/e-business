package com.example.shopapp.controllers;

import com.example.shopapp.dtos.ProductDTO;
import com.example.shopapp.dtos.ProductImageDTO;
import com.example.shopapp.models.Product;
import com.example.shopapp.models.ProductImage;
import com.example.shopapp.repositories.ProductRepository;
import com.example.shopapp.responses.ProductListResponse;
import com.example.shopapp.responses.ProductResponse;
import com.example.shopapp.services.interfaces.IProductService;
import com.github.javafaker.Faker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    @Autowired
    private final IProductService iProductService;

    @PostMapping() //kieu upload file la multipart file
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorMsg = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
            }
            Product newProduct = iProductService.createProduct(productDTO);
            return new ResponseEntity<>(newProduct, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/uploads/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(
            @PathVariable("productId") Long productId,
            @ModelAttribute("files") List<MultipartFile> files) {
        try {
            Product existingProduct = iProductService.getProductById(productId);
            //nếu ko có trường files -> files=null -> vào catch
            //nhung day ko can vi form-data chac chan co truong files khi goi api uploadImages
            //ko upfile -> goi api createProduct
            files = files == null ? new ArrayList<MultipartFile>() : files;
            //ko cho upload qua' 5 file 1 luc
            if (files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
                return new ResponseEntity<>("You can only upload maximum 5 images", HttpStatus.BAD_REQUEST);
            }
            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile file : files) {
                //khi có trường files mà ko truyền file thì vẫn cho tạo, vì ko bắt buộc truyền file
                if (file.getSize() == 0) {
                    continue;
                }
                // Kiểm tra kích thước file và định dạng
                if (file.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
                    return new ResponseEntity<>("File is too large! Maximum size is 10MB",
                            HttpStatus.PAYMENT_REQUIRED);
                }
                String contentType = file.getContentType();
                //kiem tra dinh dang file la image?
                if (contentType == null || !contentType.startsWith("image/")) {
                    return new ResponseEntity<>("File must be an image",
                            HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                }
                // Lưu file và cập nhật thumbnail trong DTO
                String filename = storeFile(file);// Thay thế hàm này với code của bạn để lưu file
                //lưu vào bảng product_images
                ProductImage productImage = iProductService.createProductImage(
                        productId,
                        ProductImageDTO
                                .builder()
                                .imageUrl(filename)
                                .build()
                );
                productImages.add(productImage);
            }
            return new ResponseEntity<>(productImages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    private String storeFile(MultipartFile file) throws IOException { //hàm nyà return tên file sau khi đã lưu
        if (!isImageFile(file) || file.getOriginalFilename() == null) {
            throw new IOException("Invalid image format");
        }
        //đổi tên để tránh cái c bị ghi đè khi trùng tên và lưu vào th mục uploads
        String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())); //lay ten file goc
        // Thêm UUID vào trước tên file để đảm bảo tên file là duy nhất
        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
        // Đường dẫn đến thư mục mà bạn muốn lưu file
        java.nio.file.Path uploadDir = Paths.get("uploads");
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

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    @GetMapping
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam(value = "page", defaultValue = "10") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {
        PageRequest pageRequest = PageRequest.of(
                page, limit, Sort.by("createdAt").descending()
        );
        Page<ProductResponse> productPage = iProductService.getAllProducts(pageRequest);
        // Lấy tổng số trang
        int totalPages = productPage.getTotalPages();
        List<ProductResponse> products = productPage.getContent();

        return new ResponseEntity<>(ProductListResponse.builder()
                .products(products)
                .totalPages(totalPages)
                .build(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") Long productId) {
        try {
            Product product = iProductService.getProductById(productId);
            return new ResponseEntity<>(product, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable("id") Long productId, @RequestBody ProductDTO productDTO) {
        try {
            Product product = iProductService.updateProduct(productId, productDTO);
            return new ResponseEntity<>(product, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable("id") Long productId) {
        try {
            iProductService.deleteProduct(productId);
            return new ResponseEntity<>(String.format("Product with id = %d deleted successfully", productId),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

//    @PostMapping("/generateFakeProducts")
    private ResponseEntity<String> generateFakeProducts() {
        Faker faker = new Faker();
        for (int i = 0; i < 1_000_000; i++) {
            String productName = faker.commerce().productName();
            if(iProductService.existsByName(productName)) {
                continue;
            }
            ProductDTO productDTO = ProductDTO.builder()
                    .name(productName)
                    .price((float)faker.number().numberBetween(10, 90_000_000))
                    .description(faker.lorem().sentence())
                    .thumbnail("")
                    .categoryId((long)faker.number().numberBetween(2, 5))
                    .build();
            try {
                iProductService.createProduct(productDTO);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.ok("Fake Products created successfully");
    }
}
