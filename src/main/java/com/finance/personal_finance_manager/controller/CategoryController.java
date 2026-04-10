package com.finance.personal_finance_manager.controller;

import com.finance.personal_finance_manager.model.Category;
import com.finance.personal_finance_manager.repository.CategoryRepository;
import com.finance.personal_finance_manager.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getCategories(@RequestParam(required = false) Category.TransactionType type) {
        List<Category> categories;
        if (type != null) {
            categories = categoryRepository.findByType(type);
        } else {
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