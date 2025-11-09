package com.example.booking.controller;

import com.example.booking.model.Accommodation;
import com.example.booking.model.RoomType;
import com.example.booking.model.User;
import com.example.booking.repository.AccommodationRepository;
import com.example.booking.repository.UserRepository;
import com.example.booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/accommodations")
public class AdminAccommodationController {

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingService bookingService;

    // --- 住宿管理 (Admin) ---

    @PostMapping
    public ResponseEntity<?> createAccommodation(@RequestBody Accommodation accommodation) {
        // 管理員新增的住宿，預設擁有者(owner)為 admin 自己
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("找不到 admin 用戶"));

        accommodation.setOwner(adminUser);

        Accommodation saved = accommodationRepository.save(accommodation);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccommodation(@PathVariable Long id,
                                                 @RequestBody Accommodation accommodation) {
        // 管理員更新住宿 (不檢查所有權)
        return accommodationRepository.findById(id)
                .map(existing -> {
                    existing.setName(accommodation.getName());
                    existing.setLocation(accommodation.getLocation());
                    existing.setDescription(accommodation.getDescription());
                    existing.setPricePerNight(accommodation.getPricePerNight());
                    existing.setImageUrl(accommodation.getImageUrl()); // ✅ 添加圖片 URL 更新
                    return ResponseEntity.ok(accommodationRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccommodation(@PathVariable Long id) {
        // 管理員刪除住宿 (不檢查所有權)
        if (!accommodationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // 注意：這裡直接刪除，BookingService 中的 deleteAccommodation 有檢查邏輯
        accommodationRepository.deleteById(id);
        return ResponseEntity.ok("刪除成功");
    }

    // (管理員取得所有住宿 - /api/accommodations 是公開的，所以不需要)

    // --- 房型管理 (Admin) ---

    @GetMapping("/{accId}/room-types")
    public List<RoomType> getRoomTypes(@PathVariable Long accId) {
        // 使用我們在 BookingService 中新增的 Admin 專用方法
        return bookingService.getRoomTypesForAdmin(accId);
    }

    @PostMapping("/{accId}/room-types")
    public RoomType createRoomType(
            @PathVariable Long accId,
            @RequestBody RoomType roomType) {
        // 使用我們在 BookingService 中新增的 Admin 專用方法
        return bookingService.createRoomTypeForAdmin(accId, roomType);
    }
}
