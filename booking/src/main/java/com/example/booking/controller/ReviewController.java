package com.example.booking.controller;

import com.example.booking.dto.ReviewDTO;
import com.example.booking.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 評論 API 控制器
 */
@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "評論管理 API")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 取得某住宿的所有評論
     */
    @GetMapping("/accommodation/{accommodationId}")
    @Operation(summary = "取得住宿的所有評論")
    public List<ReviewDTO> getReviews(@PathVariable Long accommodationId) {
        return reviewService.getReviewsByAccommodationId(accommodationId);
    }

    /**
     * 新增評論
     */
    @PostMapping("/accommodation/{accommodationId}")
    @Operation(summary = "新增評論")
    public ResponseEntity<Map<String, Object>> addReview(
            @PathVariable Long accommodationId,
            @RequestParam BigDecimal rating,
            @RequestParam(required = false) String comment,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            String username = authentication.getName();
            ReviewDTO review = reviewService.addReview(accommodationId, username, rating, comment);

            response.put("success", true);
            response.put("message", "評論新增成功");
            response.put("review", review);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

