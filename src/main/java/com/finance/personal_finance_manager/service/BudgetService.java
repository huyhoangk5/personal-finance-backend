package com.finance.personal_finance_manager.service;

import com.finance.personal_finance_manager.model.Budget;
import com.finance.personal_finance_manager.repository.BudgetRepository;
import com.finance.personal_finance_manager.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Quan trọng cho việc lưu hàng loạt

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public String checkBudgetExceeded(Long userId, Long categoryId, Double newAmount) {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        Double limit = budgetRepository.findFirstByUser_UserIdAndCategory_CategoryIdAndMonth(userId, categoryId, currentMonth)
                .map(b -> b.getCategoryLimit())
                .orElse(0.0);

        // NẾU CHƯA ĐẶT NGÂN SÁCH (limit = 0)
        if (limit <= 0) {
            return "CHƯA THIẾT LẬP: Danh mục này chưa có ngân sách cho tháng " + currentMonth;
        }

        Double actualSpending = transactionRepository.sumCurrentMonthSpending(userId, categoryId);
        if (actualSpending == null) actualSpending = 0.0;

        double totalForecast = actualSpending + newAmount;
        double percentage = (totalForecast / limit) * 100;

        if (totalForecast > limit) {
            return "CẢNH BÁO ĐỎ: Vượt hạn mức tháng " + currentMonth + "! Dự kiến: " + String.format("%.0f", percentage) + "%";
        }

        if (percentage >= 80) {
            return "CẢNH BÁO VÀNG: Sắp hết ngân sách tháng " + currentMonth + " (" + String.format("%.0f", percentage) + "%)";
        }

        return "AN TOÀN: Ngân sách tháng " + currentMonth + " hiện tại là " + String.format("%.0f", percentage) + "%.";
    }

    /**
     * Tự động sao chép ngân sách từ tháng trước sang tháng hiện tại
     */
    @Transactional
    public String copyLastMonthBudget(Long userId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate now = LocalDate.now();

        String currentMonth = now.format(formatter);
        String lastMonth = now.minusMonths(1).format(formatter);

        // 1. Kiểm tra xem tháng hiện tại đã có dữ liệu chưa
        List<Budget> currentBudgets = budgetRepository.findByUser_UserIdAndMonth(userId, currentMonth);
        if (!currentBudgets.isEmpty()) {
            return "THẤT BẠI: Tháng " + currentMonth + " đã được thiết lập ngân sách trước đó.";
        }

        // 2. Lấy ngân sách tháng trước
        List<Budget> lastMonthBudgets = budgetRepository.findByUser_UserIdAndMonth(userId, lastMonth);
        if (lastMonthBudgets.isEmpty()) {
            return "THÔNG BÁO: Không tìm thấy ngân sách tháng " + lastMonth + " để sao chép.";
        }

        // 3. Tiến hành sao chép
        List<Budget> newBudgets = new ArrayList<>();
        for (Budget oldBudget : lastMonthBudgets) {
            Budget newBudget = new Budget();
            newBudget.setUser(oldBudget.getUser());
            newBudget.setCategory(oldBudget.getCategory());
            newBudget.setCategoryLimit(oldBudget.getCategoryLimit());

            // THÊM DÒNG NÀY ĐỂ CHÉP LUÔN TỔNG HẠN MỨC
            newBudget.setTotalLimit(oldBudget.getTotalLimit());

            newBudget.setMonth(currentMonth);

            newBudgets.add(newBudget);
        }

        budgetRepository.saveAll(newBudgets);
        return "THÀNH CÔNG: Đã sao chép " + newBudgets.size() + " danh mục ngân sách từ tháng " + lastMonth + " sang tháng " + currentMonth;
    }
}