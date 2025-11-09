package com.example.booking.controller;

import com.example.booking.model.Accommodation;
import com.example.booking.model.Booking;
import com.example.booking.model.RoomType;
import com.example.booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
