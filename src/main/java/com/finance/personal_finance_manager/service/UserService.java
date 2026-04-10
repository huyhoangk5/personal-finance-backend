package com.finance.personal_finance_manager.service;

import com.finance.personal_finance_manager.model.PasswordResetToken;
import com.finance.personal_finance_manager.model.User;
import com.finance.personal_finance_manager.model.UserQrCode;
import com.finance.personal_finance_manager.repository.PasswordResetTokenRepository;
import com.finance.personal_finance_manager.repository.UserQrCodeRepository;
import com.finance.personal_finance_manager.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Các method cũ giữ nguyên
    public String registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Lỗi: Tên đăng nhập đã tồn tại!";
        }
        // Mã hóa mật khẩu trước khi lưu
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "Đăng ký thành công!";
    }

    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // Xác thực Google với token thật
    public Optional<User> authenticateGoogle(String idTokenString) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList("923508787768-tirtvocpu20jrba6khna61ppbqjv3idj.apps.googleusercontent.com"))
                .build();
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                User user = userRepository.findByUsername(email).orElse(null);
                if (user == null) {
                    user = new User();
                    user.setUsername(email);
                    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Mã hóa
                    user.setEmail(email);
                    user.setFullName(name);
                    user = userRepository.save(user);
                }
                return Optional.of(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Xác thực Facebook (giả lập)
    public Optional<User> authenticateFacebook(String accessToken) {
        String email = "fb_" + UUID.randomUUID().toString() + "@example.com";
        String username = email;
        String randomPassword = UUID.randomUUID().toString();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(randomPassword));
            user.setEmail(email);
            user.setFullName("Facebook User");
            user = userRepository.save(user);
        }
        return Optional.of(user);
    }

    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    // ==================== OTP LOGIN ====================
    @Value("${twilio.account.sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token:}")
    private String twilioAuthToken;

    @Value("${twilio.phone.number:}")
    private String twilioPhoneNumber;

    private Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    private static class OtpData {
        String otp;
        LocalDateTime expiry;
        OtpData(String otp) {
            this.otp = otp;
            this.expiry = LocalDateTime.now().plusMinutes(5);
        }
        boolean isValid() {
            return LocalDateTime.now().isBefore(expiry);
        }
    }

    @PostConstruct
    public void initTwilio() {
        if (twilioAccountSid != null && !twilioAccountSid.isEmpty()) {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            System.out.println("Twilio initialized for OTP");
        } else {
            System.out.println("Twilio not configured, OTP will be printed to console");
        }
    }

    public String generateAndSendOtp(String phoneNumber) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(phoneNumber, new OtpData(otp));
        if (twilioAccountSid != null && !twilioAccountSid.isEmpty()) {
            try {
                Message.creator(
                        new PhoneNumber(phoneNumber),
                        new PhoneNumber(twilioPhoneNumber),
                        "Mã OTP đăng nhập Finance Manager: " + otp
                ).create();
                System.out.println("SMS sent to " + phoneNumber);
            } catch (Exception e) {
                System.err.println("Failed to send SMS: " + e.getMessage());
                System.out.println("OTP for " + phoneNumber + " is " + otp);
            }
        } else {
            System.out.println("OTP for " + phoneNumber + " is " + otp);
        }
        return otp;
    }

    public Optional<User> verifyOtpAndCreateUser(String phoneNumber, String otp) {
        OtpData otpData = otpStore.get(phoneNumber);
        if (otpData != null && otpData.isValid() && otpData.otp.equals(otp)) {
            String username = phoneNumber;
            String randomPassword = UUID.randomUUID().toString();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                user = new User();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(randomPassword));
                user.setFullName("User " + phoneNumber);
                user = userRepository.save(user);
            }
            otpStore.remove(phoneNumber);
            return Optional.of(user);
        }
        return Optional.empty();
    }

    private static class QrSession {
        String token;
        LocalDateTime expiry;
        boolean used;
        User user;
        QrSession(String token) {
            this.token = token;
            this.expiry = LocalDateTime.now().plusMinutes(5);
            this.used = false;
            this.user = null;
        }
        boolean isValid() { return LocalDateTime.now().isBefore(expiry); }
    }

    private final Map<String, QrSession> qrSessions = new ConcurrentHashMap<>();

    public String generateQrToken() {
        String token = UUID.randomUUID().toString();
        qrSessions.put(token, new QrSession(token));
        return token;
    }

    // Dành cho điện thoại: xác thực token và tạo user
    public Optional<User> verifyQrToken(String token) {
        QrSession session = qrSessions.get(token);
        if (session != null && session.isValid() && !session.used) {
            String username = "qr_" + UUID.randomUUID().toString().substring(0, 8);
            String randomPassword = UUID.randomUUID().toString();
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(randomPassword));
            user.setFullName("User from QR");
            user = userRepository.save(user);
            session.user = user;
            session.used = true;
            return Optional.of(user);
        }
        return Optional.empty();
    }

    // Dành cho polling: kiểm tra trạng thái, trả về user nếu đã xác thực
    public Optional<User> getQrTokenStatus(String token) {
        QrSession session = qrSessions.get(token);
        if (session != null && session.isValid() && session.used) {
            return Optional.of(session.user);
        }
        return Optional.empty();
    }

    @Autowired
    private UserQrCodeRepository userQrCodeRepository;

    // Lưu trữ session tạm cho QR login (token -> userId)
    private final Map<String, Long> qrLoginSessions = new ConcurrentHashMap<>();
    private final Map<String, String> qrTokenToSessionToken = new ConcurrentHashMap<>();


    // Tạo QR code cố định cho user (nếu chưa có)
    public String generateUserQrCode(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Optional<UserQrCode> existing = userQrCodeRepository.findByUser(user);
        if (existing.isPresent()) {
            return existing.get().getQrToken();
        }
        String token = UUID.randomUUID().toString();
        UserQrCode qrCode = new UserQrCode(null, user, token, LocalDateTime.now());
        userQrCodeRepository.save(qrCode);
        return token;
    }

    // Xác nhận đăng nhập từ điện thoại
    public boolean confirmQrLogin(String qrToken, Long userId) {
        UserQrCode qrCode = userQrCodeRepository.findByQrToken(qrToken).orElse(null);
        if (qrCode == null || !qrCode.getUser().getUserId().equals(userId)) {
            return false;
        }
        String sessionToken = UUID.randomUUID().toString();
        qrLoginSessions.put(sessionToken, userId);
        qrTokenToSessionToken.put(qrToken, sessionToken);
        return true;
    }

    // Lấy user từ session token (polling)
    public Optional<User> getQrLoginUser(String qrToken) {
        String sessionToken = qrTokenToSessionToken.remove(qrToken);
        if (sessionToken == null) return Optional.empty();
        Long userId = qrLoginSessions.remove(sessionToken);
        if (userId != null) {
            return userRepository.findById(userId);
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private JavaMailSender mailSender; // Có thể bỏ nếu không dùng email thật

    // Phương thức tạo token reset password
    @Transactional
    public String createPasswordResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return null;
        User user = userOpt.get();
        passwordResetTokenRepository.deleteByUser(user);
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(null, token, user, LocalDateTime.now().plusHours(1));
        passwordResetTokenRepository.save(resetToken);
        return token;
    }

    // Gửi email (giả lập in ra console, có thể tích hợp thật)
    public void sendPasswordResetEmail(String email, String token) {
        String resetLink = "http://localhost:5173/reset-password?token=" + token;
        System.out.println("Link đặt lại mật khẩu: " + resetLink);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Đặt lại mật khẩu Finance Manager");
            message.setText("Click vào link để đặt lại mật khẩu: " + resetLink);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Gửi email thất bại: " + e.getMessage());
            // Không throw exception
        }
    }

    // Xác nhận token và đặt lại mật khẩu
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty() || tokenOpt.get().getExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }
        User user = tokenOpt.get().getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(tokenOpt.get());
        return true;
    }
}