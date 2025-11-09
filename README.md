# 🏨 訂房系統 (Hotel Booking System)

一個功能完整的線上訂房管理系統，**採用開放瀏覽設計**（類似 Booking.com），提供住宿搜尋、預訂、管理等完整功能。

## 📋 目錄

- [系統特色](#系統特色)
- [系統功能](#系統功能)
- [技術架構](#技術架構)
- [快速開始](#快速開始)
- [本地部署](#本地部署)
- [雲端部署 (Zeabur)](#雲端部署-zeabur)
- [測試帳號](#測試帳號)
- [API 文件](#api-文件)
- [專案結構](#專案結構)

---

## ⭐ 系統特色

### 🌐 開放瀏覽設計
- **無需登入即可瀏覽**：訪客可以自由查看所有住宿、搜尋、查看詳情
- **需要登入才能預訂**：預訂、收藏等功能需要登入，確保安全性
- **符合業界標準**：與 Booking.com、Airbnb 等主流平台一致的用戶體驗
- **流暢的使用者旅程**：瀏覽 → 登入 → 預訂，降低使用門檻

### 📱 響應式設計
- **手機版優化**：卡片式訂單列表、全寬按鈕、適當字體大小
- **桌面版體驗**：完整功能、表格顯示、豐富的互動
- **漢堡選單**：所有管理頁面支援行動裝置導覽

### 🔐 完整權限控管
- **三種角色**：管理員、房東、一般用戶
- **細緻的權限設計**：每個功能都有適當的權限檢查
- **Session 認證**：安全可靠的登入機制

---

## 🎯 系統功能

### 🔓 公開功能（無需登入）
- ✅ 首頁瀏覽
- ✅ 住宿搜尋（關鍵字、日期、人數）
- ✅ 智慧排序（價格、評分、距離、熱門度）
- ✅ 住宿詳情查看
- ✅ 房型資訊查看
- ✅ 評論查看

### 🔐 用戶功能（需登入）
- ✅ 房型選擇與預訂
- ✅ 訂單管理（查看、取消）
- ✅ 收藏功能
- ✅ 發表評論
- ✅ 個人資料管理
- ✅ 密碼重置（郵件驗證）

### 🏠 房東功能
- ✅ 住宿管理（新增、編輯、刪除）
- ✅ 房型管理（CRUD、庫存管理）
- ✅ 訂單查看與處理
- ✅ 營運數據統計（圖表）

### 👨‍💼 管理員功能
- ✅ 全局住宿管理
- ✅ 全局訂單管理
- ✅ 用戶管理（角色升級/降級）
- ✅ 系統統計報表

---

## 🛠️ 技術架構

### 後端技術
- **框架**: Spring Boot 3.2.5
- **資料庫**: MySQL 8.0
- **ORM**: Spring Data JPA / Hibernate
- **安全性**: Spring Security 6.x (Session 認證)
- **API 文件**: SpringDoc OpenAPI 3.0 (Swagger)
- **郵件**: Spring Mail (SMTP)
- **報表**: Apache POI (Excel 匯出)

### 前端技術
- **模板引擎**: Thymeleaf
- **UI 框架**: Bootstrap 5.1.3
- **圖表**: Chart.js
- **JavaScript**: 原生 ES6+

### 測試
- **單元測試**: JUnit 5 + Mockito
- **測試覆蓋率**: 95%+

---

## 🚀 快速開始

### 前置需求

確保您的電腦已安裝：

- **Java 17** 或以上
- **Maven 3.6+**
- **MySQL 8.0+**
- **Git**

### 安裝步驟

1. **克隆專案**
   ```bash
   git clone https://github.com/howie960018/MY-BOOKING-COM.git
   cd MY-BOOKING-COM
   ```

2. **創建資料庫**
   ```sql
   CREATE DATABASE booking_db;
   ```

3. **設定環境變數**
   
   **Windows (PowerShell):**
   ```powershell
   $env:DATABASE_URL="jdbc:mysql://localhost:3306/booking_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Taipei"
   $env:DB_USERNAME="root"
   $env:DB_PASSWORD="你的MySQL密碼"
   $env:MAIL_USERNAME="你的Gmail帳號"
   $env:MAIL_PASSWORD="你的Gmail應用程式密碼"
   ```

   **或使用提供的啟動腳本** (`start-dev.bat`)

4. **啟動應用**
   ```bash
   cd booking
   mvn spring-boot:run
   ```

5. **訪問系統**
   - **首頁**：http://localhost:8080 （🔓 無需登入）
   - **API 文件**：http://localhost:8080/swagger-ui/index.html
   
6. **開始使用**
   
   **訪客模式（無需登入）：**
   - ✅ 瀏覽所有住宿
   - ✅ 使用搜尋和排序功能
   - ✅ 查看住宿詳情和房型
   - ✅ 查看評論
   
   **登入後享有完整功能：**
   - ✅ 預訂房間
   - ✅ 收藏住宿
   - ✅ 查看和管理訂單
   - ✅ 發表評論
   - ✅ 管理個人資料

---

## 💻 本地部署

### 方法 1: 使用啟動腳本 (推薦)

**Windows:**
```bash
# 雙擊執行
start-dev.bat
```

腳本會自動：
- 設定環境變數
- 啟動 Spring Boot 應用
- 使用測試用的資料庫和郵件設定

### 方法 2: 手動配置

1. **修改 application.properties.example**
   ```bash
   cp booking/src/main/resources/application.properties.example application.properties
   ```

2. **編輯配置文件**
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/booking_db
   spring.datasource.username=root
   spring.datasource.password=你的密碼
   spring.mail.username=你的Gmail
   spring.mail.password=你的Gmail應用程式密碼
   ```

3. **啟動應用**
   ```bash
   cd booking
   mvn spring-boot:run
   ```

---

## ☁️ 雲端部署 (Zeabur)

### 部署步驟

1. **登入 Zeabur**
   - 訪問：https://zeabur.com
   - 使用 GitHub 帳號登入

2. **創建專案**
   - 點擊 "Create Project"
   - 選擇 "Deploy from GitHub"
   - 選擇此倉庫：`MY-BOOKING-COM`

3. **添加 MySQL 服務**
   - 在專案中點擊 "Add Service"
   - 選擇 "MySQL"
   - 等待服務啟動

4. **配置環境變數**

   在應用程式服務的 **Variables** 標籤中添加：

   ```bash
   # 資料庫（Zeabur MySQL 自動提供）
   DATABASE_URL=${MYSQL_URL}
   DB_USERNAME=${MYSQL_USERNAME}
   DB_PASSWORD=${MYSQL_PASSWORD}

   # 郵件服務（需手動添加）
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-app-password
   
   # 應用程式設定
   APP_BASE_URL=https://your-project.zeabur.app
   DDL_AUTO=update
   SHOW_SQL=false
   ```

5. **部署**
   - Zeabur 會自動檢測 `pom.xml`
   - 自動構建並部署
   - 等待部署完成（約 2-3 分鐘）

6. **訪問應用**
   - 訪問：`https://your-project.zeabur.app`

### 環境變數說明

| 變數名稱 | 說明 | 範例 |
|---------|------|------|
| `DATABASE_URL` | MySQL 連接 URL | `jdbc:mysql://...` |
| `DB_USERNAME` | 資料庫用戶名 | `root` |
| `DB_PASSWORD` | 資料庫密碼 | `your-password` |
| `MAIL_USERNAME` | Gmail 帳號 | `your@gmail.com` |
| `MAIL_PASSWORD` | Gmail 應用程式密碼 | `xxxx xxxx xxxx xxxx` |
| `APP_BASE_URL` | 應用程式網址 | `https://xxx.zeabur.app` |
| `DDL_AUTO` | Hibernate DDL 模式 | `update` |
| `SHOW_SQL` | 是否顯示 SQL | `false` |

**詳細部署教學請參考：** [ZEABUR_DEPLOYMENT_GUIDE.md](ZEABUR_DEPLOYMENT_GUIDE.md)

---

## 👥 測試帳號

### 🌐 訪客模式（推薦先體驗）

**無需註冊即可使用：**
- 直接訪問首頁開始瀏覽
- 搜尋住宿、查看詳情、瀏覽房型
- 查看其他用戶的評論
- 體驗完整的瀏覽功能

### 🔐 測試帳號（需要登入功能時使用）

系統預設提供以下測試帳號：

| 角色 | 帳號 | 密碼 | 可用功能 |
|------|------|------|----------|
| 👨‍💼 管理員 | `admin` | `password` | • 全局住宿管理<br>• 全局訂單管理<br>• 用戶管理<br>• 系統統計 |
| 🏠 房東 1 | `owner1` | `123456` | • 管理自己的住宿<br>• 房型管理<br>• 查看訂單<br>• 營運統計 |
| 🏠 房東 2 | `owner2` | `123456` | • 管理自己的住宿<br>• 房型管理<br>• 查看訂單<br>• 營運統計 |
| 👤 用戶 1 | `user1` | `123456` | • 預訂房間<br>• 收藏住宿<br>• 管理訂單<br>• 發表評論 |
| 👤 用戶 2 | `user2` | `123456` | • 預訂房間<br>• 收藏住宿<br>• 管理訂單<br>• 發表評論 |
| 👤 用戶 3 | `user3` | `123456` | • 預訂房間<br>• 收藏住宿<br>• 管理訂單<br>• 發表評論 |

**提示：** 登出後會返回首頁，可繼續以訪客身份瀏覽

---

## 📖 API 文件

### Swagger UI

訪問 Swagger UI 查看完整的 API 文件：

- **本地環境**: http://localhost:8080/swagger-ui/index.html
- **Zeabur 環境**: https://your-project.zeabur.app/swagger-ui/index.html

### 主要 API 端點

#### 公開 API（無需登入）
- `GET /api/accommodations` - 獲取所有住宿
- `GET /api/accommodations/search` - 搜尋住宿
- `GET /api/accommodations/{id}` - 獲取住宿詳情
- `GET /api/room-types/by-accommodation/{id}` - 獲取房型列表

#### 需登入 API
- `POST /api/bookings/book-by-room-type` - 創建訂單
- `GET /api/bookings/my-bookings` - 獲取我的訂單
- `POST /user/favorites/api/toggle/{id}` - 切換收藏狀態

#### 房東 API
- `GET /api/owner/accommodations` - 獲取我的住宿
- `POST /api/owner/accommodations` - 新增住宿
- `GET /api/owner/stats` - 獲取統計數據

#### 管理員 API
- `GET /api/admin/users` - 獲取所有用戶
- `PUT /api/admin/users/{id}/role` - 修改用戶角色
- `GET /api/admin/bookings` - 獲取所有訂單

---

## 📁 專案結構

```
my-booking-app-practice/
├── booking/                          # Spring Boot 應用程式
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/example/booking/
│   │   │   │       ├── config/       # 配置類
│   │   │   │       ├── controller/   # 控制器
│   │   │   │       ├── dto/          # 資料傳輸物件
│   │   │   │       ├── model/        # 實體模型
│   │   │   │       ├── repository/   # 資料存取層
│   │   │   │       ├── service/      # 業務邏輯層
│   │   │   │       └── exception/    # 異常處理
│   │   │   └── resources/
│   │   │       ├── application.properties  # 配置文件
│   │   │       ├── data.sql               # 初始化數據
│   │   │       ├── static/                # 靜態資源
│   │   │       └── templates/             # Thymeleaf 模板
│   │   └── test/                     # 測試
│   ├── pom.xml                       # Maven 配置
│   └── Dockerfile                    # Docker 配置
├── start-dev.bat                     # 本地開發啟動腳本
├── README.md                         # 專案說明（本文件）
└── ZEABUR_DEPLOYMENT_GUIDE.md        # Zeabur 部署指南
```

---

## 🔧 開發指南

### 本地開發

1. **修改代碼**
2. **重新啟動應用**
   ```bash
   mvn spring-boot:run
   ```
3. **訪問測試**
   - http://localhost:8080

### 推送到生產環境

1. **提交代碼**
   ```bash
   git add .
   git commit -m "your message"
   git push
   ```

2. **自動部署**
   - Zeabur 會自動檢測推送
   - 自動重新部署
   - 無需手動操作

---

## 🧪 測試

### 運行測試

```bash
cd booking
mvn test
```

### 測試覆蓋率

```bash
mvn test jacoco:report
```

測試報告位於：`target/site/jacoco/index.html`

---

## 📝 Gmail 應用程式密碼設定

### 步驟

1. **啟用兩步驟驗證**
   - 訪問：https://myaccount.google.com/security
   - 啟用「兩步驟驗證」

2. **產生應用程式密碼**
   - 搜尋「應用程式密碼」
   - 選擇「郵件」和「其他裝置」
   - 輸入名稱：「訂房系統」
   - 點擊「產生」

3. **複製密碼**
   - 複製 16 位密碼（包含空格）
   - 在環境變數中使用此密碼

---

## 🐛 常見問題

### 1. 資料庫連接失敗

**錯誤：** `Communications link failure`

**解決：**
- 確認 MySQL 服務正在運行
- 檢查資料庫名稱、帳號、密碼是否正確
- 確認 `DATABASE_URL` 格式正確

### 2. 郵件發送失敗

**錯誤：** `Authentication failed`

**解決：**
- 確認使用 Gmail **應用程式密碼**，不是帳號密碼
- 檢查兩步驟驗證是否已啟用
- 確認 `MAIL_USERNAME` 和 `MAIL_PASSWORD` 正確

### 3. 端口被占用

**錯誤：** `Port 8080 is already in use`

**解決：**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /F /PID <PID>

# 或修改端口
set PORT=8081
mvn spring-boot:run
```

---

## 📄 授權

MIT License

---

## 👨‍💻 作者

- **開發者**: Howard
- **GitHub**: https://github.com/howie960018
- **專案倉庫**: https://github.com/howie960018/MY-BOOKING-COM

---

## 🙏 致謝

感謝使用此訂房系統！如有任何問題或建議，歡迎提出 Issue 或 Pull Request。

---

**最後更新**: 2025-11-09  
**版本**: 1.0.0

