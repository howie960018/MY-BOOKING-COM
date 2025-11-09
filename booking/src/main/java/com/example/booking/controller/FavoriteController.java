package com.example.booking.controller;

import com.example.booking.model.Accommodation;
import com.example.booking.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏功能 Controller
 */
@Controller
@RequestMapping("/user/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 我的收藏頁面
     */
    @GetMapping
    public String favoritesPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<Accommodation> favorites = favoriteService.getFavorites(userDetails.getUsername());
        model.addAttribute("favorites", favorites);
        model.addAttribute("favoriteCount", favorites.size());
        return "user-favorites";
    }

    /**
     * API: 切換收藏狀態
     */
    @PostMapping("/api/toggle/{accommodationId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accommodationId) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean isFavorited = favoriteService.toggleFavorite(userDetails.getUsername(), accommodationId);
            response.put("success", true);
            response.put("isFavorited", isFavorited);
            response.put("message", isFavorited ? "已添加收藏" : "已取消收藏");
            response.put("favoriteCount", favoriteService.getFavoriteCount(userDetails.getUsername()));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API: 檢查是否已收藏
     */
    @GetMapping("/api/check/{accommodationId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accommodationId) {

        Map<String, Object> response = new HashMap<>();
        boolean isFavorited = favoriteService.isFavorited(userDetails.getUsername(), accommodationId);

        response.put("isFavorited", isFavorited);
        return ResponseEntity.ok(response);
    }

    /**
     * API: 取得收藏列表
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFavorites(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();
        List<Accommodation> favorites = favoriteService.getFavorites(userDetails.getUsername());

        response.put("favorites", favorites);
        response.put("count", favorites.size());
        return ResponseEntity.ok(response);
    }

    /**
     * API: 加入收藏
     */
    @PostMapping("/api/add/{accommodationId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accommodationId) {

        Map<String, Object> response = new HashMap<>();

        try {
            favoriteService.addFavorite(userDetails.getUsername(), accommodationId);
            response.put("success", true);
            response.put("message", "已添加收藏");
            response.put("favoriteCount", favoriteService.getFavoriteCount(userDetails.getUsername()));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API: 移除收藏
     */
    @DeleteMapping("/api/{accommodationId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accommodationId) {

        Map<String, Object> response = new HashMap<>();

        try {
            favoriteService.removeFavorite(userDetails.getUsername(), accommodationId);
            response.put("success", true);
            response.put("message", "已取消收藏");
            response.put("favoriteCount", favoriteService.getFavoriteCount(userDetails.getUsername()));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

