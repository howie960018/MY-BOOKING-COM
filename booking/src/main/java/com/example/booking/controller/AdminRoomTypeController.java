package com.example.booking.controller;

import com.example.booking.model.RoomType;
import com.example.booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/room-types")
public class AdminRoomTypeController {

    @Autowired
    private BookingService bookingService;

    @PutMapping("/{id}")
    public RoomType updateRoomType(@PathVariable Long id, @RequestBody RoomType roomType) {
        // 使用我們在 BookingService 中新增的 Admin 專用方法
        return bookingService.updateRoomTypeForAdmin(id, roomType);
    }

    @DeleteMapping("/{id}")
    public void deleteRoomType(@PathVariable Long id) {
        // 使用我們在 BookingService 中新增的 Admin 專用方法
        bookingService.deleteRoomTypeForAdmin(id);
    }
}
