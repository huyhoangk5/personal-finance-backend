package com.finance.personal_finance_manager.repository;

import com.finance.personal_finance_manager.model.PasswordResetToken;
import com.finance.personal_finance_manager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}