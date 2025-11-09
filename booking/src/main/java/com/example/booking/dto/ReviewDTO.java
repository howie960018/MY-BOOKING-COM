package com.example.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 評論 DTO
 */
public class ReviewDTO {
    private Long id;
    private Long accommodationId;
    private String username;
    private String userFullName;
    private BigDecimal rating;
    private String comment;
    private LocalDateTime createdAt;
    private Integer helpfulCount;

    // === Constructors ===
    public ReviewDTO() {}

    public ReviewDTO(Long id, Long accommodationId, String username, String userFullName,
                     BigDecimal rating, String comment, LocalDateTime createdAt, Integer helpfulCount) {
        this.id = id;
        this.accommodationId = accommodationId;
        this.username = username;
        this.userFullName = userFullName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.helpfulCount = helpfulCount;
    }

    // === Getters and Setters ===
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccommodationId() {
        return accommodationId;
    }

    public void setAccommodationId(Long accommodationId) {
        this.accommodationId = accommodationId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(Integer helpfulCount) {
        this.helpfulCount = helpfulCount;
    }
}

