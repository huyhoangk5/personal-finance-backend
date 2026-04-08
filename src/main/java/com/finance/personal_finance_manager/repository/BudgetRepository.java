package com.finance.personal_finance_manager.repository;

import com.finance.personal_finance_manager.model.Budget;
import com.finance.personal_finance_manager.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Thêm hàm này để khớp với BudgetService
    Optional<Budget> findFirstByUser_UserIdAndCategory_CategoryId(Long userId, Long categoryId);

    // Giữ lại hàm cũ của bạn
    Optional<Budget> findFirstByUser_UserIdAndCategory_CategoryIdAndMonth(Long userId, Long categoryId, String month);

    // Lấy danh sách ngân sách cảu 1 người trong 1 tháng
    List<Budget> findByUser_UserIdAndMonth(Long userId, String month);

    List<Budget> findByUser_UserId(Long userId);

    void deleteByCategory(Category category);
}