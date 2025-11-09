package com.example.booking.controller;

import com.example.booking.dto.ForgotPasswordDTO;
import com.example.booking.dto.PasswordUpdateDTO;
import com.example.booking.dto.ResetPasswordDTO;
import com.example.booking.dto.UserProfileUpdateDTO;
import com.example.booking.model.User;
import com.example.booking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * 用戶資料管理 Controller
 */
@Controller
@RequestMapping("/user")
public class UserProfileController {

    @Autowired
    private UserService userService;

    /**
     * 個人資料頁面
     */
    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        model.addAttribute("profileDTO", new UserProfileUpdateDTO());
        model.addAttribute("passwordDTO", new PasswordUpdateDTO());
        return "user-profile";
    }

    /**
     * 更新個人資料
     */
    @PostMapping("/profile/update")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("profileDTO") UserProfileUpdateDTO dto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "資料驗證失敗，請檢查輸入");
            return "redirect:/user/profile";
        }

        try {
            userService.updateProfile(userDetails.getUsername(), dto);
            redirectAttributes.addFlashAttribute("success", "個人資料更新成功");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/user/profile";
    }

    /**
     * 更新密碼
     */
    @PostMapping("/password/update")
    public String updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("passwordDTO") PasswordUpdateDTO dto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("passwordError", "資料驗證失敗，請檢查輸入");
            return "redirect:/user/profile";
        }

        try {
            userService.updatePassword(userDetails.getUsername(), dto);
            redirectAttributes.addFlashAttribute("passwordSuccess", "密碼更新成功");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
        }

        return "redirect:/user/profile";
    }

    // ==================== API 端點 ====================

    /**
     * API: 更新個人資料
     */
    @PostMapping("/api/profile")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfileAPI(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserProfileUpdateDTO dto) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = userService.updateProfile(userDetails.getUsername(), dto);
            response.put("success", true);
            response.put("message", "個人資料更新成功");
            response.put("data", user);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API: 更新密碼
     */
    @PostMapping("/api/password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updatePasswordAPI(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordUpdateDTO dto) {

        Map<String, Object> response = new HashMap<>();

        try {
            userService.updatePassword(userDetails.getUsername(), dto);
            response.put("success", true);
            response.put("message", "密碼更新成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 忘記密碼頁面
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());
        return "forgot-password";
    }

    /**
     * 忘記密碼 - 發送重設連結
     */
    @PostMapping("/forgot-password")
    public String forgotPassword(
            @Valid @ModelAttribute ForgotPasswordDTO dto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "請輸入有效的電子郵件");
            return "redirect:/user/forgot-password";
        }

        try {
            String token = userService.generateResetToken(dto.getEmail());
            userService.sendResetPasswordEmail(dto.getEmail(), token);
            redirectAttributes.addFlashAttribute("success",
                "密碼重設連結已發送至您的電子郵件（測試模式：請查看後台日誌）");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/user/forgot-password";
    }

    /**
     * 重設密碼頁面
     */
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            userService.validateResetToken(token);
            model.addAttribute("token", token);
            model.addAttribute("resetPasswordDTO", new ResetPasswordDTO());
            return "reset-password";
        } catch (RuntimeException e) {
            // 令牌無效或過期，重定向到忘記密碼頁面並顯示錯誤
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/forgot-password";
        }
    }

    /**
     * 重設密碼
     */
    @PostMapping("/reset-password")
    public String resetPassword(
            @Valid @ModelAttribute ResetPasswordDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== 重設密碼請求 ===");
        System.out.println("Token: " + dto.getToken());
        System.out.println("新密碼長度: " + (dto.getNewPassword() != null ? dto.getNewPassword().length() : "null"));
        System.out.println("確認密碼長度: " + (dto.getConfirmPassword() != null ? dto.getConfirmPassword().length() : "null"));
        System.out.println("驗證錯誤: " + result.hasErrors());

        if (result.hasErrors()) {
            System.out.println("驗證錯誤詳情: " + result.getAllErrors());
            model.addAttribute("error", "資料驗證失敗，請檢查輸入");
            model.addAttribute("token", dto.getToken());
            return "reset-password";
        }

        try {
            userService.resetPassword(dto.getToken(), dto.getNewPassword(), dto.getConfirmPassword());
            System.out.println("密碼重設成功！");
            redirectAttributes.addFlashAttribute("success", "密碼重設成功，請使用新密碼登入");
            return "redirect:/login";
        } catch (RuntimeException e) {
            System.out.println("密碼重設失敗: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("token", dto.getToken());
            return "reset-password";
        }
    }
}

