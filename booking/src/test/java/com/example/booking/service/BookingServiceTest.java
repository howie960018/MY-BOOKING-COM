package com.example.booking.service;

import com.example.booking.model.*;
import com.example.booking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AccommodationRepository accommodationRepo;

    @Autowired
    private RoomTypeRepository roomTypeRepo;

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User owner;
    private Accommodation testAccommodation;
    private RoomType testRoomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    @BeforeEach
    void setUp() {
        // 清空所有資料
        bookingRepo.deleteAll();
        roomTypeRepo.deleteAll();
        accommodationRepo.deleteAll();
        userRepo.deleteAll();

        // 設定測試日期
        checkInDate = LocalDate.now().plusDays(1);
        checkOutDate = LocalDate.now().plusDays(4); // 3晚

        // 建立測試用戶
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setRole("ROLE_USER");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setPhone("0900-000-000");
        testUser = userRepo.save(testUser);

        // 建立房東
        owner = new User();
        owner.setUsername("owner");
        owner.setPassword(passwordEncoder.encode("password"));
        owner.setRole("ROLE_OWNER");
        owner.setEmail("owner@example.com");
        owner.setFullName("Owner");
        owner.setPhone("0911-111-111");
        owner = userRepo.save(owner);

        // 建立住宿
        testAccommodation = new Accommodation();
        testAccommodation.setName("測試住宿");
        testAccommodation.setDescription("測試描述");
        testAccommodation.setLocation("台北");
        testAccommodation.setOwner(owner);
        testAccommodation = accommodationRepo.save(testAccommodation);

        // 建立房型
        testRoomType = new RoomType();
        testRoomType.setName("測試房型");
        testRoomType.setDescription("測試房型描述");
        testRoomType.setPricePerNight(BigDecimal.valueOf(2000));
        testRoomType.setTotalRooms(5);
        testRoomType.setAccommodation(testAccommodation);
        testRoomType = roomTypeRepo.save(testRoomType);

        // 設定 Security Context
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        testUser.getUsername(),
                        testUser.getPassword(),
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // ==================== 正常流程測試 ====================

    @Test
    @DisplayName("正常建立訂單 - 應成功建立並返回訂單資訊")
    void testBookByRoomType_Success() {
        // Given
        int quantity = 2;

        // When
        Booking result = bookingService.bookByRoomType(
                testRoomType.getId(),
                checkInDate,
                checkOutDate,
                quantity
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getBookedQuantity()).isEqualTo(quantity);
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(12000));
    }

    @Test
    @DisplayName("單間預訂 - 應正確計算總價")
    void testBookByRoomType_SingleRoom_PriceCalculation() {
        // Given
        int quantity = 1;

        // When
        Booking result = bookingService.bookByRoomType(
                testRoomType.getId(),
                checkInDate,
                checkOutDate,
                quantity
        );

        // Then
        assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(6000));
        assertThat(result.getBookedQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("計算總價 - 多晚")
    void testPriceCalculation_MultipleNights() {
        // Given - 7晚
        LocalDate sevenNightsLater = checkInDate.plusDays(7);

        // When
        Booking result = bookingService.bookByRoomType(
                testRoomType.getId(),
                checkInDate,
                sevenNightsLater,
                1
        );

        // Then - 7晚 * 2000 = 14000
        assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(14000));
    }

    @Test
    @DisplayName("計算總價 - 多間多晚")
    void testPriceCalculation_MultipleRoomsMultipleNights() {
        // Given - 5晚，3間
        LocalDate fiveNightsLater = checkInDate.plusDays(5);

        // When
        Booking result = bookingService.bookByRoomType(
                testRoomType.getId(),
                checkInDate,
                fiveNightsLater,
                3
        );

        // Then - 5晚 * 2000 * 3間 = 30000
        assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(30000));
    }

    @Test
    @DisplayName("計算總價 - 單晚單間")
    void testPriceCalculation_SingleNightSingleRoom() {
        // Given - 1晚
        LocalDate oneNightLater = checkInDate.plusDays(1);

        // When
        Booking result = bookingService.bookByRoomType(
                testRoomType.getId(),
                checkInDate,
                oneNightLater,
                1
        );

        // Then - 1晚 * 2000 = 2000
        assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(2000));
    }

    // ==================== 庫存檢查測試 ====================

    @Test
    @DisplayName("庫存不足 - 應拋出異常")
    void testBookByRoomType_InsufficientInventory_ThrowsException() {
        // Given - 先預訂3間
        bookingService.bookByRoomType(testRoomType.getId(), checkInDate, checkOutDate, 3);

        // When & Then - 再預訂3間（總共超過5間）
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(testRoomType.getId(), checkInDate, checkOutDate, 3))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("庫存不足");
    }

    @Test
    @DisplayName("剛好滿庫存 - 應成功建立訂單")
    void testBookByRoomType_ExactInventory_Success() {
        // Given - 先預訂3間
        bookingService.bookByRoomType(testRoomType.getId(), checkInDate, checkOutDate, 3);

        // When - 再預訂2間（剛好5間）
        Booking result = bookingService.bookByRoomType(
                testRoomType.getId(),
                checkInDate,
                checkOutDate,
                2
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBookedQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("庫存已滿 - 應拋出異常")
    void testBookByRoomType_FullInventory_ThrowsException() {
        // Given - 先預訂滿5間
        bookingService.bookByRoomType(testRoomType.getId(), checkInDate, checkOutDate, 5);

        // When & Then - 再預訂1間
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(testRoomType.getId(), checkInDate, checkOutDate, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("庫存不足");
    }

    // ==================== 參數驗證測試 ====================

    @Test
    @DisplayName("入住日期為 null - 應拋出異常")
    void testBookByRoomType_NullCheckInDate_ThrowsException() {
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(testRoomType.getId(), null, checkOutDate, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("日期區間不合法");
    }

    @Test
    @DisplayName("退房日期為 null - 應拋出異常")
    void testBookByRoomType_NullCheckOutDate_ThrowsException() {
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(testRoomType.getId(), checkInDate, null, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("日期區間不合法");
    }

    @Test
    @DisplayName("退房日期早於入住日期 - 應拋出異常")
    void testBookByRoomType_CheckOutBeforeCheckIn_ThrowsException() {
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(testRoomType.getId(), checkOutDate, checkInDate, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("日期區間不合法");
    }

    @Test
    @DisplayName("退房日期等於入住日期 - 應拋出異常")
    void testBookByRoomType_CheckOutEqualsCheckIn_ThrowsException() {
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(testRoomType.getId(), checkInDate, checkInDate, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("日期區間不合法");
    }

    @Test
    @DisplayName("預訂數量為 0 - 應拋出異常")
    void testBookByRoomType_ZeroQuantity_ThrowsException() {
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(testRoomType.getId(), checkInDate, checkOutDate, 0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("預訂數量需大於 0");
    }

    @Test
    @DisplayName("預訂數量為負數 - 應拋出異常")
    void testBookByRoomType_NegativeQuantity_ThrowsException() {
        assertThatThrownBy(() ->
                bookingService.bookByRoomType(testRoomType.getId(), checkInDate, checkOutDate, -1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("預訂數量需大於 0");
    }

    // ==================== 資料查找測試 ====================

    @Test
    @DisplayName("房型不存在 - 應拋出異常")
    void testBookByRoomType_RoomTypeNotFound_ThrowsException() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() ->
                bookingService.bookByRoomType(nonExistentId, checkInDate, checkOutDate, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("找不到房型");
    }

    // ==================== 訂單取消測試 ====================

    @Test
    @DisplayName("用戶取消自己的訂單 - 應成功取消")
    void testCancelBooking_ByOwner_Success() {
        // Given - 先建立訂單
        Booking booking = bookingService.bookByRoomType(
                testRoomType.getId(),
                checkInDate,
                checkOutDate,
                1
        );

        // When - 用戶取消自己的訂單
        Booking result = bookingService.cancelBooking(booking.getId(), testUser.getUsername());

        // Then
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("用戶取消他人的訂單 - 應拋出異常")
    void testCancelBooking_ByOtherUser_ThrowsException() {
        // Given - 先建立訂單
        Booking booking = bookingService.bookByRoomType(
                testRoomType.getId(),
                checkInDate,
                checkOutDate,
                1
        );

        // When & Then - 嘗試用其他用戶取消
        assertThatThrownBy(() ->
                bookingService.cancelBooking(booking.getId(), "otheruser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("沒有權限取消此訂單");  // 🔧 改為正確的錯誤訊息
    }

    @Test
    @DisplayName("房東取消訂單 - 應成功取消")
    void testCancelBooking_ByOwner_AsOwner_Success() {
        // Given - 先建立訂單
        Booking booking = bookingService.bookByRoomType(
                testRoomType.getId(),
                checkInDate,
                checkOutDate,
                1
        );

        // When - 房東使用 cancelBookingByOwner 取消訂單
        Booking result = bookingService.cancelBookingByOwner(booking.getId(), owner.getUsername());

        // Then
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("取消已取消的訂單 - 應拋出異常")
    void testCancelBooking_AlreadyCancelled_ThrowsException() {
        // Given - 先建立並取消訂單
        Booking booking = bookingService.bookByRoomType(
                testRoomType.getId(),
                checkInDate,
                checkOutDate,
                1
        );
        bookingService.cancelBooking(booking.getId(), testUser.getUsername());

        // When & Then - 再次取消
        assertThatThrownBy(() ->
                bookingService.cancelBooking(booking.getId(), testUser.getUsername()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("已取消");
    }

    // ==================== Legacy API 測試 ====================

    @Test
    @DisplayName("Legacy API - 以住宿ID預訂（應使用第一個房型）")
    void testBook_LegacyAPI() {
        // When
        Booking result = bookingService.book(
                testAccommodation.getId(),
                checkInDate,
                checkOutDate
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRoomType().getId()).isEqualTo(testRoomType.getId());
        assertThat(result.getBookedQuantity()).isEqualTo(1);
    }
}