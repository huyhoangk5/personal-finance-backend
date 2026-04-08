package com.finance.personal_finance_manager.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false)
    private String categoryName;

    @Enumerated(EnumType.STRING)
    private TransactionType type; // THU hoặc CHI

    public enum TransactionType {
        THU, CHI
    }
}
