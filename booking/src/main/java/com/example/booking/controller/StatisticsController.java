package com.example.booking.controller;

import com.example.booking.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Swagger annotations
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/statistics")
@Tag(name = "Statistics", description = "統計資料 API")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 取得訂單狀態分布
     * 管理員：所有訂單
     * 房東：自己的訂單
     */
    @GetMapping("/order-status")
    @Operation(
        summary = "取得訂單狀態分布",
        description = "回傳待確認、已確認、已取消的訂單數量統計"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得統計資料"),
        @ApiResponse(responseCode = "401", description = "未登入")
    })
    public ResponseEntity<Map<String, Long>> getOrderStatus(Authentication authentication) {
        String username = authentication.getName();
        boolean isOwner = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        Map<String, Long> result;
        if (isOwner) {
            result = statisticsService.getOwnerOrderStatusDistribution(username);
        } else {
            result = statisticsService.getOrderStatusDistribution();
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 取得訂單趨勢
     * @param days 天數，預設 30 天
     */
    @GetMapping("/orders-trend")
    @Operation(
        summary = "取得訂單趨勢",
        description = "回傳近 N 天的訂單數量趨勢，包含新增、已確認、已取消的每日統計"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得趨勢資料"),
        @ApiResponse(responseCode = "401", description = "未登入")
    })
    public ResponseEntity<List<Map<String, Object>>> getOrdersTrend(
            @Parameter(description = "統計天數", example = "30")
            @RequestParam(defaultValue = "30") int days) {

        if (days < 1 || days > 365) {
            days = 30; // 限制範圍
        }

        List<Map<String, Object>> result = statisticsService.getOrdersTrend(days);
        return ResponseEntity.ok(result);
    }

    /**
     * 取得熱門住宿排行（僅管理員）
     * @param limit 取前幾名，預設 5
     */
    @GetMapping("/top-accommodations")
    public ResponseEntity<List<Map<String, Object>>> getTopAccommodations(
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication) {

        // 檢查是否為管理員
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }

        if (limit < 1 || limit > 20) {
            limit = 5;
        }

        List<Map<String, Object>> result = statisticsService.getTopAccommodations(limit);
        return ResponseEntity.ok(result);
    }

    /**
     * 取得月度營收
     * 管理員：所有營收
     * 房東：自己的營收
     * @param months 月數，預設 12 個月
     */
    @GetMapping("/monthly-revenue")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyRevenue(
            @RequestParam(defaultValue = "12") int months,
            Authentication authentication) {

        if (months < 1 || months > 24) {
            months = 12;
        }

        String username = authentication.getName();
        boolean isOwner = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        List<Map<String, Object>> result;
        if (isOwner) {
            result = statisticsService.getOwnerMonthlyRevenue(username, months);
        } else {
            result = statisticsService.getMonthlyRevenue(months);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 取得房東的住宿營收佔比（僅房東）
     */
    @GetMapping("/accommodation-revenue")
    public ResponseEntity<List<Map<String, Object>>> getAccommodationRevenue(
            Authentication authentication) {

        boolean isOwner = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        if (!isOwner) {
            return ResponseEntity.status(403).build();
        }

        String username = authentication.getName();
        List<Map<String, Object>> result = statisticsService.getOwnerAccommodationRevenue(username);
        return ResponseEntity.ok(result);
    }

    /**
     * 取得房東的房型銷售排行（僅房東）
     */
    @GetMapping("/room-type-sales")
    public ResponseEntity<List<Map<String, Object>>> getRoomTypeSales(
            Authentication authentication) {

        boolean isOwner = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        if (!isOwner) {
            return ResponseEntity.status(403).build();
        }

        String username = authentication.getName();
        List<Map<String, Object>> result = statisticsService.getOwnerRoomTypeSales(username);
        return ResponseEntity.ok(result);
    }

    /**
     * 取得房東的入住率趨勢（僅房東）
     * @param days 天數，預設 30 天
     */
    @GetMapping("/occupancy-rate")
    public ResponseEntity<List<Map<String, Object>>> getOccupancyRate(
            @RequestParam(defaultValue = "30") int days,
            Authentication authentication) {

        boolean isOwner = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        if (!isOwner) {
            return ResponseEntity.status(403).build();
        }

        if (days < 1 || days > 90) {
            days = 30;
        }

        String username = authentication.getName();
        List<Map<String, Object>> result = statisticsService.getOwnerOccupancyRate(username, days);
        return ResponseEntity.ok(result);
    }

    /**
     * 取得管理員儀表板的所有統計資料（一次性取得）
     */
    @GetMapping("/admin/dashboard")
    @Operation(
        summary = "取得管理員儀表板資料",
        description = "一次性取得所有管理員儀表板需要的統計資料，包含訂單狀態、趨勢、熱門住宿、月度營收"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得儀表板資料"),
        @ApiResponse(responseCode = "401", description = "未登入"),
        @ApiResponse(responseCode = "403", description = "需要管理員權限")
    })
    public ResponseEntity<Map<String, Object>> getAdminDashboard(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }

        Map<String, Object> dashboard = Map.of(
            "orderStatus", statisticsService.getOrderStatusDistribution(),
            "ordersTrend", statisticsService.getOrdersTrend(30),
            "topAccommodations", statisticsService.getTopAccommodations(5),
            "monthlyRevenue", statisticsService.getMonthlyRevenue(12)
        );

        return ResponseEntity.ok(dashboard);
    }

    /**
     * 取得房東儀表板的所有統計資料（一次性取得）
     */
    @GetMapping("/owner/dashboard")
    public ResponseEntity<Map<String, Object>> getOwnerDashboard(Authentication authentication) {
        boolean isOwner = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        if (!isOwner) {
            return ResponseEntity.status(403).build();
        }

        String username = authentication.getName();
        Map<String, Object> dashboard = Map.of(
            "orderStatus", statisticsService.getOwnerOrderStatusDistribution(username),
            "accommodationRevenue", statisticsService.getOwnerAccommodationRevenue(username),
            "roomTypeSales", statisticsService.getOwnerRoomTypeSales(username),
            "occupancyRate", statisticsService.getOwnerOccupancyRate(username, 30)
        );

        return ResponseEntity.ok(dashboard);
    }
}
