package com.finance.personal_finance_manager.controller;

import com.finance.personal_finance_manager.model.User;
import com.finance.personal_finance_manager.repository.UserRepository;
import com.finance.personal_finance_manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        String result = userService.registerUser(user);
        if (result.contains("Lỗi")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        System.out.println("Login called with: " + credentials);
        String username = credentials.get("username");
        String password = credentials.get("password");
        Optional<User> user = userService.login(username, password);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(401).body("Sai tên đăng nhập hoặc mật khẩu!");
        }
    }

    @Autowired
    UserRepository userRepository;

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody User userDetails) {
        Optional<User> optionalUser = userService.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = optionalUser.get();
        if (userDetails.getFullName() != null) user.setFullName(userDetails.getFullName());
        if (userDetails.getEmail() != null) {
            // Kiểm tra email đã tồn tại chưa (trừ chính user này)
            Optional<User> existingEmail = userRepository.findByEmail(userDetails.getEmail());
            if (existingEmail.isPresent() && !existingEmail.get().getUserId().equals(userId)) {
                return ResponseEntity.badRequest().body(null); // Hoặc trả về lỗi cụ thể
            }
            user.setEmail(userDetails.getEmail());
        }
        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        Optional<User> userOpt = userService.authenticateGoogle(token);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        } else {
            return ResponseEntity.status(401).body("Xác thực Google thất bại");
        }
    }

    @PostMapping("/facebook-login")
    public ResponseEntity<?> facebookLogin(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        Optional<User> userOpt = userService.authenticateFacebook(token);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        } else {
            return ResponseEntity.status(401).body("Xác thực Facebook thất bại");
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody Map<String, String> payload) {
        String phoneNumber = payload.get("phoneNumber");
        String otp = userService.generateAndSendOtp(phoneNumber);
        return ResponseEntity.ok("OTP đã được gửi (giả lập: " + otp + ")");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        String phoneNumber = payload.get("phoneNumber");
        String otp = payload.get("otp");
        Optional<User> userOpt = userService.verifyOtpAndCreateUser(phoneNumber, otp);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        } else {
            return ResponseEntity.status(401).body("OTP không hợp lệ");
        }
    }

    @GetMapping("/qr-login/generate")
    public ResponseEntity<String> generateQrToken() {
        String token = userService.generateQrToken();
        return ResponseEntity.ok(token);
    }

    @GetMapping("/qr-login/verify")
    public ResponseEntity<?> verifyQrToken(@RequestParam String token) {
        Optional<User> userOpt = userService.verifyQrToken(token);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        } else {
            return ResponseEntity.status(401).body("Token không hợp lệ hoặc đã hết hạn");
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> payload) {
        Long userId = Long.parseLong(payload.get("userId"));
        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");
        boolean changed = userService.changePassword(userId, oldPassword, newPassword);
        if (changed) {
            return ResponseEntity.ok("Đổi mật khẩu thành công");
        } else {
            return ResponseEntity.status(400).body("Mật khẩu cũ không đúng hoặc user không tồn tại");
        }
    }

    // Lấy QR token cố định của user
    @GetMapping("/qr-code")
    public ResponseEntity<String> getUserQrCode(@RequestParam Long userId) {
        String qrToken = userService.generateUserQrCode(userId);
        return ResponseEntity.ok(qrToken);
    }

    // Xác nhận đăng nhập từ điện thoại (quét QR)
    @PostMapping("/qr-login/confirm")
    public ResponseEntity<?> confirmQrLogin(@RequestBody Map<String, String> payload) {
        String qrToken = payload.get("qrToken");
        Long userId = Long.parseLong(payload.get("userId")); // Lấy từ request (sẽ có từ frontend)
        boolean confirmed = userService.confirmQrLogin(qrToken, userId);
        if (confirmed) {
            return ResponseEntity.ok("Xác nhận thành công");
        } else {
            return ResponseEntity.status(401).body("Xác nhận thất bại");
        }
    }

    // Polling kiểm tra trạng thái (dùng session token)
    @GetMapping("/qr-login/status")
    public ResponseEntity<?> getQrLoginStatus(@RequestParam String token) {
        Optional<User> userOpt = userService.getQrLoginUser(token);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        } else {
            return ResponseEntity.status(404).body("Chưa có đăng nhập");
        }
    }

    @GetMapping("/qr-code-by-email")
    public ResponseEntity<?> getQrCodeByEmail(@RequestParam String email) {
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Email không tồn tại");
        }
        String qrToken = userService.generateUserQrCode(userOpt.get().getUserId());
        return ResponseEntity.ok(qrToken);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String token = userService.createPasswordResetToken(email);
        if (token == null) {
            return ResponseEntity.badRequest().body("Email không tồn tại");
        }
        userService.sendPasswordResetEmail(email, token);
        return ResponseEntity.ok("Email đặt lại mật khẩu đã được gửi (kiểm tra console)");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");
        boolean success = userService.resetPassword(token, newPassword);
        if (success) {
            return ResponseEntity.ok("Đặt lại mật khẩu thành công");
        } else {
            return ResponseEntity.badRequest().body("Token không hợp lệ hoặc đã hết hạn");
        }
    }
}