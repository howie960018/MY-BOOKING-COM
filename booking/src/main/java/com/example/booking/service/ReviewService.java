package com.example.booking.service;

import com.example.booking.dto.ReviewDTO;
import com.example.booking.model.Accommodation;
import com.example.booking.model.Review;
import com.example.booking.model.User;
import com.example.booking.repository.AccommodationRepository;
import com.example.booking.repository.ReviewRepository;
import com.example.booking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 評論服務
 */
@Service
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 取得某住宿的所有評論
     */
    public List<ReviewDTO> getReviewsByAccommodationId(Long accommodationId) {
        logger.info("查詢住宿 {} 的評論", accommodationId);
        List<Review> reviews = reviewRepository.findByAccommodationId(accommodationId);
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 新增評論
     */
    @Transactional
    public ReviewDTO addReview(Long accommodationId, String username, BigDecimal rating, String comment) {
        logger.info("用戶 {} 對住宿 {} 新增評論，評分: {}", username, accommodationId, rating);

        // 檢查是否已評論過
        if (reviewRepository.existsByAccommodationIdAndUsername(accommodationId, username)) {
            throw new RuntimeException("您已經評論過此住宿");
        }

        // 驗證評分範圍
        if (rating.compareTo(BigDecimal.ONE) < 0 || rating.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new RuntimeException("評分必須在 1-5 之間");
        }

        // 查詢住宿和用戶
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new RuntimeException("找不到住宿"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("找不到用戶"));

        // 創建評論
        Review review = new Review();
        review.setAccommodation(accommodation);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);

        review = reviewRepository.save(review);

        // 更新住宿的評分和評論數
        updateAccommodationRating(accommodationId);

        logger.info("評論新增成功，ID: {}", review.getId());
        return convertToDTO(review);
    }

    /**
     * 更新住宿的平均評分和評論數
     */
    @Transactional
    public void updateAccommodationRating(Long accommodationId) {
        List<Review> reviews = reviewRepository.findByAccommodationId(accommodationId);

        if (reviews.isEmpty()) {
            return;
        }

        // 計算平均評分
        BigDecimal avgRating = reviews.stream()
                .map(Review::getRating)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(reviews.size()), 2, RoundingMode.HALF_UP);

        // 更新住宿
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new RuntimeException("找不到住宿"));

        accommodation.setRating(avgRating);
        accommodation.setReviewCount(reviews.size());
        accommodationRepository.save(accommodation);

        logger.info("更新住宿 {} 的評分: {}, 評論數: {}", accommodationId, avgRating, reviews.size());
    }

    /**
     * 轉換為 DTO
     */
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setAccommodationId(review.getAccommodation().getId());
        dto.setUsername(review.getUser().getUsername());
        dto.setUserFullName(review.getUser().getFullName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setHelpfulCount(review.getHelpfulCount());
        return dto;
    }
}

