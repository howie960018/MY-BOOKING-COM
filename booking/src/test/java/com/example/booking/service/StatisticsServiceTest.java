package com.example.booking.service;

import com.example.booking.model.*;
import com.example.booking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * StatisticsService 單元測試
 * 測試統計分析功能，包含：
 * - 訂單狀態分布
 * - 訂單趨勢分析
 * - 熱門住宿排行
 * - 月度營收統計
 * - 房東專屬統計
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("統計服務單元測試")
class StatisticsServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    private List<Booking> testBookings;
    private User testUser;
    private User ownerUser;
    private Accommodation testAccommodation1;
    private Accommodation testAccommodation2;
    private RoomType roomType1;
    private RoomType roomType2;

    @BeforeEach
    void setUp() {
        // 設置測試用戶
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole("ROLE_USER");

        ownerUser = new User();
        ownerUser.setId(2L);
        ownerUser.setUsername("owner");
        ownerUser.setRole("ROLE_OWNER");

        // 設置測試住宿
        testAccommodation1 = new Accommodation();
        testAccommodation1.setId(1L);
        testAccommodation1.setName("測試旅館A");
        testAccommodation1.setLocation("台北市");
        testAccommodation1.setOwner(ownerUser);

        testAccommodation2 = new Accommodation();
        testAccommodation2.setId(2L);
        testAccommodation2.setName("測試旅館B");
        testAccommodation2.setLocation("台中市");
        testAccommodation2.setOwner(ownerUser);

        // 設置測試房型
        roomType1 = new RoomType();
        roomType1.setId(1L);
        roomType1.setName("標準雙人房");
        roomType1.setPricePerNight(BigDecimal.valueOf(2000));
        roomType1.setAccommodation(testAccommodation1);

        roomType2 = new RoomType();
        roomType2.setId(2L);
        roomType2.setName("豪華房");
        roomType2.setPricePerNight(BigDecimal.valueOf(3000));
        roomType2.setAccommodation(testAccommodation2);

        // 設置測試訂單
        testBookings = createTestBookings();
    }

    private List<Booking> createTestBookings() {
        List<Booking> bookings = new ArrayList<>();

        // 建立不同狀態的訂單
        LocalDate now = LocalDate.now();

        // PENDING 訂單
        Booking pending1 = createBooking(1L, now.plusDays(5), now.plusDays(8),
                                        roomType1, testUser, 1, BigDecimal.valueOf(6000), "PENDING");
        pending1.setCreatedAt(LocalDateTime.now().minusDays(1));
        bookings.add(pending1);

        // CONFIRMED 訂單
        Booking confirmed1 = createBooking(2L, now.plusDays(10), now.plusDays(13),
                                          roomType1, testUser, 2, BigDecimal.valueOf(12000), "CONFIRMED");
        confirmed1.setCreatedAt(LocalDateTime.now().minusDays(2));
        bookings.add(confirmed1);

        Booking confirmed2 = createBooking(3L, now.plusDays(15), now.plusDays(18),
                                          roomType2, testUser, 1, BigDecimal.valueOf(9000), "CONFIRMED");
        confirmed2.setCreatedAt(LocalDateTime.now().minusDays(3));
        bookings.add(confirmed2);

        // CANCELLED 訂單
        Booking cancelled1 = createBooking(4L, now.plusDays(20), now.plusDays(22),
                                          roomType1, testUser, 1, BigDecimal.valueOf(4000), "CANCELLED");
        cancelled1.setCreatedAt(LocalDateTime.now().minusDays(4));
        bookings.add(cancelled1);

        return bookings;
    }

    private Booking createBooking(Long id, LocalDate checkIn, LocalDate checkOut,
                                  RoomType roomType, User user, int quantity,
                                  BigDecimal totalPrice, String status) {
        Booking booking = new Booking(id, checkIn, checkOut, roomType, user, quantity, totalPrice);
        booking.setStatus(status);
        return booking;
    }

    // ==================== 訂單狀態分布測試 ====================

    @Test
    @DisplayName("取得訂單狀態分布 - 應正確統計各狀態數量")
    void testGetOrderStatusDistribution_Success() {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        Map<String, Long> result = statisticsService.getOrderStatusDistribution();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("PENDING")).isEqualTo(1L);
        assertThat(result.get("CONFIRMED")).isEqualTo(2L);
        assertThat(result.get("CANCELLED")).isEqualTo(1L);

        verify(bookingRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("取得訂單狀態分布 - 無訂單時應返回零值")
    void testGetOrderStatusDistribution_EmptyBookings() {
        // Given
        when(bookingRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        Map<String, Long> result = statisticsService.getOrderStatusDistribution();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("PENDING")).isEqualTo(0L);
        assertThat(result.get("CONFIRMED")).isEqualTo(0L);
        assertThat(result.get("CANCELLED")).isEqualTo(0L);
    }

    @Test
    @DisplayName("取得房東訂單狀態分布 - 應正確過濾房東訂單")
    void testGetOwnerOrderStatusDistribution_Success() {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        Map<String, Long> result = statisticsService.getOwnerOrderStatusDistribution("owner");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("PENDING")).isEqualTo(1L);
        assertThat(result.get("CONFIRMED")).isEqualTo(2L);
        assertThat(result.get("CANCELLED")).isEqualTo(1L);
    }

    @Test
    @DisplayName("取得房東訂單狀態分布 - 非房東用戶應返回零值")
    void testGetOwnerOrderStatusDistribution_NotOwner() {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        Map<String, Long> result = statisticsService.getOwnerOrderStatusDistribution("otherowner");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("PENDING")).isEqualTo(0L);
        assertThat(result.get("CONFIRMED")).isEqualTo(0L);
        assertThat(result.get("CANCELLED")).isEqualTo(0L);
    }

    // ==================== 訂單趨勢測試 ====================

    @Test
    @DisplayName("取得訂單趨勢 - 應返回指定天數的數據")
    void testGetOrdersTrend_Success() {
        // Given
        int days = 7;
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        List<Map<String, Object>> result = statisticsService.getOrdersTrend(days);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(days);

        // 驗證每個日期都有數據
        for (Map<String, Object> dayData : result) {
            assertThat(dayData).containsKeys("date", "new", "confirmed", "cancelled");
            assertThat(dayData.get("date")).isNotNull();
        }
    }

    @Test
    @DisplayName("取得訂單趨勢 - 單天應正確返回")
    void testGetOrdersTrend_SingleDay() {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        List<Map<String, Object>> result = statisticsService.getOrdersTrend(1);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsKeys("date", "new", "confirmed", "cancelled");
    }

    @Test
    @DisplayName("取得訂單趨勢 - 無訂單時應返回零值數據")
    void testGetOrdersTrend_NoBookings() {
        // Given
        when(bookingRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Map<String, Object>> result = statisticsService.getOrdersTrend(7);

        // Then
        assertThat(result).hasSize(7);
        for (Map<String, Object> dayData : result) {
            assertThat(dayData.get("new")).isEqualTo(0L);
            assertThat(dayData.get("confirmed")).isEqualTo(0L);
            assertThat(dayData.get("cancelled")).isEqualTo(0L);
        }
    }

    // ==================== 熱門住宿測試 ====================

    @Test
    @DisplayName("取得熱門住宿 - 應按訂單數量排序")
    void testGetTopAccommodations_Success() {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        List<Map<String, Object>> result = statisticsService.getTopAccommodations(10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();

        // 驗證第一名是訂單數最多的住宿（測試旅館A有3筆訂單）
        Map<String, Object> top1 = result.get(0);
        assertThat(top1.get("name")).isEqualTo("測試旅館A");
        assertThat(top1.get("count")).isEqualTo(3L);
    }

    @Test
    @DisplayName("取得熱門住宿 - 限制數量應正確生效")
    void testGetTopAccommodations_WithLimit() {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        List<Map<String, Object>> result = statisticsService.getTopAccommodations(1);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("取得熱門住宿 - 無訂單時應返回空列表")
    void testGetTopAccommodations_NoBookings() {
        // Given
        when(bookingRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Map<String, Object>> result = statisticsService.getTopAccommodations(10);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== 月度營收測試 ====================

    @Test
    @DisplayName("取得月度營收 - 應返回指定月數的數據")
    void testGetMonthlyRevenue_Success() {
        // Given
        int months = 6;
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        List<Map<String, Object>> result = statisticsService.getMonthlyRevenue(months);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(months);

        for (Map<String, Object> monthData : result) {
            assertThat(monthData).containsKeys("month", "revenue");
            assertThat(monthData.get("month")).asString().matches("\\d{4}-\\d{2}");
        }
    }

    @Test
    @DisplayName("取得月度營收 - 只計算 CONFIRMED 狀態訂單")
    void testGetMonthlyRevenue_OnlyConfirmed() {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        List<Map<String, Object>> result = statisticsService.getMonthlyRevenue(1);

        // Then
        // CONFIRMED訂單總營收應為 12000 + 9000 = 21000
        double totalRevenue = result.stream()
                .mapToDouble(m -> (Double) m.get("revenue"))
                .sum();

        assertThat(totalRevenue).isEqualTo(21000.0);
    }

    @Test
    @DisplayName("取得房東月度營收 - 應正確過濾房東訂單")
    void testGetOwnerMonthlyRevenue_Success() {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        List<Map<String, Object>> result = statisticsService.getOwnerMonthlyRevenue("owner", 6);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(6);
    }

    // ==================== 房東住宿營收測試 ====================

    @Test
    @DisplayName("取得房東住宿營收 - 應正確統計各住宿營收")
    void testGetOwnerAccommodationRevenue_Success() {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        List<Map<String, Object>> result = statisticsService.getOwnerAccommodationRevenue("owner");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();

        // 驗證數據結構
        for (Map<String, Object> item : result) {
            assertThat(item).containsKeys("name", "revenue");
            assertThat(item.get("revenue")).isInstanceOf(Double.class);
        }
    }

    @Test
    @DisplayName("取得房東住宿營收 - 只計算 CONFIRMED 狀態")
    void testGetOwnerAccommodationRevenue_OnlyConfirmed() {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        List<Map<String, Object>> result = statisticsService.getOwnerAccommodationRevenue("owner");

        // Then
        double totalRevenue = result.stream()
                .mapToDouble(m -> (Double) m.get("revenue"))
                .sum();

        // 只有 CONFIRMED 訂單：12000 + 9000 = 21000
        assertThat(totalRevenue).isEqualTo(21000.0);
    }

    @Test
    @DisplayName("取得房東住宿營收 - 應按營收排序")
    void testGetOwnerAccommodationRevenue_SortedByRevenue() {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        List<Map<String, Object>> result = statisticsService.getOwnerAccommodationRevenue("owner");

        // Then
        if (result.size() > 1) {
            for (int i = 0; i < result.size() - 1; i++) {
                double currentRevenue = (Double) result.get(i).get("revenue");
                double nextRevenue = (Double) result.get(i + 1).get("revenue");
                assertThat(currentRevenue).isGreaterThanOrEqualTo(nextRevenue);
            }
        }
    }

    // ==================== 房東房型銷售測試 ====================

    @Test
    @DisplayName("取得房東房型銷售排行 - 應正確統計")
    void testGetOwnerRoomTypeSales_Success() {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        List<Map<String, Object>> result = statisticsService.getOwnerRoomTypeSales("owner");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();

        for (Map<String, Object> item : result) {
            assertThat(item).containsKeys("name", "count");
            assertThat(item.get("count")).isInstanceOf(Long.class);
        }
    }

    @Test
    @DisplayName("取得房東房型銷售排行 - 應按銷售量排序")
    void testGetOwnerRoomTypeSales_SortedByCount() {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        List<Map<String, Object>> result = statisticsService.getOwnerRoomTypeSales("owner");

        // Then
        if (result.size() > 1) {
            for (int i = 0; i < result.size() - 1; i++) {
                long currentCount = (Long) result.get(i).get("count");
                long nextCount = (Long) result.get(i + 1).get("count");
                assertThat(currentCount).isGreaterThanOrEqualTo(nextCount);
            }
        }
    }

    @Test
    @DisplayName("取得房東房型銷售排行 - 無訂單時返回空列表")
    void testGetOwnerRoomTypeSales_NoBookings() {
        // Given
        when(bookingRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Map<String, Object>> result = statisticsService.getOwnerRoomTypeSales("owner");

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== 快取測試 ====================

    @Test
    @DisplayName("快取機制 - 第二次調用應使用快取數據")
    void testCaching_SecondCallUsesCache() {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When - 第一次調用
        Map<String, Long> result1 = statisticsService.getOrderStatusDistribution();

        // When - 第二次調用（應從快取讀取）
        Map<String, Long> result2 = statisticsService.getOrderStatusDistribution();

        // Then
        assertThat(result1).isEqualTo(result2);

        // 應該只調用一次 repository（第二次從快取讀取）
        verify(bookingRepository, times(1)).findAll();
    }
}

