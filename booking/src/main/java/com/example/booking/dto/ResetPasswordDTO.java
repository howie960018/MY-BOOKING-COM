package com.example.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 重設密碼 DTO
 */
public class ResetPasswordDTO {

    @NotBlank(message = "重設令牌不能為空")
    private String token;

    @NotBlank(message = "新密碼不能為空")
    @Size(min = 6, message = "新密碼長度至少6個字元")
    private String newPassword;

    @NotBlank(message = "確認密碼不能為空")
    private String confirmPassword;

    // Constructors
    public ResetPasswordDTO() {}

    public ResetPasswordDTO(String token, String newPassword, String confirmPassword) {
        this.token = token;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}

