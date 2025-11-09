# 🏨 訂房系統 (Booking System)

一個功能完整的線上訂房管理系統，提供住宿搜尋、預訂、管理等功能。支援多角色使用者（管理員、業主、一般用戶），具備完整的前後端整合與資料庫管理。

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue)
![H2 Database](https://img.shields.io/badge/Database-H2-blue)

---

## 📋 目錄

- [功能特色](#-功能特色)
- [技術架構](#-技術架構)
- [系統需求](#-系統需求)
- [快速開始](#-快速開始)
- [專案結構](#-專案結構)
- [API 文件](#-api-文件)
- [使用者角色](#-使用者角色)
- [功能說明](#-功能說明)
- [資料庫設計](#-資料庫設計)
- [開發指南](#-開發指南)
- [故障排除](#-故障排除)
- [未來規劃](#-未來規劃)

---

## ✨ 功能特色

### 🔍 搜尋與篩選
- **多條件搜尋**：支援地點、名稱、日期、人數等多重搜尋條件
- **智慧排序**：價格（低→高/高→低）、評分、熱門度、距離等多種排序方式
- **即時可用性檢查**：依據日期查詢可預訂的住宿
- **模糊搜尋**：支援關鍵字模糊匹配

### 🏠 住宿管理
- **詳細資訊展示**：住宿描述、圖片、價格、地點、評分等
- **房型管理**：支援多種房型（標準雙人房、豪華套房等）
- **庫存控制**：即時追蹤房間可用數量
- **圖片展示**：整合 Unsplash 圖片服務

### 📅 訂房功能
- **快速訂房**：首頁快速訂房 Modal
- **詳細訂房**：完整的訂房流程與資料驗證
- **日期驗證**：自動檢查入住/退房日期合理性
- **庫存檢查**：防止超訂，即時顯示可用房間數
- **價格計算**：自動計算總價（房型價格 × 天數 × 房間數）

### ❤️ 收藏功能
- **一鍵收藏**：快速將喜愛的住宿加入收藏
- **收藏管理**：查看、移除收藏列表
- **快速訂房**：從收藏頁面直接跳轉訂房

### 👤 使用者系統
- **多角色支援**：管理員、業主、一般用戶
- **註冊/登入**：安全的身份認證系統
- **密碼重置**：郵件驗證的密碼重置功能
- **個人資料管理**：修改個人資訊

### 📊 管理功能

#### 管理員 (Admin)
- **住宿管理**：新增、編輯、刪除住宿
- **訂單管理**：查看所有訂單、取消訂單
- **用戶管理**：管理系統用戶、修改角色
- **數據統計**：系統總覽數據

#### 業主 (Owner)
- **我的住宿**：管理自己的住宿
- **訂單查看**：查看自己住宿的訂單
- **房型管理**：新增、編輯房型與價格

#### 一般用戶 (User)
- **我的訂單**：查看訂單歷史、取消訂單
- **我的收藏**：管理收藏的住宿
- **個人資料**：編輯個人資訊

---

## 🛠 技術架構

### 後端技術
- **框架**：Spring Boot 3.2.5
- **語言**：Java 17
- **建構工具**：Maven 3.9+
- **安全性**：Spring Security（表單登入 + CSRF 保護）
- **資料庫**：MySQL 8.0+
- **ORM**：Spring Data JPA
- **API 文件**：Swagger/OpenAPI 3.0

### 前端技術
- **模板引擎**：Thymeleaf
- **CSS 框架**：Bootstrap 5.1.3
- **JavaScript**：原生 ES6+
- **AJAX**：Fetch API
- **圖示**：Emoji + Bootstrap Icons

### 其他服務
- **郵件服務**：Spring Mail (SMTP)
- **圖片服務**：Unsplash API
- **容器化**：Docker（選用）

---

## 💻 系統需求

- **JDK**: 17 或以上
- **Maven**: 3.9 或以上
- **MySQL**: 8.0 或以上
- **記憶體**: 至少 512MB
- **磁碟空間**: 至少 500MB
- **瀏覽器**: Chrome、Firefox、Edge（最新版本）

---

## 🚀 快速開始

### 1. 克隆專案

```bash
git clone <repository-url>
cd my-booking-app-practice/booking
```

### 2. 設定環境

**⚠️ 重要：本專案需要配置資料庫和郵件服務**

#### 步驟 A：複製配置範本

```bash
cd src/main/resources
cp application.properties.example application.properties
```

#### 步驟 B：編輯配置文件

編輯 `application.properties` 並填入你的實際配置：

```properties
# MySQL 資料庫設定
spring.datasource.url=jdbc:mysql://localhost:3306/booking_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Taipei
spring.datasource.username=你的MySQL用戶名
spring.datasource.password=你的MySQL密碼

# Gmail SMTP 設定
spring.mail.username=你的Gmail帳號@gmail.com
spring.mail.password=你的Gmail應用程式密碼
app.mail.from=你的Gmail帳號@gmail.com
```

**📖 詳細配置說明請參考：[配置指南 (CONFIGURATION_GUIDE.md)](CONFIGURATION_GUIDE.md)**

#### 步驟 C：創建資料庫

在 MySQL 中執行：

```sql
CREATE DATABASE booking_db;
```

### 3. 編譯專案

```bash
mvn clean package
```

### 4. 啟動應用程式

#### 方法 A：使用 Maven
```bash
mvn spring-boot:run
```

#### 方法 B：使用 JAR
```bash
java -jar target/booking-0.0.1-SNAPSHOT.jar
```

### 5. 訪問系統

開啟瀏覽器，訪問：

- **首頁**：http://localhost:8080
- **API 文件**：http://localhost:8080/swagger-ui/index.html

**注意：** 首次啟動時，系統會自動創建資料表並初始化測試數據。

### 6. 預設帳號

系統啟動時會自動初始化測試數據：

| 角色 | 帳號 | 密碼 | 說明 |
|------|------|------|------|
| 管理員 | admin | admin123 | 完整系統管理權限 |
| 業主 | owner | owner123 | 管理自己的住宿 |
| 用戶 | user | user123 | 一般用戶權限 |

---

## 📁 專案結構

```
booking/
├── src/
│   ├── main/
│   │   ├── java/com/example/booking/
│   │   │   ├── BookingApplication.java          # 主程式入口
│   │   │   ├── config/                          # 配置類
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── MailConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── SwaggerConfig.java
│   │   │   ├── controller/                      # 控制器
│   │   │   │   ├── AccommodationController.java
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── BookingController.java
│   │   │   │   ├── FavoriteController.java
│   │   │   │   ├── OwnerController.java
│   │   │   │   ├── PageController.java
│   │   │   │   ├── RoomTypeController.java
│   │   │   │   └── UserController.java
│   │   │   ├── dto/                             # 數據傳輸物件
│   │   │   │   ├── BookingRequest.java
│   │   │   │   ├── FavoriteRequest.java
│   │   │   │   └── LoginRequest.java
│   │   │   ├── exception/                       # 異常處理
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── model/                           # 實體類
│   │   │   │   ├── Accommodation.java
│   │   │   │   ├── Booking.java
│   │   │   │   ├── Favorite.java
│   │   │   │   ├── PasswordResetToken.java
│   │   │   │   ├── RoomType.java
│   │   │   │   └── User.java
│   │   │   ├── repository/                      # 數據存取層
│   │   │   │   ├── AccommodationRepository.java
│   │   │   │   ├── BookingRepository.java
│   │   │   │   ├── FavoriteRepository.java
│   │   │   │   ├── PasswordResetTokenRepository.java
│   │   │   │   ├── RoomTypeRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   └── service/                         # 業務邏輯層
│   │   │       ├── BookingService.java
│   │   │       ├── EmailService.java
│   │   │       └── FavoriteService.java
│   │   └── resources/
│   │       ├── application.properties           # 應用配置
│   │       ├── data.sql                         # 初始化數據
│   │       ├── static/css/
│   │       │   └── style.css                    # 樣式表
│   │       └── templates/                       # Thymeleaf 模板
│   │           ├── index.html                   # 首頁
│   │           ├── login.html                   # 登入頁
│   │           ├── register.html                # 註冊頁
│   │           ├── accommodation-detail.html    # 住宿詳情
│   │           ├── user-bookings.html           # 我的訂單
│   │           ├── user-favorites.html          # 我的收藏
│   │           ├── user-profile.html            # 個人資料
│   │           ├── admin-dashboard.html         # 管理員儀表板
│   │           ├── owner-dashboard.html         # 業主儀表板
│   │           └── ...                          # 其他頁面
│   └── test/
│       └── java/                                # 測試代碼
├── target/                                       # 編譯輸出
├── data/                                         # H2 資料庫文件
├── pom.xml                                       # Maven 配置
├── Dockerfile                                    # Docker 配置
└── README.md                                     # 專案說明
```

---

## 📡 API 文件

### 訪問 Swagger UI

啟動應用後訪問：http://localhost:8080/swagger-ui/index.html

### 主要 API 端點

#### 住宿相關
- `GET /api/accommodations` - 獲取所有住宿
- `GET /api/accommodations/{id}` - 獲取單一住宿
- `GET /api/accommodations/search` - 搜尋住宿
- `GET /api/accommodations/available` - 查詢可用住宿
- `GET /api/accommodations/{id}/room-types` - 獲取住宿的房型

#### 訂房相關
- `POST /api/bookings/book` - 建立訂單（舊版）
- `POST /api/bookings/book-by-room-type` - 建立訂單（新版）
- `GET /api/bookings/my-bookings` - 獲取我的訂單
- `POST /api/bookings/{id}/cancel` - 取消訂單

#### 收藏相關
- `POST /api/favorites/toggle` - 切換收藏狀態
- `GET /api/favorites/my-favorites` - 獲取我的收藏
- `GET /api/favorites/check-multiple` - 批量檢查收藏狀態

#### 房型相關
- `GET /api/room-types/by-accommodation/{id}` - 獲取住宿的房型

#### 用戶相關
- `POST /api/auth/register` - 用戶註冊
- `POST /api/auth/login` - 用戶登入
- `POST /logout` - 登出
- `POST /api/auth/forgot-password` - 忘記密碼
- `POST /api/auth/reset-password` - 重置密碼

詳細的 API 文件請參考 [API_DOCUMENTATION_GUIDE.md](API_DOCUMENTATION_GUIDE.md)

---

## 👥 使用者角色

### 🔴 管理員 (ADMIN)
- 查看所有住宿、訂單、用戶
- 新增、編輯、刪除住宿
- 管理用戶角色
- 取消任何訂單
- 查看系統統計數據

### 🟡 業主 (OWNER)
- 管理自己的住宿
- 新增、編輯房型
- 查看自己住宿的訂單
- 查看訂房統計

### 🟢 一般用戶 (USER)
- 搜尋、瀏覽住宿
- 預訂住宿
- 管理訂單（查看、取消）
- 收藏住宿
- 編輯個人資料

---

## 🎯 功能說明

### 搜尋功能

系統支援多種搜尋方式的組合：

1. **關鍵字搜尋**：輸入地點或住宿名稱
2. **日期搜尋**：選擇入住和退房日期
3. **人數選擇**：指定入住人數（目前為顯示用）
4. **排序功能**：
   - 我們的推薦（預設）
   - 💰 價格：低到高
   - 💰 價格：高到低
   - ⭐ 評分最高
   - 🔥 最受歡迎（綜合訂房次數和評分）
   - 📍 距離最近（距離市中心）

### 訂房流程

1. **選擇住宿**：從首頁或搜尋結果選擇住宿
2. **選擇房型**：查看可用房型與價格
3. **選擇日期**：入住和退房日期
4. **選擇數量**：需要的房間數量
5. **確認訂單**：系統自動計算總價
6. **完成訂房**：訂單狀態為 PENDING（待確認）

### 價格計算

```
總價 = 房型單價 × 住宿天數 × 房間數量
```

例如：
- 房型單價：NT$ 2,000/晚
- 住宿天數：3 晚
- 房間數量：2 間
- **總價：NT$ 12,000**

### 庫存管理

系統即時計算房間可用數量：

```
可用房間 = 總房間數 - 該時段已預訂房間數
```

防止超訂機制：
- 檢查選擇的房間數是否超過可用數量
- 即時顯示剩餘房間數
- 預訂失敗時提示庫存不足

---

## 🗄 資料庫設計

### 主要資料表

#### User（用戶表）
- `id`: 主鍵
- `username`: 用戶名（唯一）
- `password`: 加密密碼
- `email`: 電子郵件
- `fullName`: 全名
- `role`: 角色（USER/OWNER/ADMIN）

#### Accommodation（住宿表）
- `id`: 主鍵
- `name`: 住宿名稱
- `location`: 地點
- `description`: 描述
- `pricePerNight`: 每晚價格
- `imageUrl`: 圖片網址
- `rating`: 評分
- `reviewCount`: 評論數
- `distanceFromCenter`: 距離市中心距離
- `bookingCount`: 訂房次數
- `owner`: 業主（外鍵）

#### RoomType（房型表）
- `id`: 主鍵
- `name`: 房型名稱
- `description`: 描述
- `pricePerNight`: 每晚價格
- `totalRooms`: 總房間數
- `accommodation`: 所屬住宿（外鍵）

#### Booking（訂單表）
- `id`: 主鍵
- `checkIn`: 入住日期
- `checkOut`: 退房日期
- `quantity`: 房間數量
- `totalPrice`: 總價
- `status`: 狀態（PENDING/CONFIRMED/CANCELLED）
- `roomType`: 房型（外鍵）
- `user`: 用戶（外鍵）

#### Favorite（收藏表）
- `id`: 主鍵
- `user`: 用戶（外鍵）
- `accommodation`: 住宿（外鍵）

#### PasswordResetToken（密碼重置令牌表）
- `id`: 主鍵
- `token`: 令牌（唯一）
- `user`: 用戶（外鍵）
- `expiryDate`: 過期時間

### ER 圖關係

```
User ──< Booking >── RoomType >── Accommodation
 │                                      │
 └────< Favorite >─────────────────────┘
 │
 └────< PasswordResetToken
 
User ──< Accommodation (owner)
```

---

## 💡 開發指南

### 添加新功能

1. **定義實體類**：在 `model/` 包中創建
2. **創建 Repository**：繼承 `JpaRepository`
3. **實現業務邏輯**：在 `service/` 包中
4. **創建控制器**：在 `controller/` 包中
5. **設計前端頁面**：在 `templates/` 目錄中

### 本地開發

```bash
# 啟動開發模式（自動重載）
mvn spring-boot:run

# 連接到 MySQL 資料庫
mysql -u root -p
use booking_db;
show tables;
```

### 測試

```bash
# 運行所有測試
mvn test

# 運行特定測試
mvn test -Dtest=BookingServiceTest
```

### 打包部署

```bash
# 打包成 JAR
mvn clean package

# 運行 JAR
java -jar target/booking-0.0.1-SNAPSHOT.jar

# Docker 部署
docker build -t booking-app .
docker run -p 8080:8080 booking-app
```

---

## 🐛 故障排除

### 常見問題

#### 1. 端口被佔用

**錯誤**：`Port 8080 is already in use`

**解決方法**：
```bash
# Windows
netstat -ano | findstr :8080
taskkill /F /PID <PID>

# 或修改 application.properties
server.port=8081
```

#### 2. 資料庫鎖定

**錯誤**：`Database may be already in use`

**解決方法**：
```bash
# 刪除資料庫鎖定文件
del data\testdb.lock.db
```

#### 3. 編譯錯誤

**錯誤**：`Source option 17 is no longer supported`

**解決方法**：
- 確認 JDK 版本為 17+
- 檢查 `pom.xml` 中的 Java 版本設定

#### 4. 郵件發送失敗

**錯誤**：`AuthenticationFailedException`

**解決方法**：
- 確認 Gmail 帳號已啟用「應用程式密碼」
- 檢查 `application.properties` 中的郵件設定

#### 5. 排序功能無效

**解決方法**：
- 確認應用程式已重新編譯
- 清除瀏覽器快取（Ctrl+Shift+Delete）
- 強制重新載入（Ctrl+F5）
- 查看瀏覽器主控台是否有錯誤

### 調試技巧

#### 啟用調試日誌

在 `application.properties` 中添加：
```properties
logging.level.com.example.booking=DEBUG
logging.level.org.springframework.web=DEBUG
```

#### 查看 SQL 語句

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

#### 使用瀏覽器開發者工具

- 按 **F12** 開啟
- **Console** 標籤：查看 JavaScript 錯誤和日誌
- **Network** 標籤：查看 API 請求和回應
- **Application** 標籤：查看 Cookies 和 Local Storage

---

## 🔐 安全性

### 已實現的安全措施

- ✅ **密碼加密**：使用 BCrypt 加密存儲
- ✅ **CSRF 保護**：防止跨站請求偽造
- ✅ **SQL 注入防護**：使用 JPA 參數化查詢
- ✅ **XSS 防護**：Thymeleaf 自動轉義
- ✅ **身份認證**：Spring Security 整合
- ✅ **角色授權**：基於角色的訪問控制

### 安全建議

**生產環境部署時請務必：**

1. 修改預設管理員密碼
2. 使用 HTTPS
3. 設定強密碼策略
4. 定期備份資料庫
5. 啟用日誌監控
6. 修改 `spring.jpa.hibernate.ddl-auto` 為 `validate` 或 `none`
7. 使用環境變數管理敏感資訊
8. 限制資料庫訪問 IP

---

## 🚀 未來規劃

### 計劃中的功能

- [ ] **付款整合**：串接第三方支付（綠界、藍新）
- [ ] **評論系統**：用戶評論與評分
- [ ] **即時通知**：WebSocket 訂單通知
- [ ] **多語言支援**：i18n 國際化
- [ ] **進階搜尋**：價格區間、設施篩選
- [ ] **地圖整合**：Google Maps 顯示位置
- [ ] **圖片上傳**：自定義住宿圖片
- [ ] **優惠券系統**：折扣碼與促銷活動
- [ ] **統計報表**：業主收入報表
- [ ] **行動版優化**：RWD 響應式設計強化

### 技術改進

- [ ] 前端框架升級（React/Vue.js）
- [ ] 微服務架構重構
- [ ] Redis 快取整合
- [ ] Elasticsearch 搜尋優化
- [ ] 單元測試覆蓋率提升至 80%+
- [ ] CI/CD 自動化部署

---

## 📝 版本歷史

### v1.0.0 (2025-11-09)
- ✅ 初始版本發布
- ✅ 完整的訂房功能
- ✅ 收藏系統
- ✅ 多角色管理
- ✅ 搜尋與排序
- ✅ 密碼重置
- ✅ API 文件

---

## 👨‍💻 作者

**開發團隊**

如有問題或建議，歡迎聯繫或提交 Issue。

---

## 📄 授權

本專案僅供學習與研究使用。

---

## 🙏 致謝

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Bootstrap](https://getbootstrap.com/)
- [Thymeleaf](https://www.thymeleaf.org/)
- [Unsplash](https://unsplash.com/)
- [H2 Database](https://www.h2database.com/)

---

## 📞 支援

如遇到問題，請：

1. 查看 [故障排除](#-故障排除) 章節
2. 閱讀 [API 文件](API_DOCUMENTATION_GUIDE.md)
3. 檢查應用程式日誌
4. 使用瀏覽器開發者工具調試

---

**祝您使用愉快！** 🎉

