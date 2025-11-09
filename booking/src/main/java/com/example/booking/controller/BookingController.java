package com.example.booking.controller;

import com.example.booking.model.Booking;
import com.example.booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Swagger annotations
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "訂單管理 API")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // === 1. 舊版：以住宿 ID 下單 ===
    @PostMapping
    @Operation(
        summary = "建立訂單（以住宿 ID）",
        description = "根據住宿 ID、入住/退房日期建立新訂單。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "訂單建立成功"),
        @ApiResponse(responseCode = "400", description = "訂單參數錯誤或庫存不足"),
        @ApiResponse(responseCode = "401", description = "未登入")
    })
    public ResponseEntity<Booking> bookByAccommodation(
            @Parameter(description = "住宿 ID", required = true, example = "1")
            @RequestParam Long accommodationId,
            @Parameter(description = "入住日期", required = true, example = "2025-01-15")
            @RequestParam String checkIn,
            @Parameter(description = "退房日期", required = true, example = "2025-01-18")
            @RequestParam String checkOut
    ) {
        LocalDate in = LocalDate.parse(checkIn);
        LocalDate out = LocalDate.parse(checkOut);
        Booking booking = bookingService.book(accommodationId, in, out);
        return ResponseEntity.ok(booking);
    }

    // === 2. 新版：以房型 ID 下單（支援數量與庫存檢查）===
    @PostMapping("/by-room-type")
    @Operation(
        summary = "建立訂單",
        description = "根據房型 ID、入住/退房日期與房間數量建立新訂單。系統會自動檢查庫存並計算總價。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "訂單建立成功"),
        @ApiResponse(responseCode = "400", description = "訂單參數錯誤或庫存不足"),
        @ApiResponse(responseCode = "401", description = "未登入")
    })
    public ResponseEntity<Booking> bookByRoomType(
            @Parameter(description = "房型 ID", required = true, example = "1")
            @RequestParam Long roomTypeId,
            @Parameter(description = "入住日期", required = true, example = "2025-01-15")
            @RequestParam String checkIn,
            @Parameter(description = "退房日期", required = true, example = "2025-01-18")
            @RequestParam String checkOut,
            @Parameter(description = "房間數量", required = false, example = "2")
            @RequestParam(defaultValue = "1") Integer quantity
    ) {
        LocalDate in = LocalDate.parse(checkIn);
        LocalDate out = LocalDate.parse(checkOut);
        Booking booking = bookingService.bookByRoomType(roomTypeId, in, out, quantity);
        return ResponseEntity.ok(booking);
    }

    // === 2-2. 訂房 API（返回 JSON 格式，供前端使用）===
    @PostMapping("/book-by-room-type")
    @Operation(
        summary = "建立訂單（前端專用）",
        description = "根據房型 ID、入住/退房日期與房間數量建立新訂單，返回 JSON 格式回應。"
    )
    public ResponseEntity<Map<String, Object>> bookByRoomTypeJson(
            @RequestParam Long roomTypeId,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam(defaultValue = "1") Integer quantity
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            LocalDate in = LocalDate.parse(checkIn);
            LocalDate out = LocalDate.parse(checkOut);
            Booking booking = bookingService.bookByRoomType(roomTypeId, in, out, quantity);

            response.put("success", true);
            response.put("message", "訂房成功");
            response.put("bookingId", booking.getId());
            response.put("totalPrice", booking.getTotalPrice());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // === 3. 使用者查自己的訂單（自動取登入帳號） ===
    @GetMapping
    @Operation(
        summary = "取得訂單列表",
        description = "取得當前登入使用者的訂單列表。管理員可看所有訂單，房東可看自己住宿的訂單，一般用戶只能看自己的訂單。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得訂單列表"),
        @ApiResponse(responseCode = "401", description = "未登入")
    })
    public ResponseEntity<List<Booking>> getBookings(Authentication authentication) {
        String username = authentication.getName();
        // 根據用戶角色返回不同的訂單列表
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"))) {
            return ResponseEntity.ok(bookingService.getBookingsForOwner(username));
        } else {
            return ResponseEntity.ok(bookingService.getBookingsForUser(username));
        }
    }

    // === 4. 管理員查所有訂單 ===
    @GetMapping("/admin/all")
    @Operation(
        summary = "取得所有訂單（管理員）",
        description = "管理員取得系統所有訂單列表。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得訂單列表"),
        @ApiResponse(responseCode = "403", description = "需要管理員權限")
    })
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // === 5.（選用）Admin 可查指定使用者的訂單 ===
    @GetMapping("/user/{username}")
    @Operation(
        summary = "取得指定使用者訂單（管理員）",
        description = "管理員可查詢指定使用者的所有訂單。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得訂單列表"),
        @ApiResponse(responseCode = "403", description = "需要管理員權限")
    })
    public ResponseEntity<List<Booking>> getBookingsForUser(
            @Parameter(description = "使用者名稱", required = true, example = "user")
            @PathVariable String username) {
        return ResponseEntity.ok(bookingService.getBookingsForUser(username));
    }

    // === 6. 一般用戶取消訂單（限自己的訂單） ===
    @DeleteMapping("/{id}")
    @Operation(
        summary = "取消訂單",
        description = "一般用戶取消自己的訂單。只能取消尚未開始入住的訂單。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "取消成功"),
        @ApiResponse(responseCode = "400", description = "訂單無法取消（已開始或已取消）"),
        @ApiResponse(responseCode = "403", description = "無權限取消此訂單"),
        @ApiResponse(responseCode = "404", description = "找不到訂單")
    })
    public ResponseEntity<Booking> cancelBooking(
            @Parameter(description = "訂單 ID", required = true, example = "1")
            @PathVariable Long id, Authentication authentication) {
        Booking updated = bookingService.cancelBooking(id, authentication.getName());
        return ResponseEntity.ok(updated);
    }

    // === 7. 管理員取消任意訂單 ===
    @DeleteMapping("/admin/{id}")
    @Operation(
        summary = "管理員取消訂單",
        description = "管理員可取消任意訂單。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "取消成功"),
        @ApiResponse(responseCode = "403", description = "需要管理員權限"),
        @ApiResponse(responseCode = "404", description = "找不到訂單")
    })
    public ResponseEntity<Booking> adminCancelBooking(
            @Parameter(description = "訂單 ID", required = true, example = "1")
            @PathVariable Long id, Authentication authentication) {
        // 檢查是否為管理員
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }
        Booking updated = bookingService.cancelBookingByAdmin(id);
        return ResponseEntity.ok(updated);
    }
}
