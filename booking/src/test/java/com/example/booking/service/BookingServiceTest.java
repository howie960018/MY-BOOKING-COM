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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BookingService 單元測試
 * 測試預訂住宿的核心邏輯，包含：
 * - 正常訂單建立
 * - 庫存檢查
 * - 併發訂單處理
 * - 訂單取消
 * - 異常處理
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("訂房服務單元測試")
class BookingServiceTest {

    @Mock
    private AccommodationRepository accommodationRepo;

    @Mock
    private BookingRepository bookingRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private RoomTypeRepository roomTypeRepo;

    @InjectMocks
    private BookingService bookingService;

    private User testUser;
    private User ownerUser;
    private Accommodation testAccommodation;
    private RoomType testRoomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

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
        testAccommodation.setDescription("測試用住宿");
        testAccommodation.setPricePerNight(BigDecimal.valueOf(2000));
        testAccommodation.setOwner(ownerUser);

        // 設置測試房型
        testRoomType = new RoomType();
        testRoomType.setId(1L);
        testRoomType.setName("標準雙人房");
        testRoomType.setDescription("舒適雙人房");
        testRoomType.setPricePerNight(BigDecimal.valueOf(2000));
        testRoomType.setTotalRooms(5);
        testRoomType.setAccommodation(testAccommodation);

        // 設置測試日期
        checkInDate = LocalDate.now().plusDays(7);
        checkOutDate = LocalDate.now().plusDays(10);

        // 設置 Spring Security 上下文
        setupSecurityContext("testuser");
    }

    private void setupSecurityContext(String username) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // ==================== 正常流程測試 ====================

    @Test
    @DisplayName("正常建立訂單 - 應成功建立並返回訂單資訊")
    void testBookByRoomType_Success() {
        // Given
        int quantity = 2;
        Long alreadyBooked = 0L;

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roomTypeRepo.findById(1L)).thenReturn(Optional.of(testRoomType));
        when(bookingRepo.sumBookedQuantityBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(alreadyBooked);

        Booking savedBooking = new Booking(
                1L, checkInDate, checkOutDate, testRoomType, testUser, quantity,
                BigDecimal.valueOf(12000) // 2000 * 3晚 * 2間
        );
        savedBooking.setStatus("PENDING");

        when(bookingRepo.save(any(Booking.class))).thenReturn(savedBooking);

        // When
        Booking result = bookingService.bookByRoomType(1L, checkInDate, checkOutDate, quantity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBookedQuantity()).isEqualTo(quantity);
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(12000));

        verify(userRepo, times(1)).findByUsername("testuser");
        verify(roomTypeRepo, times(1)).findById(1L);
        verify(bookingRepo, times(1)).sumBookedQuantityBetween(eq(1L), any(LocalDate.class), any(LocalDate.class));
        verify(bookingRepo, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("單間預訂 - 應正確計算總價")
    void testBookByRoomType_SingleRoom_PriceCalculation() {
        // Given
        int quantity = 1;
        Long alreadyBooked = 0L;

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roomTypeRepo.findById(1L)).thenReturn(Optional.of(testRoomType));
        when(bookingRepo.sumBookedQuantityBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(alreadyBooked);

        when(bookingRepo.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(1L);
            return booking;
        });

        // When
        Booking result = bookingService.bookByRoomType(1L, checkInDate, checkOutDate, quantity);

        // Then - 3晚 * 2000/晚 * 1間 = 6000
        assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(6000));
        assertThat(result.getBookedQuantity()).isEqualTo(1);
    }

    // ==================== 庫存檢查測試 ====================

    @Test
    @DisplayName("庫存不足 - 應拋出異常")
    void testBookByRoomType_InsufficientInventory_ThrowsException() {
        // Given
        int quantity = 3;
        Long alreadyBooked = 3L; // 已訂3間，總共5間，要訂3間會超過

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roomTypeRepo.findById(1L)).thenReturn(Optional.of(testRoomType));
        when(bookingRepo.sumBookedQuantityBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(alreadyBooked);

        // When & Then
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(1L, checkInDate, checkOutDate, quantity))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("庫存不足");

        verify(bookingRepo, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("剛好滿庫存 - 應成功建立訂單")
    void testBookByRoomType_ExactInventory_Success() {
        // Given
        int quantity = 2;
        Long alreadyBooked = 3L; // 已訂3間，要訂2間，剛好5間

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roomTypeRepo.findById(1L)).thenReturn(Optional.of(testRoomType));
        when(bookingRepo.sumBookedQuantityBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(alreadyBooked);

        when(bookingRepo.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(1L);
            return booking;
        });

        // When
        Booking result = bookingService.bookByRoomType(1L, checkInDate, checkOutDate, quantity);

        // Then
        assertThat(result).isNotNull();
        verify(bookingRepo, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("庫存已滿 - 應拋出異常")
    void testBookByRoomType_FullInventory_ThrowsException() {
        // Given
        int quantity = 1;
        Long alreadyBooked = 5L; // 已訂滿5間

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roomTypeRepo.findById(1L)).thenReturn(Optional.of(testRoomType));
        when(bookingRepo.sumBookedQuantityBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(alreadyBooked);

        // When & Then
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(1L, checkInDate, checkOutDate, quantity))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("庫存不足");
    }

    // ==================== 參數驗證測試 ====================

    @Test
    @DisplayName("入住日期為 null - 應拋出異常")
    void testBookByRoomType_NullCheckInDate_ThrowsException() {
        // When & Then
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(1L, null, checkOutDate, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("日期區間不合法");
    }

    @Test
    @DisplayName("退房日期為 null - 應拋出異常")
    void testBookByRoomType_NullCheckOutDate_ThrowsException() {
        // When & Then
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(1L, checkInDate, null, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("日期區間不合法");
    }

    @Test
    @DisplayName("退房日期早於入住日期 - 應拋出異常")
    void testBookByRoomType_CheckOutBeforeCheckIn_ThrowsException() {
        // When & Then
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(1L, checkOutDate, checkInDate, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("日期區間不合法");
    }

    @Test
    @DisplayName("退房日期等於入住日期 - 應拋出異常")
    void testBookByRoomType_CheckOutEqualsCheckIn_ThrowsException() {
        // When & Then
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(1L, checkInDate, checkInDate, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("日期區間不合法");
    }

    @Test
    @DisplayName("預訂數量為 0 - 應拋出異常")
    void testBookByRoomType_ZeroQuantity_ThrowsException() {
        // When & Then
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(1L, checkInDate, checkOutDate, 0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("預訂數量需大於 0");
    }

    @Test
    @DisplayName("預訂數量為負數 - 應拋出異常")
    void testBookByRoomType_NegativeQuantity_ThrowsException() {
        // When & Then
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(1L, checkInDate, checkOutDate, -1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("預訂數量需大於 0");
    }

    // ==================== 資料查找測試 ====================

    @Test
    @DisplayName("用戶不存在 - 應拋出異常")
    void testBookByRoomType_UserNotFound_ThrowsException() {
        // Given
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(1L, checkInDate, checkOutDate, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("找不到用戶");
    }

    @Test
    @DisplayName("房型不存在 - 應拋出異常")
    void testBookByRoomType_RoomTypeNotFound_ThrowsException() {
        // Given
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roomTypeRepo.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(99L, checkInDate, checkOutDate, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("找不到房型");
    }

    // ==================== 訂單取消測試 ====================

    @Test
    @DisplayName("用戶取消自己的訂單 - 應成功取消")
    void testCancelBooking_ByOwner_Success() {
        // Given
        Booking booking = new Booking(
                1L, checkInDate, checkOutDate, testRoomType, testUser, 1, BigDecimal.valueOf(6000)
        );
        booking.setStatus("PENDING");

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepo.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Booking result = bookingService.cancelBooking(1L, "testuser");

        // Then
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        verify(bookingRepo, times(1)).save(booking);
    }

    @Test
    @DisplayName("用戶取消他人的訂單 - 應拋出異常")
    void testCancelBooking_ByOtherUser_ThrowsException() {
        // Given
        Booking booking = new Booking(
                1L, checkInDate, checkOutDate, testRoomType, testUser, 1, BigDecimal.valueOf(6000)
        );
        booking.setStatus("PENDING");

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(booking));

        // When & Then
        assertThatThrownBy(() ->
                bookingService.cancelBooking(1L, "otheruser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("沒有權限取消此訂單");

        verify(bookingRepo, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("取消已取消的訂單 - 應拋出異常")
    void testCancelBooking_AlreadyCancelled_ThrowsException() {
        // Given
        Booking booking = new Booking(
                1L, checkInDate, checkOutDate, testRoomType, testUser, 1, BigDecimal.valueOf(6000)
        );
        booking.setStatus("CANCELLED");

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(booking));

        // When & Then
        assertThatThrownBy(() ->
                bookingService.cancelBooking(1L, "testuser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("訂單已取消");

        verify(bookingRepo, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("取消已開始的住宿 - 應拋出異常")
    void testCancelBooking_AlreadyStarted_ThrowsException() {
        // Given - 入住日期在過去
        LocalDate pastCheckIn = LocalDate.now().minusDays(1);
        LocalDate futureCheckOut = LocalDate.now().plusDays(2);

        Booking booking = new Booking(
                1L, pastCheckIn, futureCheckOut, testRoomType, testUser, 1, BigDecimal.valueOf(6000)
        );
        booking.setStatus("CONFIRMED");

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(booking));

        // When & Then
        assertThatThrownBy(() ->
                bookingService.cancelBooking(1L, "testuser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("已開始入住");

        verify(bookingRepo, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("管理員取消任意訂單 - 應成功取消")
    void testCancelBookingByAdmin_Success() {
        // Given
        Booking booking = new Booking(
                1L, checkInDate, checkOutDate, testRoomType, testUser, 1, BigDecimal.valueOf(6000)
        );
        booking.setStatus("CONFIRMED");

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepo.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Booking result = bookingService.cancelBookingByAdmin(1L);

        // Then
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        verify(bookingRepo, times(1)).save(booking);
    }

    @Test
    @DisplayName("管理員取消不存在的訂單 - 應拋出異常")
    void testCancelBookingByAdmin_NotFound_ThrowsException() {
        // Given
        when(bookingRepo.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                bookingService.cancelBookingByAdmin(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("找不到訂單");
    }

    // ==================== 舊版 API 相容性測試 ====================

    @Test
    @DisplayName("使用住宿ID訂房（舊版API） - 應委派給房型訂房")
    void testBook_LegacyAPI_DelegatesToBookByRoomType() {
        // Given
        when(roomTypeRepo.findByAccommodationId(1L))
                .thenReturn(List.of(testRoomType));
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roomTypeRepo.findById(1L)).thenReturn(Optional.of(testRoomType));
        when(bookingRepo.sumBookedQuantityBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(0L);

        when(bookingRepo.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(1L);
            return booking;
        });

        // When
        Booking result = bookingService.book(1L, checkInDate, checkOutDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBookedQuantity()).isEqualTo(1); // 預設數量為1
        verify(roomTypeRepo, times(1)).findByAccommodationId(1L);
    }

    @Test
    @DisplayName("使用住宿ID訂房但無房型 - 應拋出異常")
    void testBook_NoRoomTypes_ThrowsException() {
        // Given
        when(roomTypeRepo.findByAccommodationId(1L))
                .thenReturn(List.of());

        // When & Then
        assertThatThrownBy(() ->
                bookingService.book(1L, checkInDate, checkOutDate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("此住宿尚無可訂房型");
    }

    // ==================== 總價計算測試 ====================

    @Test
    @DisplayName("計算總價 - 單間多晚")
    void testPriceCalculation_SingleRoomMultipleNights() {
        // Given - 7晚，單價2000
        LocalDate sevenNightsLater = checkInDate.plusDays(7);
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roomTypeRepo.findById(1L)).thenReturn(Optional.of(testRoomType));
        when(bookingRepo.sumBookedQuantityBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(0L);

        when(bookingRepo.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(1L);
            return booking;
        });

        // When
        Booking result = bookingService.bookByRoomType(1L, checkInDate, sevenNightsLater, 1);

        // Then - 7晚 * 2000 = 14000
        assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(14000));
    }

    @Test
    @DisplayName("計算總價 - 多間多晚")
    void testPriceCalculation_MultipleRoomsMultipleNights() {
        // Given - 5晚，3間，單價2000
        LocalDate fiveNightsLater = checkInDate.plusDays(5);
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roomTypeRepo.findById(1L)).thenReturn(Optional.of(testRoomType));
        when(bookingRepo.sumBookedQuantityBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(0L);

        when(bookingRepo.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(1L);
            return booking;
        });

        // When
        Booking result = bookingService.bookByRoomType(1L, checkInDate, fiveNightsLater, 3);

        // Then - 5晚 * 2000 * 3間 = 30000
        assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(30000));
    }

    @Test
    @DisplayName("計算總價 - 單晚單間")
    void testPriceCalculation_SingleNightSingleRoom() {
        // Given - 1晚，1間，單價2000
        LocalDate oneNightLater = checkInDate.plusDays(1);
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roomTypeRepo.findById(1L)).thenReturn(Optional.of(testRoomType));
        when(bookingRepo.sumBookedQuantityBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(0L);

        when(bookingRepo.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(1L);
            return booking;
        });

        // When
        Booking result = bookingService.bookByRoomType(1L, checkInDate, oneNightLater, 1);

        // Then - 1晚 * 2000 = 2000
        assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(2000));
    }
}

