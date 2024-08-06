package com.example.shopapp.controllers;

import com.example.shopapp.dtos.CategoryDTO;
import com.example.shopapp.responses.CategoryResponseDTO;
import com.example.shopapp.services.interfaces.ICategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/categories")
//@Validated //validate ở mức toàn cục cho tất cả method có @Valid hoặc @Validated, method ko có sẽ ko áp dụng
//nếu ko dùng @Validated global, tuy nhiên nó vẫn validate n~ method có @Valid hoặc @Validated
//khác nhau là: @Validated mức class có thể validate theo NHÓM cụ thể ở dto khi validation
@RequiredArgsConstructor
public class CategoryController {
    @Autowired
    private final ICategoryService iCategoryService;

    @PostMapping
    public ResponseEntity<?> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO,
            BindingResult result //nếu valid lỗi -> nằm trong BindingResult
    ) {
        try {
            if (result.hasErrors()) { //neu lấy lỗi cụ thể ch cần getFieldErrors().get(index) mà ko cần stream..
                List<String> errorMsg = result.getFieldErrors()
                        //trả về list chứa object FieldError (đại diện 1 lỗi c thể)
                        //tuong ung List<FieldError> fieldErrors = ...;
                        .stream() //chuyển danh lỗi lỗi thành 1 luồng để xử lý dễ hơn
                        .map(FieldError::getDefaultMessage)//map là method cảu stream API,
                        // duyệt qua từng FieldError,
                        // với mỗi FieldError gọi getDefaultMessage để lấy mô tả lỗi
                        //FieldError được lấy từ result.getFieldErrors()
                        .toList(); //đây cũng là method của stream API convert stream() to a list
                return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
            }
            iCategoryService.createCategory(categoryDTO);
            return new ResponseEntity<>("Insert category successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }//exception
    }

    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        try {
            List<CategoryResponseDTO> categoryResponseDTOS = iCategoryService.getAllCategories();
//        return new ResponseEntity<>(String.format("getAllCategories, page = %d, limit = %d", page, limit), HttpStatus.OK);
            return new ResponseEntity<>(categoryResponseDTOS, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable("id") Long id,
            @RequestBody CategoryDTO categoryDTO
    ) {
        try {
            iCategoryService.updateCategory(id, categoryDTO);
            return new ResponseEntity<>("Update category successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            iCategoryService.deleteCategory(id);
            return new ResponseEntity<>("Delete category with id " + id + " successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
