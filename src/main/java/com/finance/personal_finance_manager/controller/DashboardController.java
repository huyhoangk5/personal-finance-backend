package com.finance.personal_finance_manager.controller;

import com.finance.personal_finance_manager.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/spending-by-category")
    public Map<String, Double> getSpendingByCategory(@RequestParam Long userId) {
        return dashboardService.getSpendingStats(userId);
    }

    @GetMapping("/balance")
    public Map<String, Double> getBalance(@RequestParam Long userId) {
        return dashboardService.getBalanceStats(userId);
    }

    @GetMapping("/spending-by-category-month")
    public Map<String, Double> getSpendingByCategoryAndMonth(
            @RequestParam Long userId,
            @RequestParam String month) {
        return dashboardService.getSpendingByCategoryAndMonth(userId, month);
    }

    @GetMapping("/monthly-summary")
    public Map<String, Object> getMonthlySummary(@RequestParam Long userId, @RequestParam int months) {
        return dashboardService.getMonthlySummary(userId, months);
    }

    @GetMapping("/top-spending-categories")
    public List<Map<String, Object>> getTopSpendingCategories(@RequestParam Long userId, @RequestParam int limit) {
        return dashboardService.getTopSpendingCategories(userId, limit);
    }

    @GetMapping("/trend")
    public List<Map<String, Object>> getTrend(@RequestParam Long userId, @RequestParam int months) {
        return dashboardService.getTrend(userId, months);
    }

    @GetMapping("/balance-month")
    public Map<String, Double> getBalanceByMonth(@RequestParam Long userId, @RequestParam String month) {
        return dashboardService.getBalanceStatsByMonth(userId, month);
    }

    @GetMapping("/income-by-category-month")
    public Map<String, Double> getIncomeByCategoryAndMonth(
            @RequestParam Long userId,
            @RequestParam String month) {
        return dashboardService.getIncomeByCategoryAndMonth(userId, month);
    }

    @GetMapping("/daily-summary")
    public Map<String, Map<String, Double>> getDailySummary(@RequestParam Long userId, @RequestParam String month) {
        return dashboardService.getDailySummary(userId, month);
    }
}