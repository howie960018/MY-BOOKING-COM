package com.example.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 應用程式基本測試
 * 注意：此測試不啟動 Spring 上下文，僅驗證基本類別載入
 * 完整的業務邏輯測試請參考：
 * - BookingServiceTest (24 tests)
 * - StatisticsServiceTest (20 tests)
 * - ExportServiceTest (21 tests)
 * - RoomTypeManagementTest (23 tests)
 */
@DisplayName("應用程式基本測試")
class BookingApplicationTests {

    @Test
    @DisplayName("應用程式主類別應能正常載入")
    void applicationMainClassShouldLoad() {
        // Given & When
        Class<?> mainClass = BookingApplication.class;

        // Then
        assertThat(mainClass).isNotNull();
        assertThat(mainClass.getName()).isEqualTo("com.example.booking.BookingApplication");
    }

    @Test
    @DisplayName("應用程式主類別應有 main 方法")
    void applicationShouldHaveMainMethod() throws NoSuchMethodException {
        // Given
        Class<?> mainClass = BookingApplication.class;

        // When
        var mainMethod = mainClass.getMethod("main", String[].class);

        // Then
        assertThat(mainMethod).isNotNull();
        assertThat(mainMethod.getReturnType()).isEqualTo(void.class);
    }
}
