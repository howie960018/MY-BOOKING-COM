package com.example.booking.service;

import com.example.booking.model.Accommodation;
import com.example.booking.model.Favorite;
import com.example.booking.model.User;
import com.example.booking.repository.AccommodationRepository;
import com.example.booking.repository.FavoriteRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * FavoriteService 單元測試
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("收藏服務測試")
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    private User testUser;
    private Accommodation testAccommodation;
    private Favorite testFavorite;

    @BeforeEach
    void setUp() {
        // 準備測試資料
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testAccommodation = new Accommodation();
        testAccommodation.setId(1L);
        testAccommodation.setName("測試住宿");
        testAccommodation.setLocation("台北");
        testAccommodation.setPricePerNight(new BigDecimal("2000"));

        testFavorite = new Favorite(testUser, testAccommodation);
        testFavorite.setId(1L);
    }

    @Test
    @DisplayName("添加收藏 - 成功")
    void addFavorite_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodation));
        when(favoriteRepository.existsByUserUsernameAndAccommodationId("testuser", 1L)).thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(testFavorite);

        // When
        Favorite result = favoriteService.addFavorite("testuser", 1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    @DisplayName("添加收藏 - 用戶不存在")
    void addFavorite_UserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            favoriteService.addFavorite("nonexistent", 1L);
        });

        assertTrue(exception.getMessage().contains("找不到用戶"));
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    @DisplayName("添加收藏 - 住宿不存在")
    void addFavorite_AccommodationNotFound() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(accommodationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            favoriteService.addFavorite("testuser", 999L);
        });

        assertTrue(exception.getMessage().contains("找不到住宿"));
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    @DisplayName("添加收藏 - 已經收藏過")
    void addFavorite_AlreadyFavorited() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodation));
        when(favoriteRepository.existsByUserUsernameAndAccommodationId("testuser", 1L)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            favoriteService.addFavorite("testuser", 1L);
        });

        assertTrue(exception.getMessage().contains("已經收藏過此住宿"));
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    @DisplayName("取消收藏 - 成功")
    void removeFavorite_Success() {
        // Given
        when(favoriteRepository.existsByUserUsernameAndAccommodationId("testuser", 1L)).thenReturn(true);
        doNothing().when(favoriteRepository).deleteByUserUsernameAndAccommodationId("testuser", 1L);

        // When
        favoriteService.removeFavorite("testuser", 1L);

        // Then
        verify(favoriteRepository, times(1)).deleteByUserUsernameAndAccommodationId("testuser", 1L);
    }

    @Test
    @DisplayName("取消收藏 - 尚未收藏")
    void removeFavorite_NotFavorited() {
        // Given
        when(favoriteRepository.existsByUserUsernameAndAccommodationId("testuser", 1L)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            favoriteService.removeFavorite("testuser", 1L);
        });

        assertTrue(exception.getMessage().contains("尚未收藏此住宿"));
        verify(favoriteRepository, never()).deleteByUserUsernameAndAccommodationId(anyString(), anyLong());
    }

    @Test
    @DisplayName("切換收藏狀態 - 添加收藏")
    void toggleFavorite_AddFavorite() {
        // Given
        when(favoriteRepository.existsByUserUsernameAndAccommodationId("testuser", 1L)).thenReturn(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodation));
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(testFavorite);

        // When
        boolean result = favoriteService.toggleFavorite("testuser", 1L);

        // Then
        assertTrue(result); // 已添加收藏
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    @DisplayName("切換收藏狀態 - 取消收藏")
    void toggleFavorite_RemoveFavorite() {
        // Given
        when(favoriteRepository.existsByUserUsernameAndAccommodationId("testuser", 1L))
                .thenReturn(true) // 第一次檢查：已收藏
                .thenReturn(true); // toggleFavorite 內的檢查
        doNothing().when(favoriteRepository).deleteByUserUsernameAndAccommodationId("testuser", 1L);

        // When
        boolean result = favoriteService.toggleFavorite("testuser", 1L);

        // Then
        assertFalse(result); // 已取消收藏
        verify(favoriteRepository, times(1)).deleteByUserUsernameAndAccommodationId("testuser", 1L);
    }

    @Test
    @DisplayName("取得用戶的所有收藏")
    void getFavorites_Success() {
        // Given
        List<Favorite> favorites = Arrays.asList(testFavorite);
        when(favoriteRepository.findByUserUsername("testuser")).thenReturn(favorites);

        // When
        List<Accommodation> result = favoriteService.getFavorites("testuser");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("測試住宿", result.get(0).getName());
        verify(favoriteRepository, times(1)).findByUserUsername("testuser");
    }

    @Test
    @DisplayName("檢查是否已收藏 - 是")
    void isFavorited_True() {
        // Given
        when(favoriteRepository.existsByUserUsernameAndAccommodationId("testuser", 1L)).thenReturn(true);

        // When
        boolean result = favoriteService.isFavorited("testuser", 1L);

        // Then
        assertTrue(result);
        verify(favoriteRepository, times(1)).existsByUserUsernameAndAccommodationId("testuser", 1L);
    }

    @Test
    @DisplayName("檢查是否已收藏 - 否")
    void isFavorited_False() {
        // Given
        when(favoriteRepository.existsByUserUsernameAndAccommodationId("testuser", 1L)).thenReturn(false);

        // When
        boolean result = favoriteService.isFavorited("testuser", 1L);

        // Then
        assertFalse(result);
        verify(favoriteRepository, times(1)).existsByUserUsernameAndAccommodationId("testuser", 1L);
    }
}

