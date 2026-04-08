package com.finance.personal_finance_manager.repository;

import com.finance.personal_finance_manager.model.Transaction;
import com.finance.personal_finance_manager.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUser_UserId(Long userId);

    List<Transaction> findByNoteContainingIgnoreCaseAndUser_UserId(String keyword, Long userId);

    List<Transaction> findTop5ByUser_UserIdOrderByTransactionIdDesc(Long userId);

    @Query("SELECT t.category.categoryName, SUM(t.amount) " +
            "FROM Transaction t " +
            "WHERE t.user.userId = :userId AND t.type = 'CHI' " +
            "GROUP BY t.category.categoryName")
    List<Object[]> sumAmountByCategoryAndUser(@Param("userId") Long userId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type AND t.user.userId = :userId")
    Double sumAmountByTypeAndUser(@Param("type") Category.TransactionType type, @Param("userId") Long userId);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.user.userId = :userId " +
            "AND t.category.categoryId = :categoryId " +
            "AND t.type = 'CHI' " +
            "AND MONTH(t.date) = MONTH(CURRENT_DATE) " +
            "AND YEAR(t.date) = YEAR(CURRENT_DATE)")
    Double sumCurrentMonthSpending(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    @Query("SELECT c.categoryName, SUM(t.amount) FROM Transaction t JOIN t.category c " +
            "WHERE t.user.userId = :userId AND t.type = 'CHI' " +
            "AND FUNCTION('DATE_FORMAT', t.date, '%Y-%m') = :month " +
            "GROUP BY c.categoryName")
    List<Object[]> sumAmountByCategoryAndUserAndMonth(@Param("userId") Long userId, @Param("month") String month);

    void deleteByCategory(Category category);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type AND t.user.userId = :userId AND FUNCTION('DATE_FORMAT', t.date, '%Y-%m') = :month")
    Double sumAmountByTypeAndUserAndMonth(@Param("type") Category.TransactionType type, @Param("userId") Long userId, @Param("month") String month);

    @Query("SELECT c.categoryName, SUM(t.amount) FROM Transaction t JOIN t.category c " +
            "WHERE t.user.userId = :userId AND t.type = 'THU' " +
            "AND FUNCTION('DATE_FORMAT', t.date, '%Y-%m') = :month " +
            "GROUP BY c.categoryName")
    List<Object[]> sumIncomeByCategoryAndUserAndMonth(@Param("userId") Long userId, @Param("month") String month);

    @Query("SELECT t.date, SUM(t.amount) FROM Transaction t " +
            "WHERE t.user.userId = :userId AND t.type = :type " +
            "AND FUNCTION('DATE_FORMAT', t.date, '%Y-%m') = :month " +
            "GROUP BY t.date")
    List<Object[]> sumAmountByDayAndType(@Param("userId") Long userId,
                                         @Param("type") Category.TransactionType type,
                                         @Param("month") String month);
}