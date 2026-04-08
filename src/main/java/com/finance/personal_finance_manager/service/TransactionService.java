package com.finance.personal_finance_manager.service;

import com.finance.personal_finance_manager.dto.TransactionResponse;
import com.finance.personal_finance_manager.model.Category;
import com.finance.personal_finance_manager.model.Transaction;
import com.finance.personal_finance_manager.repository.CategoryRepository;
import com.finance.personal_finance_manager.repository.TransactionRepository;
import com.finance.personal_finance_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BudgetService budgetService;

    // 1. Lấy tất cả giao dịch của 1 User cụ thể
    public List<Transaction> getAllTransactionsByUser(Long userId) {
        // Cần thêm hàm findByUser_UserId trong Repository nếu chưa có
        return transactionRepository.findByUser_UserId(userId);
    }

    // 2. Lưu giao dịch mới (Giữ nguyên logic nạp User/Category)
    public TransactionResponse saveTransaction(Transaction transaction) {
        if (transaction.getUser() != null) {
            userRepository.findById(transaction.getUser().getUserId()).ifPresent(transaction::setUser);
        }
        if (transaction.getCategory() != null) {
            categoryRepository.findById(transaction.getCategory().getCategoryId()).ifPresent(transaction::setCategory);
        }

        String message = "Giao dịch thu nhập hoặc chưa đặt hạn mức.";
        if (transaction.getType() == Category.TransactionType.CHI) {
            message = budgetService.checkBudgetExceeded(
                    transaction.getUser().getUserId(),
                    transaction.getCategory().getCategoryId(),
                    transaction.getAmount()
            );
        }

        Transaction saved = transactionRepository.save(transaction);
        return new TransactionResponse(saved, message);
    }

    // 3. Tìm kiếm theo ghi chú VÀ UserId
    public List<Transaction> searchByNote(String keyword, Long userId) {
        return transactionRepository.findByNoteContainingIgnoreCaseAndUser_UserId(keyword, userId);
    }

    // 4. Lấy 5 giao dịch gần nhất của UserId
    public List<Transaction> getRecentTransactions(Long userId) {
        return transactionRepository.findTop5ByUser_UserIdOrderByTransactionIdDesc(userId);
    }

    // 5. Xóa giao dịch (Bảo mật: Chỉ xóa nếu đúng chủ sở hữu)
    public boolean deleteTransaction(Long id, Long userId) {
        return transactionRepository.findById(id).map(t -> {
            if (t.getUser().getUserId().equals(userId)) {
                transactionRepository.delete(t);
                return true;
            }
            return false;
        }).orElse(false);
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    public Transaction updateTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public Map<String, Double> getSpendingByCategoryAndMonth(Long userId, String month) {
        List<Object[]> results = transactionRepository.sumAmountByCategoryAndUserAndMonth(userId, month);
        Map<String, Double> map = new HashMap<>();
        for (Object[] row : results) {
            map.put((String) row[0], (Double) row[1]);
        }
        return map;
    }
}