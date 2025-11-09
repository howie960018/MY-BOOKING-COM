package com.example.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 個人資料更新 DTO
 */
public class UserProfileUpdateDTO {

    @NotBlank(message = "全名不能為空")
    @Size(max = 100, message = "全名長度不能超過100字元")
    private String fullName;

    @NotBlank(message = "電子郵件不能為空")
    @Email(message = "電子郵件格式不正確")
    private String email;

    @Size(max = 20, message = "電話長度不能超過20字元")
    private String phone;

    // Constructors
    public UserProfileUpdateDTO() {}

    public UserProfileUpdateDTO(String fullName, String email, String phone) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

