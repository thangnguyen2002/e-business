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
import org.springframework.core.io.UrlResource;
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
import java.util.*;
import java.util.stream.Collectors;

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
                String filename = iProductService.storeFile(file);// Thay thế hàm này với code của bạn để lưu file
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

    @GetMapping("/images/{imageName}")
    public ResponseEntity<?> viewImage(@PathVariable String imageName) {
        try {
            Path imagePath = Paths.get("uploads/" + imageName);
            UrlResource resource = new UrlResource(imagePath.toUri());
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                return new ResponseEntity<>(
                        new UrlResource(Paths.get("uploads/notfound.jpeg").toUri()),
                        HttpStatus.OK);
//                return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("images/{imageName}")
    public ResponseEntity<?> deleteFile(@PathVariable String imageName) {
        try {
            iProductService.deleteFile(imageName);
            return new ResponseEntity<>("Delete file successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "category_id", defaultValue = "0") Long categoryId

    ) {
        PageRequest pageRequest = PageRequest.of(
                page, limit,
                Sort.by("createdAt").descending()
//                Sort.by("id").ascending()
        );

        Page<ProductResponse> productPage = iProductService.getAllProducts(categoryId, keyword, pageRequest);
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

    @GetMapping("/by-ids")
    public ResponseEntity<?> getProductsByIds(@RequestParam("ids") String ids) {
        //eg: 1,3,5,7
        try {
            // Tách chuỗi ids thành một mảng các số nguyên
            List<Long> productIds = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .toList();
            List<Product> products = iProductService.findProductsByIds(productIds);
            return ResponseEntity.ok().body(products);

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
            if (iProductService.existsByName(productName)) {
                continue;
            }
            ProductDTO productDTO = ProductDTO.builder()
                    .name(productName)
                    .price((float) faker.number().numberBetween(10, 90_000_000))
                    .description(faker.lorem().sentence())
                    .thumbnail("")
                    .categoryId((long) faker.number().numberBetween(2, 5))
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
