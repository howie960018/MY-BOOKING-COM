# 🚀 Zeabur 部署指南

本指南將教你如何將訂房系統部署到 Zeabur，並正確管理敏感資訊。

---

## 📋 部署流程概覽

```
GitHub (公開代碼，無敏感資訊)
    ↓
Zeabur (從 GitHub 自動部署)
    ↓
環境變數 (在 Zeabur 控制台設定敏感資訊)
    ↓
應用程式 (讀取環境變數)
```

---

## 🔧 步驟一：修改 application.properties 使用環境變數

我們需要修改配置文件，讓它能從環境變數讀取敏感資訊。

### 當前問題
```properties
# ❌ 硬編碼的敏感資訊（不能上傳到 GitHub）
spring.datasource.password=2FTA93108
spring.mail.password=rgsqpqcanthwqars
```

### 解決方案
```properties
# ✅ 從環境變數讀取（可以安全上傳到 GitHub）
spring.datasource.password=${DB_PASSWORD:default_password}
spring.mail.password=${MAIL_PASSWORD:default_password}
```

**語法說明：**
- `${環境變數名稱:預設值}`
- 如果環境變數存在，使用環境變數的值
- 如果不存在，使用預設值（用於本地開發）

---

## 📝 步驟二：創建可上傳的 application.properties

創建一個使用環境變數的配置文件：

**文件：** `src/main/resources/application.properties`

```properties
# ===== Database Configuration =====
# Zeabur 會自動提供 MySQL 連接資訊
spring.datasource.url=${DATABASE_URL:jdbc:mysql://localhost:3306/booking_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Taipei}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:password}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=${DDL_AUTO:update}
spring.jpa.show-sql=${SHOW_SQL:false}
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Data Initialization
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true

# ===== Thymeleaf =====
spring.thymeleaf.cache=false
spring.jackson.serialization.fail-on-empty-beans=false
spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true

# ===== Server Encoding =====
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true

# ===== Server Port =====
server.port=${PORT:8080}

# ===== Spring Mail Configuration =====
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME:your_email@gmail.com}
spring.mail.password=${MAIL_PASSWORD:your_app_password}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

# Mail From
app.mail.from=${MAIL_FROM:${MAIL_USERNAME}}
app.mail.from-name=${MAIL_FROM_NAME:Booking Service}

# Application Base URL
app.base-url=${APP_BASE_URL:http://localhost:8080}

# ===== Swagger Configuration =====
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.tags-sorter=alpha
springdoc.swagger-ui.operations-sorter=alpha
springdoc.swagger-ui.try-it-out-enabled=true
springdoc.swagger-ui.display-request-duration=true
springdoc.swagger-ui.default-models-expand-depth=2
springdoc.swagger-ui.doc-expansion=none
springdoc.swagger-ui.show-extensions=true
```

**說明：**
- ✅ 這個文件**可以安全上傳到 GitHub**
- ✅ 包含預設值，本地開發時可以直接使用
- ✅ 部署到 Zeabur 時，會自動讀取環境變數

---

## 🌐 步驟三：在 Zeabur 設定環境變數

### 1. 登入 Zeabur

訪問：https://zeabur.com

### 2. 創建新專案

1. 點擊 "Create Project"
2. 選擇 "Deploy from GitHub"
3. 授權 Zeabur 訪問你的 GitHub
4. 選擇 `booking-system` 倉庫

### 3. 添加 MySQL 服務

Zeabur 會自動偵測到你需要 MySQL，或者手動添加：

1. 在專案中點擊 "Add Service"
2. 選擇 "MySQL"
3. Zeabur 會自動創建並配置 MySQL

### 4. 設定環境變數

在你的應用程式服務中，點擊 "Environment Variables"，添加以下變數：

#### 資料庫環境變數（如果 Zeabur 沒有自動設定）

```bash
# Zeabur 通常會自動設定這些，如果沒有，手動添加：
DATABASE_URL=<Zeabur 提供的 MySQL URL>
DB_USERNAME=<Zeabur 提供的用戶名>
DB_PASSWORD=<Zeabur 提供的密碼>
```

#### 郵件環境變數（必須手動設定）

```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=howie960018@gmail.com
MAIL_PASSWORD=rgsqpqcanthwqars
MAIL_FROM=howie960018@gmail.com
MAIL_FROM_NAME=訂房系統
```

#### 應用程式環境變數

```bash
# 生產環境設定
DDL_AUTO=update
SHOW_SQL=false
APP_BASE_URL=https://你的專案名稱.zeabur.app
```

### 5. 重新部署

設定完環境變數後，點擊 "Redeploy" 重新部署應用程式。

---

## 📂 步驟四：更新 .gitignore

確保 `.gitignore` 正確配置：

```gitignore
# Compiled class files
*.class

# Log files
*.log

# Package Files
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

# Maven
target/

# Database files
data/
*.mv.db
*.trace.db
*.lock.db

# IDE
.idea/
.vscode/
*.iml

# OS
.DS_Store
Thumbs.db

# ===== 本地環境配置 =====
# 如果你想保留一個本地專用的配置文件，可以這樣命名並忽略：
application-local.properties
.env
.env.local
```

---

## 🔄 步驟五：本地開發配置

為了方便本地開發，你有兩個選擇：

### 選項 A：使用本地配置文件（推薦）

創建 `application-local.properties`（這個文件不會上傳到 GitHub）：

```properties
# 本地開發配置
spring.datasource.url=jdbc:mysql://localhost:3306/booking_db?useSSL=false
spring.datasource.username=root
spring.datasource.password=2FTA93108

spring.mail.username=howie960018@gmail.com
spring.mail.password=rgsqpqcanthwqars
app.mail.from=howie960018@gmail.com
```

啟動時指定使用本地配置：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 選項 B：使用環境變數（進階）

在本地設定環境變數：

**Windows (PowerShell):**
```powershell
$env:DB_PASSWORD="2FTA93108"
$env:MAIL_USERNAME="howie960018@gmail.com"
$env:MAIL_PASSWORD="rgsqpqcanthwqars"
mvn spring-boot:run
```

**Windows (CMD):**
```cmd
set DB_PASSWORD=2FTA93108
set MAIL_USERNAME=howie960018@gmail.com
set MAIL_PASSWORD=rgsqpqcanthwqars
mvn spring-boot:run
```

---

## 🎯 步驟六：部署到 Zeabur

### 1. 推送代碼到 GitHub

```bash
cd C:\my-booking-app-practice

# 初始化 Git（如果尚未初始化）
git init

# 添加文件
git add .

# 提交
git commit -m "配置環境變數，準備部署到 Zeabur"

# 連接到 GitHub
git remote add origin https://github.com/你的用戶名/booking-system.git
git push -u origin main
```

### 2. 在 Zeabur 控制台操作

1. **創建專案**：點擊 "Create Project"
2. **連接 GitHub**：選擇你的倉庫
3. **添加 MySQL**：Zeabur 會自動建議添加
4. **設定環境變數**：添加郵件相關的環境變數
5. **部署**：Zeabur 會自動開始部署

### 3. 等待部署完成

- Zeabur 會自動建構你的應用程式
- 部署成功後，你會獲得一個 URL：`https://你的專案名稱.zeabur.app`

---

## 🔍 驗證部署

### 1. 檢查應用程式日誌

在 Zeabur 控制台中，點擊你的服務 → "Logs"，確認：

```
✅ Started BookingApplication in X.XXX seconds
✅ No errors related to database connection
✅ No errors related to mail configuration
```

### 2. 訪問應用程式

訪問：`https://你的專案名稱.zeabur.app`

測試功能：
- ✅ 首頁正常顯示
- ✅ 可以註冊新用戶
- ✅ 可以登入
- ✅ 可以瀏覽住宿
- ✅ 郵件發送功能（忘記密碼）

### 3. 檢查資料庫連接

訪問：`https://你的專案名稱.zeabur.app/api/accommodations`

應該能看到 JSON 格式的住宿資料。

---

## 🐛 常見問題排除

### 問題 1：應用程式無法啟動

**錯誤：** `Access denied for user...`

**解決方法：**
1. 檢查 Zeabur 是否正確設定了 MySQL 服務
2. 檢查環境變數 `DATABASE_URL`、`DB_USERNAME`、`DB_PASSWORD` 是否正確
3. 在 Zeabur MySQL 服務中查看連接資訊

### 問題 2：郵件發送失敗

**錯誤：** `AuthenticationFailedException`

**解決方法：**
1. 確認環境變數 `MAIL_PASSWORD` 使用的是 Gmail **應用程式密碼**
2. 確認環境變數 `MAIL_USERNAME` 格式正確
3. 檢查 Gmail 帳號是否啟用了「兩步驟驗證」

### 問題 3：資料表未創建

**錯誤：** `Table 'booking_db.users' doesn't exist`

**解決方法：**
1. 確認 `spring.jpa.hibernate.ddl-auto` 設定為 `update` 或 `create`
2. 確認 `spring.sql.init.mode=always` 已設定
3. 檢查 `data.sql` 文件是否在正確位置

### 問題 4：連接超時

**錯誤：** `Connection timed out`

**解決方法：**
1. 檢查 Zeabur MySQL 服務是否正常運行
2. 確認應用程式和 MySQL 在同一個專案中
3. 檢查防火牆設定

---

## 📊 環境變數對照表

| 環境變數 | 用途 | 範例值 | 必要性 |
|---------|------|--------|--------|
| `DATABASE_URL` | MySQL 連接 URL | `jdbc:mysql://...` | ✅ 必要 |
| `DB_USERNAME` | 資料庫用戶名 | `root` | ✅ 必要 |
| `DB_PASSWORD` | 資料庫密碼 | `your_password` | ✅ 必要 |
| `MAIL_HOST` | SMTP 主機 | `smtp.gmail.com` | ✅ 必要 |
| `MAIL_PORT` | SMTP 端口 | `587` | ✅ 必要 |
| `MAIL_USERNAME` | 郵件帳號 | `your@gmail.com` | ✅ 必要 |
| `MAIL_PASSWORD` | 郵件密碼 | `app_password` | ✅ 必要 |
| `MAIL_FROM` | 寄件者 | `your@gmail.com` | ⚪ 可選 |
| `MAIL_FROM_NAME` | 寄件者名稱 | `訂房系統` | ⚪ 可選 |
| `APP_BASE_URL` | 應用程式 URL | `https://your-app.zeabur.app` | ✅ 必要 |
| `DDL_AUTO` | Hibernate DDL | `update` | ⚪ 可選 |
| `SHOW_SQL` | 顯示 SQL | `false` | ⚪ 可選 |
| `PORT` | 服務端口 | `8080` | ⚪ 可選 |

---

## 🎉 部署成功檢查清單

- [ ] GitHub 倉庫已創建並推送代碼
- [ ] `application.properties` 使用環境變數
- [ ] Zeabur 專案已創建
- [ ] MySQL 服務已添加
- [ ] 所有環境變數已設定
- [ ] 應用程式部署成功
- [ ] 可以訪問首頁
- [ ] 資料庫連接正常
- [ ] 郵件功能正常
- [ ] 所有功能測試通過

---

## 🔒 安全最佳實踐

### ✅ 應該做的

1. **使用環境變數** 管理所有敏感資訊
2. **定期輪換密碼** 特別是資料庫和郵件密碼
3. **啟用 HTTPS** Zeabur 預設提供
4. **監控日誌** 定期檢查異常活動
5. **限制訪問** 設定適當的 CORS 政策

### ❌ 不應該做的

1. ❌ 不要在代碼中硬編碼密碼
2. ❌ 不要在公開的 Issue 或 PR 中討論敏感資訊
3. ❌ 不要分享 Zeabur 環境變數截圖
4. ❌ 不要使用弱密碼
5. ❌ 不要在生產環境開啟 `show-sql=true`

---

## 📚 相關資源

- [Zeabur 官方文檔](https://zeabur.com/docs)
- [Zeabur 環境變數指南](https://zeabur.com/docs/environment/variables)
- [Spring Boot 外部化配置](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Gmail SMTP 設定](https://support.google.com/a/answer/176600)

---

## 💡 總結

使用環境變數的好處：

1. ✅ **安全**：敏感資訊不會出現在代碼中
2. ✅ **靈活**：不同環境使用不同配置
3. ✅ **方便**：部署時只需在 Zeabur 設定一次
4. ✅ **團隊協作**：每個開發者可以使用自己的配置

**核心概念：**
- 代碼（GitHub）：公開，不含敏感資訊
- 配置（Zeabur）：私密，通過環境變數管理

這樣你就可以安全地將代碼上傳到 GitHub，同時在 Zeabur 上正常部署運行！🚀

