package com.example.booking.controller;

import com.example.booking.model.Accommodation;
import com.example.booking.model.Booking;
import com.example.booking.model.RoomType;
import com.example.booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/accommodations")
    public List<Accommodation> getOwnerAccommodations(Authentication authentication) {
        return bookingService.getAccommodationsForOwner(authentication.getName());
    }

    @PostMapping("/accommodations")
    public Accommodation createAccommodation(
            @RequestBody Accommodation accommodation,
            Authentication authentication) {
        return bookingService.createAccommodation(accommodation, authentication.getName());
    }

    @PutMapping("/accommodations/{id}")
    public Accommodation updateAccommodation(
            @PathVariable Long id,
            @RequestBody Accommodation accommodation,
            Authentication authentication) {
        return bookingService.updateAccommodation(id, accommodation, authentication.getName());
    }

    @DeleteMapping("/accommodations/{id}")
    public void deleteAccommodation(
            @PathVariable Long id,
            Authentication authentication) {
        bookingService.deleteAccommodation(id, authentication.getName());
    }

    @PostMapping("/accommodations/{accId}/room-types")
    public RoomType createRoomType(
            @PathVariable Long accId,
            @RequestBody RoomType roomType,
            Authentication authentication) {
        return bookingService.createRoomType(accId, roomType, authentication.getName());
    }

    @GetMapping("/accommodations/{accId}/room-types")
    public List<RoomType> getRoomTypes(
            @PathVariable Long accId,
            Authentication authentication) {
        // 使用現有的 checkAccommodationOwnership 方法來驗證權限
        bookingService.checkAccommodationOwnership(accId, authentication.getName());
        return bookingService.getRoomTypesForAccommodation(accId);
    }

    @PutMapping("/room-types/{id}")
    public RoomType updateRoomType(
            @PathVariable Long id,
            @RequestBody RoomType roomType,
            Authentication authentication) {
        return bookingService.updateRoomType(id, roomType, authentication.getName());
    }

    @DeleteMapping("/room-types/{id}")
    public void deleteRoomType(
            @PathVariable Long id,
            Authentication authentication) {
        bookingService.deleteRoomType(id, authentication.getName());
    }

    // 取得房東的所有訂單
    @GetMapping("/bookings")
    public List<Booking> getOwnerBookings(Authentication authentication) {
        return bookingService.getBookingsForOwner(authentication.getName());
    }

    @PostMapping("/bookings/{id}/confirm")
    public ResponseEntity<Booking> confirmBooking(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            Booking booking = bookingService.confirmBookingByOwner(id, authentication.getName());
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            Booking booking = bookingService.cancelBookingByOwner(id, authentication.getName());
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 取得房東的統計數據
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getOwnerStats(Authentication authentication) {
        try {
            String username = authentication.getName();

            // 獲取房東的所有住宿和訂單
            List<Accommodation> accommodations = bookingService.getAccommodationsForOwner(username);
            List<Booking> bookings = bookingService.getBookingsForOwner(username);

            // 計算統計數據
            Map<String, Object> stats = new HashMap<>();

            // 基本統計
            stats.put("accommodationCount", accommodations.size());

            // 計算總房型數
            int totalRoomTypes = accommodations.stream()
                    .mapToInt(acc -> bookingService.getRoomTypesForAccommodation(acc.getId()).size())
                    .sum();
            stats.put("roomTypeCount", totalRoomTypes);

            // 待確認訂單數
            long pendingBookings = bookings.stream()
                    .filter(b -> "PENDING".equals(b.getStatus()))
                    .count();
            stats.put("pendingBookings", pendingBookings);

            // 計算本月營收
            YearMonth currentMonth = YearMonth.now();
            double monthlyRevenue = bookings.stream()
                    .filter(b -> "CONFIRMED".equals(b.getStatus()))
                    .filter(b -> {
                        LocalDate bookingDate = b.getCheckIn();
                        return bookingDate != null &&
                               YearMonth.from(bookingDate).equals(currentMonth);
                    })
                    .mapToDouble(b -> b.getTotalPrice() != null ? b.getTotalPrice().doubleValue() : 0.0)
                    .sum();
            stats.put("monthlyRevenue", monthlyRevenue);

            // 訂單狀態分布
            Map<String, Long> statusData = new HashMap<>();
            statusData.put("pending", bookings.stream()
                    .filter(b -> "PENDING".equals(b.getStatus()))
                    .count());
            statusData.put("confirmed", bookings.stream()
                    .filter(b -> "CONFIRMED".equals(b.getStatus()))
                    .count());
            statusData.put("cancelled", bookings.stream()
                    .filter(b -> "CANCELLED".equals(b.getStatus()))
                    .count());
            stats.put("bookingStatusData", statusData);

            // 最近6個月的營收趨勢
            Map<String, Object> revenueData = new HashMap<>();
            List<String> labels = new ArrayList<>();
            List<Double> values = new ArrayList<>();

            for (int i = 5; i >= 0; i--) {
                YearMonth month = YearMonth.now().minusMonths(i);
                labels.add(month.getMonth().toString().substring(0, 3));

                double revenue = bookings.stream()
                        .filter(b -> "CONFIRMED".equals(b.getStatus()))
                        .filter(b -> {
                            LocalDate date = b.getCheckIn();
                            return date != null && YearMonth.from(date).equals(month);
                        })
                        .mapToDouble(b -> b.getTotalPrice() != null ? b.getTotalPrice().doubleValue() : 0.0)
                        .sum();
                values.add(revenue);
            }

            revenueData.put("labels", labels);
            revenueData.put("values", values);
            stats.put("revenueData", revenueData);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
