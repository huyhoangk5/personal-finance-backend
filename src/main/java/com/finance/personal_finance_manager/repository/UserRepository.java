package com.finance.personal_finance_manager.repository;

import com.finance.personal_finance_manager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm kiếm người dùng bằng tên đăng nhập
    Optional<User> findByUsername(String username);

    // Kiểm tra xem username đã tồn tại hay chưa (dùng khi đăng ký)
    Boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);
}