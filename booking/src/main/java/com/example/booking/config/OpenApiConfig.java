package com.example.booking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🏨 Hotel Booking Management System API")
                        .version("v2.0.0")
                        .description("""
                                # 完整的訂房系統 RESTful API 文件
                                
                                **最後更新**: 2025-11-09  
                                **API 版本**: 2.0.0  
                                **Spring Boot**: 3.2.5
                                
                                ---

                                ## 🎯 系統概述
                                
                                這是一個功能完整的訂房管理系統，**採用開放瀏覽設計**（類似 Booking.com）。
                                
                                ### 核心特色
                                - 🌐 **開放瀏覽**：訪客無需登入即可查看住宿、搜尋、查看詳情
                                - 🔐 **安全預訂**：預訂、收藏等功能需要登入
                                - 👥 **多角色系統**：管理員、房東、一般用戶
                                - 📊 **完整功能**：住宿管理、訂單處理、統計分析
                                
                                ### 主要功能
                                - 🏨 住宿與房型管理（含圖片上傳）
                                - 📅 房間預訂與庫存管理
                                - ⭐ 收藏與評論系統
                                - 👤 用戶資料與密碼管理
                                - 📊 統計圖表與報表匯出
                                - 🔐 完整的權限控管系統

                                ---

                                ## 🌐 訪問控制說明
                                
                                ### 🔓 公開訪問（無需登入）
                                - ✅ 首頁瀏覽
                                - ✅ 住宿列表查詢
                                - ✅ 住宿詳情頁面
                                - ✅ 搜尋與排序功能
                                - ✅ 房型資訊查看
                                - ✅ 評論查看
                                
                                ### 🔐 需要登入
                                - 🔒 建立訂單
                                - 🔒 查看我的訂單
                                - 🔒 收藏功能
                                - 🔒 發表評論
                                - 🔒 個人資料管理
                                
                                ### 👨‍💼 管理員專用
                                - 🔒 全局訂單管理
                                - 🔒 全局住宿管理
                                - 🔒 用戶管理
                                - 🔒 系統統計
                                
                                ### 🏠 房東專用
                                - 🔒 自己的住宿管理
                                - 🔒 房型管理
                                - 🔒 訂單查看
                                - 🔒 營運統計

                                ---

                                ## 🔐 認證方式
                                
                                本 API 使用 **Session-based 認證**（Spring Security）。
                                
                                ### 使用步驟：
                                1. **登入系統**
                                   - 訪問: `/login` (網頁登入)
                                   - 或使用 API: `POST /api/auth/login`
                                
                                2. **取得 Session**
                                   - 登入成功後，系統會建立 Session Cookie (`JSESSIONID`)
                                   - Cookie 會自動附加在後續請求中
                                
                                3. **使用 API**
                                   - 在同個瀏覽器中使用 Swagger UI
                                   - 或在 API 請求中帶上 `JSESSIONID` Cookie
                                
                                4. **登出**
                                   - 訪問: `POST /logout`
                                   - 登出後返回首頁，可繼續瀏覽住宿

                                ---

                                ## 👥 測試帳號
                                
                                | 角色 | 帳號 | 密碼 | 權限說明 |
                                |------|------|------|----------|
                                | 👨‍💼 管理員 | `admin` | `password` | 完整系統管理權限，可查看所有資料 |
                                | 🏠 房東1 | `owner1` | `123456` | 管理自己的住宿、房型與訂單 |
                                | 🏠 房東2 | `owner2` | `123456` | 管理自己的住宿、房型與訂單 |
                                | 👤 用戶1 | `user1` | `123456` | 查詢、預訂、收藏、評論功能 |
                                | 👤 用戶2 | `user2` | `123456` | 查詢、預訂、收藏、評論功能 |
                                | 👤 用戶3 | `user3` | `123456` | 查詢、預訂、收藏、評論功能 |

                                ---

                                ## 📋 API 分組說明
                                
                                ### 🔓 公開 API（無需登入）
                                這些 API 訪客可以直接使用，無需註冊或登入：
                                
                                - **Accommodations**: 
                                  - `GET /api/accommodations` - 獲取所有住宿
                                  - `GET /api/accommodations/search` - 搜尋住宿
                                  - `GET /api/accommodations/{id}` - 查看住宿詳情
                                  
                                - **Room Types**: 
                                  - `GET /api/room-types/by-accommodation/{id}` - 查看房型列表
                                  - `GET /api/room-types/{id}` - 查看房型詳情
                                  - `GET /api/room-types/{id}/availability` - 檢查房型可用性
                                  
                                - **Reviews**: 
                                  - `GET /api/reviews/accommodation/{id}` - 查看住宿評論
                                  
                                - **Authentication**: 
                                  - `POST /api/auth/register` - 用戶註冊
                                  - `POST /api/auth/login` - 用戶登入
                                
                                ### 🔒 需登入 API（一般用戶）
                                這些 API 需要先登入才能使用：
                                
                                - **Bookings**: 
                                  - `POST /api/bookings/book-by-room-type` - 建立訂單
                                  - `GET /api/bookings` - 查看我的訂單
                                  - `DELETE /api/bookings/{id}` - 取消訂單
                                  
                                - **Favorites**: 
                                  - `POST /user/favorites/api/toggle/{id}` - 切換收藏狀態
                                  - `GET /user/favorites/api/check/{id}` - 檢查收藏狀態
                                  - `GET /user/favorites/api/list` - 查看我的收藏
                                  
                                - **Reviews**: 
                                  - `POST /api/reviews` - 發表評論
                                  
                                - **User Profile**: 
                                  - `GET /user/api/profile` - 查看個人資料
                                  - `PUT /user/api/profile` - 更新個人資料
                                  - `PUT /user/api/change-password` - 修改密碼
                                
                                ### 👨‍💼 管理員專用 API
                                需要 ADMIN 角色：
                                
                                - **Admin Users**: 用戶管理（查看、角色升級/降級）
                                - **Admin Accommodations**: 全局住宿管理（CRUD）
                                - **Admin Bookings**: 全局訂單管理（查看、確認、取消）
                                - **Admin Statistics**: 系統統計資料
                                - **Admin Export**: 報表匯出
                                
                                ### 🏠 房東專用 API
                                需要 OWNER 角色：
                                
                                - **Owner Accommodations**: 管理自己的住宿（CRUD）
                                - **Owner Room Types**: 管理自己的房型（CRUD、庫存）
                                - **Owner Bookings**: 查看自己住宿的訂單
                                - **Owner Statistics**: 自己的經營統計資料
                                - **Owner Export**: 自己的訂單報表匯出

                                ---

                                ## 🚀 快速開始
                                
                                ### 1️⃣ 查看住宿列表
                                ```http
                                GET /api/accommodations
                                ```
                                
                                ### 2️⃣ 搜尋住宿（按地點或名稱）
                                ```http
                                GET /api/accommodations/search?keyword=台北&sortBy=price_low
                                ```
                                
                                ### 3️⃣ 查看房型
                                ```http
                                GET /api/room-types/by-accommodation/{accommodationId}
                                ```
                                
                                ### 4️⃣ 檢查可用性
                                ```http
                                GET /api/room-types/1/availability?checkIn=2025-12-01&checkOut=2025-12-05
                                ```
                                
                                ### 5️⃣ 建立訂單（需登入）
                                ```http
                                POST /api/bookings/book-by-room-type
                                Content-Type: application/json
                                
                                {
                                  "roomTypeId": 1,
                                  "checkIn": "2025-12-01",
                                  "checkOut": "2025-12-05",
                                  "quantity": 2
                                }
                                ```
                                
                                ### 6️⃣ 查看我的訂單
                                ```http
                                GET /api/bookings
                                ```

                                ---

                                ## 📊 核心功能清單
                                
                                ### 住宿管理
                                - ✅ 住宿 CRUD（管理員、房東）
                                - ✅ 圖片 URL 上傳
                                - ✅ 搜尋與排序（地點、名稱、價格、評分）
                                - ✅ 詳細資訊查詢
                                
                                ### 房型管理
                                - ✅ 房型 CRUD（房東）
                                - ✅ 庫存管理
                                - ✅ 價格設定
                                - ✅ 可用性即時查詢
                                
                                ### 訂單管理
                                - ✅ 房型預訂（庫存檢查、衝突偵測）
                                - ✅ 訂單查詢（用戶、房東、管理員）
                                - ✅ 訂單取消（權限控管）
                                - ✅ 訂單狀態管理（PENDING、CONFIRMED、CANCELLED）
                                - ✅ Excel 報表匯出
                                
                                ### 收藏與評論
                                - ✅ 收藏管理（添加、移除、切換、查詢）
                                - ✅ 評論系統（新增、查詢）
                                - ✅ 評分機制（1-5 星）
                                - ✅ 平均評分自動計算
                                
                                ### 用戶管理
                                - ✅ 用戶註冊（含 Email 驗證）
                                - ✅ 個人資料更新
                                - ✅ 密碼修改（含舊密碼驗證）
                                - ✅ 忘記密碼（Email 令牌）
                                - ✅ 密碼重設（24小時有效）
                                
                                ### 統計分析
                                - ✅ 訂單狀態分布
                                - ✅ 訂單趨勢圖
                                - ✅ 月度營收統計
                                - ✅ 住宿營收分布
                                - ✅ 房型銷售排名
                                - ✅ 熱門住宿排行

                                ---

                                ## 🛠️ 技術架構
                                
                                ### 後端技術
                                - **框架**: Spring Boot 3.2.5
                                - **安全**: Spring Security（Session-based）
                                - **持久層**: Spring Data JPA + Hibernate
                                - **資料庫**: MySQL 8.0
                                - **郵件**: Spring Mail（SMTP）
                                - **文件**: SpringDoc OpenAPI 3.0
                                - **報表**: Apache POI（Excel）
                                
                                ### 測試技術
                                - **單元測試**: JUnit 5 + Mockito
                                - **測試數量**: 129 個（100% 通過）
                                - **覆蓋率**: 95%

                                ---

                                ## 📝 API 規範
                                
                                ### HTTP 狀態碼
                                - `200 OK` - 請求成功
                                - `201 Created` - 資源建立成功
                                - `400 Bad Request` - 請求參數錯誤
                                - `401 Unauthorized` - 未登入
                                - `403 Forbidden` - 權限不足
                                - `404 Not Found` - 資源不存在
                                - `500 Internal Server Error` - 伺服器錯誤
                                
                                ### 日期格式
                                - 統一使用 ISO 8601 格式: `YYYY-MM-DD`
                                - 範例: `2025-12-01`
                                
                                ### 價格格式
                                - 使用 `BigDecimal` 類型
                                - 保留兩位小數
                                - 範例: `2000.00`

                                ---

                                ## 🔗 相關連結
                                
                                - 📖 [API 使用指南](http://localhost:8080/api-docs)
                                - 🌐 [系統首頁](http://localhost:8080)
                                - 🔐 [登入頁面](http://localhost:8080/login)
                                - 📊 [Swagger UI](http://localhost:8080/swagger-ui.html)
                                
                                ---
                                
                                **開發團隊**: Booking System Development Team  
                                **最後更新**: 2025-11-09  
                                **技術支援**: developer@bookingsystem.com
                                """)
                        .contact(new Contact()
                                .name("Booking System Development Team")
                                .email("developer@bookingsystem.com")
                                .url("https://github.com/yourusername/booking-system"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("🖥️ 開發環境 (Local Development)"),
                        new Server()
                                .url("https://mybookingappdemo.zeabur.app")
                                .description("🌐 正式環境 (Production)")
                ))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("""
                                        **Session-based 認證（Cookie）**
                                        
                                        ## 使用方式：
                                        
                                        ### 方法 1: 透過瀏覽器登入
                                        1. 在同個瀏覽器訪問 http://localhost:8080/login
                                        2. 使用測試帳號登入（如 admin/password）
                                        3. 回到 Swagger UI，Cookie 會自動帶上
                                        4. 可以直接測試需要登入的 API
                                        
                                        ### 方法 2: 透過 API 登入
                                        1. 使用 POST /api/auth/login 登入
                                        2. 系統會設定 JSESSIONID Cookie
                                        3. 後續請求會自動攜帶此 Cookie
                                        
                                        ## 測試帳號：
                                        - 管理員: admin / password
                                        - 房東1: owner1 / 123456
                                        - 房東2: owner2 / 123456
                                        - 用戶1: user1 / 123456
                                        - 用戶2: user2 / 123456
                                        - 用戶3: user3 / 123456
                                        
                                        ## 注意事項：
                                        - Session 有效期: 30 分鐘（無操作會過期）
                                        - Cookie 屬性: HttpOnly, SameSite=Lax
                                        - 登出後 Session 會失效
                                        """)));
    }
}

