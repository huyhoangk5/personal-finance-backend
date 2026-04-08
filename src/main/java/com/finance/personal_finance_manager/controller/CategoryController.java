package com.finance.personal_finance_manager.controller;

import com.finance.personal_finance_manager.model.Category;
import com.finance.personal_finance_manager.repository.CategoryRepository;
import com.finance.personal_finance_manager.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173") // Cho phép React gọi API không bị lỗi CORS
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    /**
     * Lấy danh sách danh mục. Có thể lọc theo type.
     * API 1: GET http://localhost:8080/api/categories (Lấy tất cả)
     * API 2: GET http://localhost:8080/api/categories?type=THU (Chỉ lấy THU)
     * API 3: GET http://localhost:8080/api/categories?type=CHI (Chỉ lấy CHI)
     */
    @GetMapping
    public ResponseEntity<List<Category>> getCategories(
            @RequestParam(required = false) Category.TransactionType type) {

        List<Category> categories;

        if (type != null) {
            // Nếu có truyền type thì lọc theo type
            categories = categoryRepository.findByType(type);
        } else {
            // Nếu không truyền gì thì lấy tất cả
            categories = categoryRepository.findAll();
        }

        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        return categoryRepository.save(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category details) {
        return categoryRepository.findById(id).map(cat -> {
            cat.setCategoryName(details.getCategoryName());
            cat.setType(details.getType());
            return ResponseEntity.ok(categoryRepository.save(cat));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
}