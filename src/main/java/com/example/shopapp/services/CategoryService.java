package com.example.shopapp.services;

import com.example.shopapp.dtos.CategoryDTO;
import com.example.shopapp.responses.CategoryResponseDTO;
import com.example.shopapp.models.Category;
import com.example.shopapp.repositories.CategoryRepository;
import com.example.shopapp.services.interfaces.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor //tu dong sinh constructor cho cac final
public class CategoryService implements ICategoryService {
    @Autowired
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponseDTO createCategory(CategoryDTO categoryDTO) {
        Category newCategory = Category
                .builder()
                .name(categoryDTO.getName())
                .build();
        Category category = categoryRepository.save(newCategory);
        return new CategoryResponseDTO(category.getId(), category.getName());
    }

    @Override
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return new CategoryResponseDTO(category.getId(), category.getName());
    }

    @Override
    public List<CategoryResponseDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll(); //no tra ve 1 list, it ban ghi nen ko can phan trang
        List<CategoryResponseDTO> categoryResponseDTOS = new ArrayList<CategoryResponseDTO>();
        for (Category category : categories) {
            CategoryResponseDTO categoryResponseDTO = new CategoryResponseDTO();
            categoryResponseDTO.setId(category.getId());
            categoryResponseDTO.setName(category.getName());
            categoryResponseDTOS.add(categoryResponseDTO);
        }
        return categoryResponseDTOS;
    }

    @Override
    public CategoryResponseDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        existingCategory.setName(categoryDTO.getName());
        Category categoryUpdate = categoryRepository.save(existingCategory);
        return new CategoryResponseDTO(categoryUpdate.getId(), categoryUpdate.getName());
    }

    @Override
    public void deleteCategory(Long id) {
        //xóa xong
        categoryRepository.deleteById(id);
    }
}
