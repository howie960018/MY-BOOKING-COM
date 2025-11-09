package com.example.booking.service;

import com.example.booking.dto.PasswordUpdateDTO;
import com.example.booking.dto.UserProfileUpdateDTO;
import com.example.booking.model.User;
import com.example.booking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 用戶服務
 * 處理用戶資料更新、密碼修改、忘記密碼等功能
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final int RESET_TOKEN_VALIDITY_HOURS = 24;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    /**
     * 取得用戶資料
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("找不到用戶：" + username));
    }

    /**
     * 更新個人資料
     */
    @Transactional
    public User updateProfile(String username, UserProfileUpdateDTO dto) {
        logger.info("用戶 {} 更新個人資料", username);

        User user = getUserByUsername(username);

        // 檢查 email 是否已被其他用戶使用
        Optional<User> existingUser = userRepository.findByEmail(dto.getEmail());
        if (existingUser.isPresent() && !existingUser.get().getUsername().equals(username)) {
            throw new RuntimeException("此電子郵件已被使用");
        }

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());

        User savedUser = userRepository.save(user);
        logger.info("用戶 {} 個人資料更新成功", username);

        return savedUser;
    }

    /**
     * 更新密碼
     */
    @Transactional
    public void updatePassword(String username, PasswordUpdateDTO dto) {
        logger.info("用戶 {} 更新密碼", username);

        // 驗證新密碼與確認密碼是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("新密碼與確認密碼不一致");
        }

        User user = getUserByUsername(username);

        // 驗證舊密碼
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("舊密碼不正確");
        }

        // 檢查新密碼是否與舊密碼相同
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("新密碼不能與舊密碼相同");
        }

        // 加密並儲存新密碼
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        logger.info("用戶 {} 密碼更新成功", username);
    }

    /**
     * 忘記密碼 - 生成重設令牌
     */
    @Transactional
    public String generateResetToken(String email) {
        logger.info("為電子郵件 {} 生成密碼重設令牌", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("找不到此電子郵件的用戶"));

        // 生成隨機令牌
        String token = UUID.randomUUID().toString();

        // 設定令牌及過期時間
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(RESET_TOKEN_VALIDITY_HOURS));
        userRepository.save(user);

        logger.info("用戶 {} 的密碼重設令牌已生成", user.getUsername());

        // 實際應用中應該發送郵件，這裡返回 token 用於測試
        return token;
    }

    /**
     * 驗證重設令牌
     */
    public User validateResetToken(String token) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("無效的重設令牌"));

        if (user.getResetTokenExpiry() == null ||
            user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("重設令牌已過期");
        }

        return user;
    }

    /**
     * 重設密碼
     */
    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        logger.info("使用令牌重設密碼");

        // 驗證新密碼與確認密碼是否一致
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("新密碼與確認密碼不一致");
        }

        // 驗證令牌
        User user = validateResetToken(token);

        // 設定新密碼
        user.setPassword(passwordEncoder.encode(newPassword));

        // 清除重設令牌
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);

        logger.info("用戶 {} 密碼重設成功", user.getUsername());
    }

    /**
     * 發送重設密碼郵件
     */
    public void sendResetPasswordEmail(String email, String token) {
        logger.info("發送密碼重設郵件至 {}", email);

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("找不到此電子郵件的用戶"));

            // 使用 EmailService 發送真實郵件
            emailService.sendPasswordResetEmail(email, user.getUsername(), token);

            logger.info("密碼重設郵件發送成功至 {}", email);
        } catch (Exception e) {
            logger.error("發送密碼重設郵件失敗: {}", e.getMessage());
            // 記錄重設連結到日誌（備用方案）
            logger.info("重設連結（備用）：http://localhost:8080/user/reset-password?token={}", token);
            // 不拋出異常，讓用戶知道請求已處理（即使郵件發送失敗）
        }
    }
}

