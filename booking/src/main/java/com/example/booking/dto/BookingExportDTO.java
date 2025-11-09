package com.example.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingExportDTO {
    private Long id;
    private LocalDateTime createdAt;
    private String customerName;
    private String accommodationName;
    private String roomTypeName;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Long nights;
    private Integer quantity;
    private BigDecimal pricePerNight;
    private BigDecimal totalPrice;
    private String status;

    // Constructor
    public BookingExportDTO() {}

    public BookingExportDTO(Long id, LocalDateTime createdAt, String customerName,
                            String accommodationName, String roomTypeName,
                            LocalDate checkIn, LocalDate checkOut,
                            Integer quantity, BigDecimal pricePerNight,
                            BigDecimal totalPrice, String status) {
        this.id = id;
        this.createdAt = createdAt;
        this.customerName = customerName;
        this.accommodationName = accommodationName;
        this.roomTypeName = roomTypeName;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.quantity = quantity;
        this.pricePerNight = pricePerNight;
        this.totalPrice = totalPrice;
        this.status = status;
        // Calculate nights
        this.nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getAccommodationName() { return accommodationName; }
    public void setAccommodationName(String accommodationName) { this.accommodationName = accommodationName; }

    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }

    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }

    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }

    public Long getNights() { return nights; }
    public void setNights(Long nights) { this.nights = nights; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Status text conversion
    public String getStatusText() {
        switch (status) {
            case "CONFIRMED": return "已確認";
            case "CANCELLED": return "已取消";
            case "PENDING": return "待確認";
            default: return status;
        }
    }
}
