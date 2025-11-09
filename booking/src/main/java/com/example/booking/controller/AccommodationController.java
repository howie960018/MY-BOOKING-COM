package com.example.booking.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.example.booking.model.Accommodation;
import com.example.booking.model.RoomType;
import com.example.booking.service.BookingService;

// Swagger annotations
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/accommodations")
@Tag(name = "Accommodations", description = "住宿資訊查詢 API")
public class AccommodationController {

    private final BookingService bookingService;

    public AccommodationController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    @Operation(
        summary = "取得所有住宿",
        description = "回傳系統中所有可用的住宿列表，支援多種排序方式"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得住宿列表"),
        @ApiResponse(responseCode = "500", description = "伺服器錯誤")
    })
    public List<Accommodation> list(
        @Parameter(description = "排序方式：price_asc(價格低到高), price_desc(價格高到低), rating(評分), popularity(熱門), distance(距離), name_asc(名稱A-Z), name_desc(名稱Z-A)", example = "price_asc")
        @RequestParam(required = false) String sortBy
    ) {
        return bookingService.getAllAccommodations(sortBy);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "取得單一住宿",
        description = "根據住宿 ID 查詢詳細資訊"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得住宿資訊"),
        @ApiResponse(responseCode = "404", description = "找不到該住宿")
    })
    public Accommodation getById(
        @Parameter(description = "住宿 ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        return bookingService.getAllAccommodations()
                .stream()
                .filter(acc -> acc.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @GetMapping("/search")
    @Operation(
        summary = "搜尋住宿",
        description = "根據地點或名稱關鍵字搜尋住宿，支援模糊搜尋、排序功能"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得搜尋結果"),
        @ApiResponse(responseCode = "400", description = "搜尋參數錯誤")
    })
    public List<Accommodation> searchAccommodations(
        @Parameter(description = "搜尋關鍵字（地點或名稱）", example = "台北")
        @RequestParam(required = false) String query,
        @Parameter(description = "地點關鍵字（保留向下兼容）", example = "台北")
        @RequestParam(required = false) String location,
        @Parameter(description = "入住日期", example = "2025-11-10")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
        @Parameter(description = "退房日期", example = "2025-11-12")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
        @Parameter(description = "入住人數", example = "2")
        @RequestParam(required = false) Integer guests,
        @Parameter(description = "排序方式：price_asc(價格低到高), price_desc(價格高到低), rating(評分), popularity(熱門), distance(距離)", example = "price_asc")
        @RequestParam(required = false) String sortBy
    ) {
        // 統一使用 query 參數，但保留 location 以向下兼容
        String searchKeyword = query != null ? query : location;

        // 如果有日期，執行可用性搜尋
        if (checkIn != null && checkOut != null) {
            List<Accommodation> available = bookingService.getAvailableAccommodations(checkIn, checkOut);

            // 如果還有關鍵字，進一步篩選
            if (searchKeyword != null && !searchKeyword.isBlank()) {
                String keyword = searchKeyword.toLowerCase().trim();
                available = available.stream()
                    .filter(acc ->
                        acc.getLocation().toLowerCase().contains(keyword) ||
                        acc.getName().toLowerCase().contains(keyword)
                    )
                    .collect(java.util.stream.Collectors.toList());
            }

            return bookingService.sortAccommodations(available, sortBy);
        }

        // 只有關鍵字的情況
        if (searchKeyword != null && !searchKeyword.isBlank()) {
            return bookingService.searchByLocationOrName(searchKeyword, sortBy);
        }

        // 沒有任何條件，返回所有住宿
        return bookingService.getAllAccommodations(sortBy);
    }

    @GetMapping("/available")
    @Operation(
        summary = "查詢可用住宿",
        description = "根據入住和退房日期查詢該時段內可預訂的住宿"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得可用住宿"),
        @ApiResponse(responseCode = "400", description = "日期參數錯誤")
    })
    public List<Accommodation> getAvailable(
        @Parameter(description = "入住日期", required = true, example = "2025-01-15")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
        @Parameter(description = "退房日期", required = true, example = "2025-01-18")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
        @Parameter(description = "排序方式：price_asc(價格低到高), price_desc(價格高到低), rating(評分), popularity(熱門), distance(距離)", example = "price_asc")
        @RequestParam(required = false) String sortBy
    ) {
        System.out.println("🔍 /available 端點被調用 - checkIn: " + checkIn + ", checkOut: " + checkOut + ", sortBy: " + sortBy);
        List<Accommodation> available = bookingService.getAvailableAccommodations(checkIn, checkOut);
        System.out.println("📊 可用住宿數量: " + available.size());
        List<Accommodation> sorted = bookingService.sortAccommodations(available, sortBy);
        System.out.println("✅ 排序後返回 " + sorted.size() + " 筆資料");
        return sorted;
    }

    @GetMapping("/{id}/room-types")
    @Operation(
        summary = "取得住宿的房型列表",
        description = "查詢指定住宿的所有可用房型"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得房型列表"),
        @ApiResponse(responseCode = "404", description = "找不到該住宿")
    })
    public List<RoomType> getRoomTypesByAccommodation(
        @Parameter(description = "住宿 ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        return bookingService.getRoomTypesForAccommodation(id);
    }
}
