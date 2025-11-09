package com.example.booking.service;

import com.example.booking.dto.ReviewDTO;
import com.example.booking.model.Accommodation;
import com.example.booking.model.Review;
import com.example.booking.model.User;
import com.example.booking.repository.AccommodationRepository;
import com.example.booking.repository.ReviewRepository;
import com.example.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * ReviewService 單元測試
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("評論服務測試")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User testUser;
    private Accommodation testAccommodation;
    private Review testReview;

    @BeforeEach
    void setUp() {
        // 準備測試資料
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setFullName("測試用戶");
        testUser.setEmail("test@example.com");

        testAccommodation = new Accommodation();
        testAccommodation.setId(1L);
        testAccommodation.setName("測試住宿");
        testAccommodation.setLocation("台北");
        testAccommodation.setPricePerNight(new BigDecimal("2000"));

        testReview = new Review();
        testReview.setId(1L);
        testReview.setAccommodation(testAccommodation);
        testReview.setUser(testUser);
        testReview.setRating(new BigDecimal("4.5"));
        testReview.setComment("很棒的住宿體驗");
        testReview.setHelpfulCount(5);
    }

    @Test
    @DisplayName("取得住宿的所有評論 - 成功")
    void getReviewsByAccommodationId_Success() {
        // Given
        List<Review> reviews = Arrays.asList(testReview);
        when(reviewRepository.findByAccommodationId(1L)).thenReturn(reviews);

        // When
        List<ReviewDTO> result = reviewService.getReviewsByAccommodationId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals("測試用戶", result.get(0).getUserFullName());
        assertEquals(new BigDecimal("4.5"), result.get(0).getRating());
        verify(reviewRepository, times(1)).findByAccommodationId(1L);
    }

    @Test
    @DisplayName("取得住宿的所有評論 - 無評論")
    void getReviewsByAccommodationId_Empty() {
        // Given
        when(reviewRepository.findByAccommodationId(1L)).thenReturn(Collections.emptyList());

        // When
        List<ReviewDTO> result = reviewService.getReviewsByAccommodationId(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reviewRepository, times(1)).findByAccommodationId(1L);
    }

    @Test
    @DisplayName("新增評論 - 成功")
    void addReview_Success() {
        // Given
        when(reviewRepository.existsByAccommodationIdAndUsername(1L, "testuser")).thenReturn(false);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodation));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(reviewRepository.findByAccommodationId(1L)).thenReturn(Arrays.asList(testReview));

        // When
        ReviewDTO result = reviewService.addReview(1L, "testuser", new BigDecimal("4.5"), "很棒的住宿體驗");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(new BigDecimal("4.5"), result.getRating());
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(accommodationRepository, times(1)).save(any(Accommodation.class)); // updateAccommodationRating 呼叫
    }

    @Test
    @DisplayName("新增評論 - 已經評論過")
    void addReview_AlreadyReviewed() {
        // Given
        when(reviewRepository.existsByAccommodationIdAndUsername(1L, "testuser")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.addReview(1L, "testuser", new BigDecimal("4.5"), "很棒");
        });

        assertTrue(exception.getMessage().contains("已經評論過此住宿"));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("新增評論 - 評分小於1")
    void addReview_RatingTooLow() {
        // Given
        when(reviewRepository.existsByAccommodationIdAndUsername(1L, "testuser")).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.addReview(1L, "testuser", new BigDecimal("0.5"), "測試");
        });

        assertTrue(exception.getMessage().contains("評分必須在 1-5 之間"));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("新增評論 - 評分大於5")
    void addReview_RatingTooHigh() {
        // Given
        when(reviewRepository.existsByAccommodationIdAndUsername(1L, "testuser")).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.addReview(1L, "testuser", new BigDecimal("5.5"), "測試");
        });

        assertTrue(exception.getMessage().contains("評分必須在 1-5 之間"));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("新增評論 - 住宿不存在")
    void addReview_AccommodationNotFound() {
        // Given
        when(reviewRepository.existsByAccommodationIdAndUsername(999L, "testuser")).thenReturn(false);
        when(accommodationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.addReview(999L, "testuser", new BigDecimal("4.5"), "測試");
        });

        assertTrue(exception.getMessage().contains("找不到住宿"));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("新增評論 - 用戶不存在")
    void addReview_UserNotFound() {
        // Given
        when(reviewRepository.existsByAccommodationIdAndUsername(1L, "nonexistent")).thenReturn(false);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodation));
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.addReview(1L, "nonexistent", new BigDecimal("4.5"), "測試");
        });

        assertTrue(exception.getMessage().contains("找不到用戶"));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("更新住宿評分 - 成功")
    void updateAccommodationRating_Success() {
        // Given
        Review review1 = new Review();
        review1.setRating(new BigDecimal("4.0"));

        Review review2 = new Review();
        review2.setRating(new BigDecimal("5.0"));

        List<Review> reviews = Arrays.asList(review1, review2);
        when(reviewRepository.findByAccommodationId(1L)).thenReturn(reviews);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodation));
        when(accommodationRepository.save(any(Accommodation.class))).thenReturn(testAccommodation);

        // When
        reviewService.updateAccommodationRating(1L);

        // Then
        verify(accommodationRepository, times(1)).save(any(Accommodation.class));
        assertEquals(new BigDecimal("4.50"), testAccommodation.getRating());
        assertEquals(2, testAccommodation.getReviewCount());
    }

    @Test
    @DisplayName("更新住宿評分 - 無評論時不更新")
    void updateAccommodationRating_NoReviews() {
        // Given
        when(reviewRepository.findByAccommodationId(1L)).thenReturn(Collections.emptyList());

        // When
        reviewService.updateAccommodationRating(1L);

        // Then
        verify(accommodationRepository, never()).save(any(Accommodation.class));
    }

    @Test
    @DisplayName("更新住宿評分 - 住宿不存在")
    void updateAccommodationRating_AccommodationNotFound() {
        // Given
        Review review1 = new Review();
        review1.setRating(new BigDecimal("4.0"));
        when(reviewRepository.findByAccommodationId(999L)).thenReturn(Arrays.asList(review1));
        when(accommodationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            reviewService.updateAccommodationRating(999L);
        });

        verify(accommodationRepository, never()).save(any(Accommodation.class));
    }

    @Test
    @DisplayName("評分計算 - 多個評論平均值")
    void updateAccommodationRating_MultipleReviews() {
        // Given
        Review review1 = new Review();
        review1.setRating(new BigDecimal("3.0"));

        Review review2 = new Review();
        review2.setRating(new BigDecimal("4.0"));

        Review review3 = new Review();
        review3.setRating(new BigDecimal("5.0"));

        List<Review> reviews = Arrays.asList(review1, review2, review3);
        when(reviewRepository.findByAccommodationId(1L)).thenReturn(reviews);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodation));
        when(accommodationRepository.save(any(Accommodation.class))).thenReturn(testAccommodation);

        // When
        reviewService.updateAccommodationRating(1L);

        // Then
        assertEquals(new BigDecimal("4.00"), testAccommodation.getRating());
        assertEquals(3, testAccommodation.getReviewCount());
    }
}

