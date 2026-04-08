package com.finance.personal_finance_manager.repository;

import com.finance.personal_finance_manager.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Lấy danh sách danh mục theo loại (Ví dụ: Chỉ lấy các mục THU)
    List<Category> findByType(Category.TransactionType type);

    // Tìm danh mục theo tên chính xác
    List<Category> findByCategoryNameContainingIgnoreCase(String name);
}