package com.finance.personal_finance_manager.dto;

import com.finance.personal_finance_manager.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionResponse {
    private Transaction transaction;
    private String budgetMessage;
}