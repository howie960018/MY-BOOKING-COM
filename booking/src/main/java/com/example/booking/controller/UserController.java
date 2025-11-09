package com.example.booking.controller;

import com.example.booking.model.User;
import com.example.booking.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "使用者認證與註冊 API")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    @Operation(
        summary = "註冊新帳號",
        description = """
            註冊新使用者帳號。帳號需 3-20 字元，密碼至少 6 字元，Email 必填。
            註冊後預設為一般用戶角色（ROLE_USER）。
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "註冊成功",
            content = @Content(mediaType = "text/plain")
        ),
        @ApiResponse(responseCode = "400", description = "參數錯誤（帳號或密碼格式不符）"),
        @ApiResponse(responseCode = "409", description = "帳號已存在"),
        @ApiResponse(responseCode = "500", description = "系統錯誤")
    })
    public ResponseEntity<?> register(
        @Parameter(description = "使用者帳號（3-20 字元）", required = true, example = "newuser")
        @RequestParam String username,
        @Parameter(description = "密碼（至少 6 字元）", required = true, example = "password123")
        @RequestParam String password,
        @Parameter(description = "電子郵件", required = true, example = "user@example.com")
        @RequestParam String email,
        @Parameter(description = "全名", required = false, example = "張三")
        @RequestParam(required = false) String fullName,
        @Parameter(description = "聯絡電話", required = false, example = "0912-345-678")
        @RequestParam(required = false) String phone) {

        // 基本验证
        if (username == null || username.isBlank() ||
                password == null || password.isBlank() ||
                email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("帳號、密碼與電子郵件不得為空");
        }

        // 长度验证
        if (username.length() < 3 || username.length() > 20) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("帳號長度需要在3到20個字元之間");
        }
        if (password.length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("密碼至少需要6個字元");
        }

        // Email 格式驗證
        if (!email.contains("@")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("請輸入有效的電子郵件");
        }

        // 检查用户名是否已存在
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("此帳號已被使用");
        }

        // 檢查 Email 是否已存在
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("此電子郵件已被使用");
        }

        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password)); // 加密
            user.setRole("ROLE_USER"); // 确保角色前缀正确
            user.setEmail(email);
            user.setFullName(fullName != null ? fullName : username); // 如果沒填全名，使用帳號
            user.setPhone(phone);

            userRepository.save(user);
            return ResponseEntity.ok("註冊成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("註冊失敗：系統錯誤");
        }
    }

    @GetMapping("/me")
    @Operation(
        summary = "取得目前登入者資訊",
        description = "回傳目前登入使用者的帳號名稱"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功取得使用者資訊",
            content = @Content(mediaType = "text/plain")
        ),
        @ApiResponse(responseCode = "401", description = "未登入")
    })
    public ResponseEntity<?> currentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("未登入");
        }
        return ResponseEntity.ok(authentication.getName());
    }



}
