package com.example.booking.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "accommodations")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 避免 Lazy 加載報錯
@Schema(description = "住宿資訊")
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "住宿 ID", example = "1")
    private Long id;

    @Schema(description = "住宿名稱", example = "日安旅館", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "地點", example = "台北市中山區", requiredMode = Schema.RequiredMode.REQUIRED)
    private String location;

    @Schema(description = "描述", example = "溫馨雙人房，交通便利")
    private String description;

    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    @Schema(description = "每晚價格", example = "2000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal pricePerNight = BigDecimal.valueOf(1000); // 預設1000元

    @Column(length = 500)
    @Schema(description = "設施", example = "WiFi, 停車場, 早餐")
    private String amenities; // 例："WiFi, 停車場, 早餐"

    @Column(name = "rating", precision = 3, scale = 2)
    @Schema(description = "評分", example = "4.5")
    private BigDecimal rating; // 評分 0-5

    @Column(name = "review_count")
    @Schema(description = "評論數量", example = "128")
    private Integer reviewCount = 0; // 評論數量

    @Column(name = "booking_count")
    @Schema(description = "訂房次數", example = "256")
    private Integer bookingCount = 0; // 訂房次數（用於推薦排序）

    @Column(name = "distance_from_center", precision = 5, scale = 2)
    @Schema(description = "距離市中心距離（公里）", example = "2.5")
    private BigDecimal distanceFromCenter; // 距離市中心（公里）

    @Column(name = "image_url", length = 1000)
    @Schema(description = "住宿主圖片URL", example = "https://example.com/hotel1.jpg")
    private String imageUrl; // 主圖片

    @Column(name = "images", length = 2000)
    @Schema(description = "更多圖片URLs（逗號分隔）", example = "img1.jpg,img2.jpg,img3.jpg")
    private String images; // 多張圖片，用逗號分隔

    @Column(name = "nearby_attractions", length = 1000)
    @Schema(description = "附近景點", example = "台北101, 信義商圈, 國父紀念館")
    private String nearbyAttractions; // 附近景點

    @Column(name = "address", length = 500)
    @Schema(description = "詳細地址", example = "台北市信義區信義路五段7號")
    private String address; // 詳細地址

    @Column(name = "phone", length = 50)
    @Schema(description = "聯絡電話", example = "02-1234-5678")
    private String phone; // 聯絡電話

    // === 新增：與 RoomType 的一對多關聯 ===
    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"accommodation", "bookings"}) // 防止雙向遞迴
    private List<RoomType> roomTypes;

    // === 新增：與 User 的多對一關聯 ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User owner;

    // === 建構子 ===
    public Accommodation() {}

    public Accommodation(Long id, String name, String location, String description, BigDecimal pricePerNight) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.description = description;
        this.pricePerNight = pricePerNight;
    }

    // === Getter / Setter ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }

    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

    public Integer getBookingCount() { return bookingCount; }
    public void setBookingCount(Integer bookingCount) { this.bookingCount = bookingCount; }

    public BigDecimal getDistanceFromCenter() { return distanceFromCenter; }
    public void setDistanceFromCenter(BigDecimal distanceFromCenter) { this.distanceFromCenter = distanceFromCenter; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public String getNearbyAttractions() { return nearbyAttractions; }
    public void setNearbyAttractions(String nearbyAttractions) { this.nearbyAttractions = nearbyAttractions; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<RoomType> getRoomTypes() { return roomTypes; }
    public void setRoomTypes(List<RoomType> roomTypes) { this.roomTypes = roomTypes; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
}
