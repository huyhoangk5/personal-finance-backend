package com.finance.personal_finance_manager.controller;

import com.finance.personal_finance_manager.model.Budget;
import com.finance.personal_finance_manager.repository.BudgetRepository;
import com.finance.personal_finance_manager.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private BudgetRepository budgetRepository;

    @GetMapping
    public List<Budget> getBudgets(@RequestParam Long userId) {
        // Gọi trực tiếp repository hoặc qua service nếu bạn đã viết hàm findByUser_UserId
        return budgetRepository.findByUser_UserId(userId);
    }

    // POST http://localhost:8080/api/budgets/copy-last-month?userId=1
    @PostMapping("/copy-last-month")
    public ResponseEntity<String> copyBudget(@RequestParam Long userId) {
        String result = budgetService.copyLastMonthBudget(userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/set-limit")
    public ResponseEntity<?> setBudgetLimit(@RequestBody Budget budget) {
        // Logic: Nếu đã tồn tại categoryId trong tháng đó thì cập nhật, chưa thì thêm mới
        Optional<Budget> existing = budgetRepository.findFirstByUser_UserIdAndCategory_CategoryIdAndMonth(
                budget.getUser().getUserId(),
                budget.getCategory().getCategoryId(),
                budget.getMonth());

        if (existing.isPresent()) {
            Budget b = existing.get();
            b.setCategoryLimit(budget.getCategoryLimit());
            return ResponseEntity.ok(budgetRepository.save(b));
        }
        return ResponseEntity.ok(budgetRepository.save(budget));
    }
}
