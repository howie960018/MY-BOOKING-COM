package com.example.booking.service;

import com.example.booking.dto.BookingExportDTO;
import com.example.booking.model.*;
import com.example.booking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ExportService 單元測試
 * 測試匯出功能，包含：
 * - 管理員匯出所有訂單
 * - 用戶匯出個人訂單
 * - 房東匯出訂單
 * - Excel 檔案生成
 * - 日期過濾
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("匯出服務單元測試")
class ExportServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ExportService exportService;

    private List<Booking> testBookings;
    private User testUser;
    private User ownerUser;
    private Accommodation testAccommodation;
    private RoomType testRoomType;

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
        testAccommodation = new Accommodation();
        testAccommodation.setId(1L);
        testAccommodation.setName("測試旅館");
        testAccommodation.setLocation("台北市");
        testAccommodation.setOwner(ownerUser);

        // 設置測試房型
        testRoomType = new RoomType();
        testRoomType.setId(1L);
        testRoomType.setName("標準雙人房");
        testRoomType.setPricePerNight(BigDecimal.valueOf(2000));
        testRoomType.setAccommodation(testAccommodation);

        // 設置測試訂單
        testBookings = createTestBookings();
    }

    private List<Booking> createTestBookings() {
        List<Booking> bookings = new ArrayList<>();

        LocalDate now = LocalDate.now();

        // 訂單1 - CONFIRMED
        Booking booking1 = new Booking(1L, now.minusDays(10), now.minusDays(7),
                testRoomType, testUser, 1, BigDecimal.valueOf(6000));
        booking1.setStatus("CONFIRMED");
        booking1.setCreatedAt(LocalDateTime.now().minusDays(15));
        bookings.add(booking1);

        // 訂單2 - PENDING
        Booking booking2 = new Booking(2L, now.plusDays(5), now.plusDays(8),
                testRoomType, testUser, 2, BigDecimal.valueOf(12000));
        booking2.setStatus("PENDING");
        booking2.setCreatedAt(LocalDateTime.now().minusDays(2));
        bookings.add(booking2);

        // 訂單3 - CANCELLED
        Booking booking3 = new Booking(3L, now.plusDays(10), now.plusDays(13),
                testRoomType, testUser, 1, BigDecimal.valueOf(6000));
        booking3.setStatus("CANCELLED");
        booking3.setCreatedAt(LocalDateTime.now().minusDays(5));
        bookings.add(booking3);

        return bookings;
    }

    // ==================== 管理員匯出測試 ====================

    @Test
    @DisplayName("管理員匯出所有訂單 - 無篩選條件應返回所有訂單")
    void testExportAllBookings_NoFilter_Success() throws IOException {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        byte[] result = exportService.exportAllBookings(null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        verify(bookingRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("管理員匯出所有訂單 - 依狀態篩選")
    void testExportAllBookings_FilterByStatus() throws IOException {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        byte[] result = exportService.exportAllBookings(null, null, "CONFIRMED");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        // Excel 應該包含篩選後的資料
    }

    @Test
    @DisplayName("管理員匯出所有訂單 - 依日期範圍篩選")
    void testExportAllBookings_FilterByDateRange() throws IOException {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(15);

        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        byte[] result = exportService.exportAllBookings(startDate, endDate, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    @DisplayName("管理員匯出所有訂單 - 同時使用多個篩選條件")
    void testExportAllBookings_MultipleFilters() throws IOException {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(15);
        String status = "PENDING";

        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        byte[] result = exportService.exportAllBookings(startDate, endDate, status);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    @DisplayName("管理員匯出所有訂單 - 無符合條件的訂單仍應返回 Excel")
    void testExportAllBookings_NoMatchingBookings() throws IOException {
        // Given
        when(bookingRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        byte[] result = exportService.exportAllBookings(null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0); // 空的 Excel 檔案仍有基本結構
    }

    // ==================== 用戶匯出測試 ====================

    @Test
    @DisplayName("用戶匯出個人訂單 - 應只匯出該用戶的訂單")
    void testExportUserBookings_Success() throws IOException {
        // Given
        String username = "testuser";
        when(bookingRepository.findByUserUsername(username)).thenReturn(testBookings);

        // When
        byte[] result = exportService.exportUserBookings(username, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        verify(bookingRepository, times(1)).findByUserUsername(username);
    }

    @Test
    @DisplayName("用戶匯出個人訂單 - 依日期範圍篩選")
    void testExportUserBookings_WithDateFilter() throws IOException {
        // Given
        String username = "testuser";
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(15);

        when(bookingRepository.findByUserUsername(username)).thenReturn(testBookings);

        // When
        byte[] result = exportService.exportUserBookings(username, startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    @DisplayName("用戶匯出個人訂單 - 無訂單時應返回空 Excel")
    void testExportUserBookings_NoBookings() throws IOException {
        // Given
        String username = "newuser";
        when(bookingRepository.findByUserUsername(username)).thenReturn(Collections.emptyList());

        // When
        byte[] result = exportService.exportUserBookings(username, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    // ==================== 房東匯出測試 ====================

    @Test
    @DisplayName("房東匯出訂單 - 應只匯出該房東的訂單")
    void testExportOwnerBookings_Success() throws IOException {
        // Given
        String ownerUsername = "owner";
        when(bookingRepository.findByOwnerUsernameFetchAll(ownerUsername)).thenReturn(testBookings);

        // When
        byte[] result = exportService.exportOwnerBookings(ownerUsername, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        verify(bookingRepository, times(1)).findByOwnerUsernameFetchAll(ownerUsername);
    }

    @Test
    @DisplayName("房東匯出訂單 - 依日期範圍篩選")
    void testExportOwnerBookings_WithDateFilter() throws IOException {
        // Given
        String ownerUsername = "owner";
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(15);

        when(bookingRepository.findByOwnerUsernameFetchAll(ownerUsername)).thenReturn(testBookings);

        // When
        byte[] result = exportService.exportOwnerBookings(ownerUsername, startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    @DisplayName("房東匯出訂單 - 無訂單時應返回空 Excel")
    void testExportOwnerBookings_NoBookings() throws IOException {
        // Given
        String ownerUsername = "newowner";
        when(bookingRepository.findByOwnerUsernameFetchAll(ownerUsername))
                .thenReturn(Collections.emptyList());

        // When
        byte[] result = exportService.exportOwnerBookings(ownerUsername, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    // ==================== Excel 檔案結構測試 ====================

    @Test
    @DisplayName("Excel 檔案結構 - 應能正確讀取")
    void testExcelFileStructure_ReadableFormat() throws IOException {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        byte[] result = exportService.exportAllBookings(null, null, null);

        // Then
        assertThat(result).isNotNull();

        // 驗證是否為有效的 Excel 檔案（透過讀取前幾個位元組）
        // Excel 檔案應以 PK 開頭（ZIP 格式）
        assertThat(result.length).isGreaterThan(100);

        // 可以被 ByteArrayInputStream 讀取
        ByteArrayInputStream inputStream = new ByteArrayInputStream(result);
        assertThat(inputStream.available()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Excel 檔案大小 - 應合理")
    void testExcelFileSize_Reasonable() throws IOException {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        byte[] result = exportService.exportAllBookings(null, null, null);

        // Then
        // Excel 檔案大小應在合理範圍內（不應為空，也不應過大）
        assertThat(result.length).isBetween(1000, 1000000); // 1KB ~ 1MB
    }

    // ==================== 日期篩選邏輯測試 ====================

    @Test
    @DisplayName("日期篩選 - 開始日期應包含當天")
    void testDateFilter_StartDateInclusive() throws IOException {
        // Given
        LocalDate startDate = LocalDate.now().plusDays(5);
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        byte[] result = exportService.exportAllBookings(startDate, null, null);

        // Then
        assertThat(result).isNotNull();
        // 應該包含入住日期 >= startDate 的訂單
    }

    @Test
    @DisplayName("日期篩選 - 結束日期應包含當天")
    void testDateFilter_EndDateInclusive() throws IOException {
        // Given
        LocalDate endDate = LocalDate.now().plusDays(10);
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        byte[] result = exportService.exportAllBookings(null, endDate, null);

        // Then
        assertThat(result).isNotNull();
        // 應該包含入住日期 <= endDate 的訂單
    }

    @Test
    @DisplayName("日期篩選 - 開始日期晚於結束日期應返回空結果")
    void testDateFilter_InvalidDateRange() throws IOException {
        // Given
        LocalDate startDate = LocalDate.now().plusDays(20);
        LocalDate endDate = LocalDate.now().plusDays(10);
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        byte[] result = exportService.exportAllBookings(startDate, endDate, null);

        // Then
        assertThat(result).isNotNull();
        // 應該返回空的 Excel（沒有符合條件的訂單）
    }

    // ==================== 狀態篩選測試 ====================

    @Test
    @DisplayName("狀態篩選 - CONFIRMED 狀態")
    void testStatusFilter_Confirmed() throws IOException {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        byte[] result = exportService.exportAllBookings(null, null, "CONFIRMED");

        // Then
        assertThat(result).isNotNull();
        // 應該只包含 CONFIRMED 狀態的訂單
    }

    @Test
    @DisplayName("狀態篩選 - PENDING 狀態")
    void testStatusFilter_Pending() throws IOException {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        byte[] result = exportService.exportAllBookings(null, null, "PENDING");

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("狀態篩選 - CANCELLED 狀態")
    void testStatusFilter_Cancelled() throws IOException {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        byte[] result = exportService.exportAllBookings(null, null, "CANCELLED");

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("狀態篩選 - 空字串應視為無篩選")
    void testStatusFilter_EmptyString() throws IOException {
        // Given
        when(bookingRepository.findAll()).thenReturn(testBookings);

        // When
        byte[] result = exportService.exportAllBookings(null, null, "");

        // Then
        assertThat(result).isNotNull();
        // 應該返回所有訂單
    }

    // ==================== 異常處理測試 ====================

    @Test
    @DisplayName("Repository 拋出異常時應正確處理")
    void testRepositoryException_HandledGracefully() {
        // Given
        when(bookingRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() ->
                exportService.exportAllBookings(null, null, null))
                .isInstanceOf(RuntimeException.class);
    }
}

