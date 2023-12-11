package com.example.shopapp.services.interfaces;

import com.example.shopapp.dtos.CategoryDTO;
import com.example.shopapp.responses.CategoryResponseDTO;

import java.util.List;

public interface ICategoryService {
    CategoryResponseDTO createCategory(CategoryDTO categoryDTO);
    CategoryResponseDTO getCategoryById(Long id);
    List<CategoryResponseDTO> getAllCategories();
    CategoryResponseDTO updateCategory(Long categoryId, CategoryDTO categoryDTO);
    void deleteCategory(Long id);
}
