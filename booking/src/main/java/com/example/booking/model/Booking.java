package com.example.booking.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "bookings")
@Schema(description = "訂單資訊")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "訂單 ID", example = "1")
    private Long id;

    @Column(name = "check_in", nullable = false)
    @Schema(description = "入住日期", example = "2025-01-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    @Schema(description = "退房日期", example = "2025-01-18", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate checkOut;

    @Column(name = "total_price", precision = 12, scale = 2)
    @Schema(description = "訂單總價", example = "6000.00")
    private BigDecimal totalPrice;

    @Column(name = "booked_quantity", nullable = false)
    @Schema(description = "預訂房間數量", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer bookedQuantity = 1;

    @Column(name = "status", nullable = false)
    @Schema(description = "訂單狀態", example = "CONFIRMED", allowableValues = {"PENDING", "CONFIRMED", "CANCELLED"})
    private String status = "PENDING";

    // === 關聯：房型 ===
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id")
    @JsonIgnoreProperties({"bookings"}) // 不再忽略 accommodation，允許回傳住宿資料
    private RoomType roomType;

    // === 關聯：使用者 ===
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password"}) // 不暴露密碼
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Booking() {}

    public Booking(Long id, LocalDate checkIn, LocalDate checkOut,
                   RoomType roomType, User user, Integer bookedQuantity, BigDecimal totalPrice) {
        this.id = id;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.roomType = roomType;
        this.user = user;
        this.bookedQuantity = bookedQuantity;
        this.totalPrice = totalPrice;
    }

    // === Getters / Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }

    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public Integer getBookedQuantity() { return bookedQuantity; }
    public void setBookedQuantity(Integer bookedQuantity) { this.bookedQuantity = bookedQuantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
