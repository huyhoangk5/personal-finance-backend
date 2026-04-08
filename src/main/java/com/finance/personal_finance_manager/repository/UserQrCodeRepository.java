package com.finance.personal_finance_manager.repository;

import com.finance.personal_finance_manager.model.User;
import com.finance.personal_finance_manager.model.UserQrCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserQrCodeRepository extends JpaRepository<UserQrCode, Long> {
    Optional<UserQrCode> findByQrToken(String qrToken);
    Optional<UserQrCode> findByUser(User user);
}