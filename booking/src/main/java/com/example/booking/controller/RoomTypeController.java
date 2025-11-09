package com.example.booking.controller;

import com.example.booking.model.RoomType;
import com.example.booking.repository.RoomTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-types")
@Tag(name = "Room Types", description = "房型資訊查詢 API")
public class RoomTypeController {

    @Autowired
    private RoomTypeRepository roomTypeRepo;

    @GetMapping
    @Operation(
        summary = "取得所有房型",
        description = "回傳系統中所有房型的列表（管理用途）"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得房型列表"),
        @ApiResponse(responseCode = "500", description = "伺服器錯誤")
    })
    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepo.findAll();
    }

    @GetMapping("/by-accommodation/{accId}")
    @Operation(
        summary = "查詢住宿的房型",
        description = "根據住宿 ID 取得該住宿底下的所有房型清單"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得房型列表"),
        @ApiResponse(responseCode = "404", description = "找不到該住宿")
    })
    public List<RoomType> getByAccommodation(
        @Parameter(description = "住宿 ID", required = true, example = "1")
        @PathVariable Long accId) {
        return roomTypeRepo.findByAccommodationId(accId);
    }
}
