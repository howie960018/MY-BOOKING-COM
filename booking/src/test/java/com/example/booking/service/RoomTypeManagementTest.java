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
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RoomType Management 單元測試
 * 測試房型管理功能，包含：
 * - 新增房型
 * - 更新房型
 * - 刪除房型
 * - 查詢房型
 * - 所有權驗證
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("房型管理單元測試")
class RoomTypeManagementTest {

    @Mock
    private AccommodationRepository accommodationRepo;

    @Mock
    private RoomTypeRepository roomTypeRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private BookingService bookingService;

    private User ownerUser;
    private User otherOwner;
    private Accommodation testAccommodation;
    private RoomType testRoomType;

    @BeforeEach
    void setUp() {
        // 設置房東用戶
        ownerUser = new User();
        ownerUser.setId(1L);
        ownerUser.setUsername("owner");
        ownerUser.setRole("ROLE_OWNER");

        otherOwner = new User();
        otherOwner.setId(2L);
        otherOwner.setUsername("otherowner");
        otherOwner.setRole("ROLE_OWNER");

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
    }

    // ==================== 新增房型測試 ====================

    @Test
    @DisplayName("新增房型 - 房東應能為自己的住宿新增房型")
    void testCreateRoomType_Success() {
        // Given
        RoomType newRoomType = new RoomType();
        newRoomType.setName("豪華房");
        newRoomType.setDescription("豪華加大床");
        newRoomType.setPricePerNight(BigDecimal.valueOf(3000));
        newRoomType.setTotalRooms(3);

        when(accommodationRepo.findById(1L)).thenReturn(Optional.of(testAccommodation));
        when(roomTypeRepo.save(any(RoomType.class))).thenAnswer(invocation -> {
            RoomType rt = invocation.getArgument(0);
            rt.setId(2L);
            return rt;
        });

        // When
        RoomType result = bookingService.createRoomType(1L, newRoomType, "owner");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccommodation()).isEqualTo(testAccommodation);
        verify(roomTypeRepo, times(1)).save(any(RoomType.class));
    }

    @Test
    @DisplayName("新增房型 - 非房東無權新增房型")
    void testCreateRoomType_UnauthorizedOwner_ThrowsException() {
        // Given
        RoomType newRoomType = new RoomType();
        newRoomType.setName("豪華房");

        when(accommodationRepo.findById(1L)).thenReturn(Optional.of(testAccommodation));

        // When & Then
        assertThatThrownBy(() ->
                bookingService.createRoomType(1L, newRoomType, "otherowner"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("無權限操作此住宿");

        verify(roomTypeRepo, never()).save(any(RoomType.class));
    }

    @Test
    @DisplayName("新增房型 - 住宿不存在應拋出異常")
    void testCreateRoomType_AccommodationNotFound_ThrowsException() {
        // Given
        RoomType newRoomType = new RoomType();
        when(accommodationRepo.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                bookingService.createRoomType(99L, newRoomType, "owner"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("找不到住宿");
    }

    // ==================== 查詢房型測試 ====================

    @Test
    @DisplayName("查詢房型列表 - 應返回指定住宿的所有房型")
    void testGetRoomTypesForAccommodation_Success() {
        // Given
        List<RoomType> roomTypes = Arrays.asList(testRoomType);
        when(roomTypeRepo.findByAccommodationId(1L)).thenReturn(roomTypes);

        // When
        List<RoomType> result = bookingService.getRoomTypesForAccommodation(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRoomType);
        verify(roomTypeRepo, times(1)).findByAccommodationId(1L);
    }

    @Test
    @DisplayName("查詢房型列表 - 住宿無房型應返回空列表")
    void testGetRoomTypesForAccommodation_EmptyList() {
        // Given
        when(roomTypeRepo.findByAccommodationId(1L)).thenReturn(Collections.emptyList());

        // When
        List<RoomType> result = bookingService.getRoomTypesForAccommodation(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    // ==================== 所有權檢查測試 ====================

    @Test
    @DisplayName("所有權檢查 - 房東擁有的住宿應通過檢查")
    void testCheckAccommodationOwnership_ValidOwner_NoException() {
        // Given
        when(accommodationRepo.findById(1L)).thenReturn(Optional.of(testAccommodation));

        // When & Then
        assertThatNoException().isThrownBy(() ->
                bookingService.checkAccommodationOwnership(1L, "owner"));
    }

    @Test
    @DisplayName("所有權檢查 - 非房東應拋出異常")
    void testCheckAccommodationOwnership_InvalidOwner_ThrowsException() {
        // Given
        when(accommodationRepo.findById(1L)).thenReturn(Optional.of(testAccommodation));

        // When & Then
        assertThatThrownBy(() ->
                bookingService.checkAccommodationOwnership(1L, "otherowner"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("無權限操作此住宿");
    }

    @Test
    @DisplayName("所有權檢查 - 住宿不存在應拋出異常")
    void testCheckAccommodationOwnership_NotFound_ThrowsException() {
        // Given
        when(accommodationRepo.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                bookingService.checkAccommodationOwnership(99L, "owner"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("找不到住宿");
    }

    // ==================== 住宿管理測試 ====================

    @Test
    @DisplayName("新增住宿 - 房東應能建立新住宿")
    void testCreateAccommodation_Success() {
        // Given
        Accommodation newAccommodation = new Accommodation();
        newAccommodation.setName("新旅館");
        newAccommodation.setLocation("高雄市");
        newAccommodation.setDescription("全新開幕");
        newAccommodation.setPricePerNight(BigDecimal.valueOf(2500));

        when(userRepo.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
        when(accommodationRepo.save(any(Accommodation.class))).thenAnswer(invocation -> {
            Accommodation acc = invocation.getArgument(0);
            acc.setId(2L);
            return acc;
        });

        // When
        Accommodation result = bookingService.createAccommodation(newAccommodation, "owner");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOwner()).isEqualTo(ownerUser);
        verify(accommodationRepo, times(1)).save(any(Accommodation.class));
    }

    @Test
    @DisplayName("新增住宿 - 用戶不存在應拋出異常")
    void testCreateAccommodation_UserNotFound_ThrowsException() {
        // Given
        Accommodation newAccommodation = new Accommodation();
        when(userRepo.findByUsername("unknown")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                bookingService.createAccommodation(newAccommodation, "unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("找不到用戶");
    }

    @Test
    @DisplayName("更新住宿 - 房東應能更新自己的住宿")
    void testUpdateAccommodation_Success() {
        // Given
        Accommodation updatedAccommodation = new Accommodation();
        updatedAccommodation.setName("更新後的旅館名稱");
        updatedAccommodation.setLocation("新北市");
        updatedAccommodation.setDescription("更新後的描述");
        updatedAccommodation.setPricePerNight(BigDecimal.valueOf(2500));
        updatedAccommodation.setAmenities("WiFi, 停車場");

        when(accommodationRepo.findById(1L)).thenReturn(Optional.of(testAccommodation));
        when(accommodationRepo.save(any(Accommodation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Accommodation result = bookingService.updateAccommodation(1L, updatedAccommodation, "owner");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("更新後的旅館名稱");
        assertThat(result.getLocation()).isEqualTo("新北市");
        assertThat(result.getDescription()).isEqualTo("更新後的描述");
        assertThat(result.getPricePerNight()).isEqualByComparingTo(BigDecimal.valueOf(2500));
        verify(accommodationRepo, times(1)).save(testAccommodation);
    }

    @Test
    @DisplayName("更新住宿 - 非房東無權更新")
    void testUpdateAccommodation_UnauthorizedOwner_ThrowsException() {
        // Given
        Accommodation updatedAccommodation = new Accommodation();
        updatedAccommodation.setName("嘗試更新");

        when(accommodationRepo.findById(1L)).thenReturn(Optional.of(testAccommodation));

        // When & Then
        assertThatThrownBy(() ->
                bookingService.updateAccommodation(1L, updatedAccommodation, "otherowner"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("無權限修改此住宿");

        verify(accommodationRepo, never()).save(any(Accommodation.class));
    }

    @Test
    @DisplayName("更新住宿 - 住宿不存在應拋出異常")
    void testUpdateAccommodation_NotFound_ThrowsException() {
        // Given
        Accommodation updatedAccommodation = new Accommodation();
        when(accommodationRepo.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                bookingService.updateAccommodation(99L, updatedAccommodation, "owner"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("找不到住宿");
    }

    @Test
    @DisplayName("刪除住宿 - 房東應能刪除自己的住宿")
    void testDeleteAccommodation_Success() {
        // Given
        when(accommodationRepo.findById(1L)).thenReturn(Optional.of(testAccommodation));
        doNothing().when(accommodationRepo).deleteById(1L);

        // When
        assertThatNoException().isThrownBy(() ->
                bookingService.deleteAccommodation(1L, "owner"));

        // Then
        verify(accommodationRepo, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("刪除住宿 - 非房東無權刪除")
    void testDeleteAccommodation_UnauthorizedOwner_ThrowsException() {
        // Given
        when(accommodationRepo.findById(1L)).thenReturn(Optional.of(testAccommodation));

        // When & Then
        assertThatThrownBy(() ->
                bookingService.deleteAccommodation(1L, "otherowner"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("無權限刪除此住宿");

        verify(accommodationRepo, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("刪除住宿 - 住宿不存在應拋出異常")
    void testDeleteAccommodation_NotFound_ThrowsException() {
        // Given
        when(accommodationRepo.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                bookingService.deleteAccommodation(99L, "owner"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("找不到住宿");
    }

    // ==================== 房東住宿查詢測試 ====================

    @Test
    @DisplayName("取得房東住宿列表 - 應返回該房東的所有住宿")
    void testGetAccommodationsForOwner_Success() {
        // Given
        List<Accommodation> accommodations = Arrays.asList(testAccommodation);
        when(accommodationRepo.findByOwnerUsername("owner")).thenReturn(accommodations);

        // When
        List<Accommodation> result = bookingService.getAccommodationsForOwner("owner");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testAccommodation);
        verify(accommodationRepo, times(1)).findByOwnerUsername("owner");
    }

    @Test
    @DisplayName("取得房東住宿列表 - 無住宿時應返回空列表")
    void testGetAccommodationsForOwner_EmptyList() {
        // Given
        when(accommodationRepo.findByOwnerUsername("newowner")).thenReturn(Collections.emptyList());

        // When
        List<Accommodation> result = bookingService.getAccommodationsForOwner("newowner");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    // ==================== 住宿查詢測試 ====================

    @Test
    @DisplayName("取得所有住宿 - 應返回所有住宿列表")
    void testGetAllAccommodations_Success() {
        // Given
        List<Accommodation> accommodations = Arrays.asList(testAccommodation);
        when(accommodationRepo.findAll()).thenReturn(accommodations);

        // When
        List<Accommodation> result = bookingService.getAllAccommodations();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(accommodationRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("依地點搜尋住宿 - 應返回符合條件的住宿")
    void testSearchByLocation_Success() {
        // Given
        when(accommodationRepo.findByLocationContainingIgnoreCase("台北"))
                .thenReturn(Arrays.asList(testAccommodation));

        // When
        List<Accommodation> result = bookingService.searchByLocation("台北");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLocation()).contains("台北");
    }

    @Test
    @DisplayName("依地點搜尋住宿 - 空字串應返回所有住宿")
    void testSearchByLocation_EmptyString_ReturnsAll() {
        // Given
        List<Accommodation> accommodations = Arrays.asList(testAccommodation);
        when(accommodationRepo.findAll()).thenReturn(accommodations);

        // When
        List<Accommodation> result = bookingService.searchByLocation("");

        // Then
        assertThat(result).hasSize(1);
        verify(accommodationRepo, times(1)).findAll();
        verify(accommodationRepo, never()).findByLocationContainingIgnoreCase(anyString());
    }

    @Test
    @DisplayName("依地點搜尋住宿 - null 應返回所有住宿")
    void testSearchByLocation_Null_ReturnsAll() {
        // Given
        List<Accommodation> accommodations = Arrays.asList(testAccommodation);
        when(accommodationRepo.findAll()).thenReturn(accommodations);

        // When
        List<Accommodation> result = bookingService.searchByLocation(null);

        // Then
        assertThat(result).hasSize(1);
        verify(accommodationRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("依地點搜尋住宿 - 無符合條件應返回空列表")
    void testSearchByLocation_NoMatch_ReturnsEmpty() {
        // Given
        when(accommodationRepo.findByLocationContainingIgnoreCase("不存在的地點"))
                .thenReturn(Collections.emptyList());

        // When
        List<Accommodation> result = bookingService.searchByLocation("不存在的地點");

        // Then
        assertThat(result).isEmpty();
    }
}

