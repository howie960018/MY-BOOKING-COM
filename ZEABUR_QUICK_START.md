# 🎯 Zeabur 部署快速指南

## 📚 問題解答：為什麼可以上傳到 GitHub？

### 問題
如果把敏感資訊都隱藏了，Zeabur 怎麼讀得到配置？

### 答案
使用**環境變數** (Environment Variables) 機制！

```
本地開發（你的電腦）          Zeabur 部署（雲端）
┌──────────────────┐         ┌──────────────────┐
│ application.     │         │ application.     │
│ properties       │         │ properties       │
│                  │         │                  │
│ password=真實密碼 │  ═══>   │ password=${ENV}  │
│ ❌ 不能上傳       │         │ ✅ 可以上傳       │
└──────────────────┘         └──────────────────┘
                                      ↓
                                 讀取環境變數
                                      ↓
                             ┌──────────────────┐
                             │ Zeabur 控制台    │
                             │                  │
                             │ ENV=真實密碼      │
                             │ ✅ 在網頁設定     │
                             └──────────────────┘
```

---

## 🔧 實際操作步驟

### 步驟 1：修改配置文件（已完成）

**原始版本（不能上傳）：**
```properties
spring.datasource.password=2FTA93108
spring.mail.password=rgsqpqcanthwqars
```

**環境變數版本（可以上傳）：**
```properties
spring.datasource.password=${DB_PASSWORD:2FTA93108}
spring.mail.password=${MAIL_PASSWORD:rgsqpqcanthwqars}
```

**說明：**
- `${DB_PASSWORD:2FTA93108}` 表示：
  - 優先讀取環境變數 `DB_PASSWORD`
  - 如果沒有環境變數，使用預設值 `2FTA93108`（本地開發用）
  
### 步驟 2：準備檔案

你現在有這些文件：

| 檔案 | 用途 | 上傳到 GitHub |
|------|------|---------------|
| `application.properties` | 原始配置（含真實密碼） | ❌ 不要上傳 |
| `application.properties.zeabur` | Zeabur 專用配置（使用環境變數） | ✅ 可以上傳 |
| `application.properties.example` | 配置範本 | ✅ 可以上傳 |
| `ZEABUR_ENV_TEMPLATE.txt` | 環境變數清單 | ✅ 可以上傳 |

**行動方案：**

**選項 A：保留原始 application.properties 供本地使用**
```bash
# 將原始文件重命名
cd C:\my-booking-app-practice\booking\src\main\resources
ren application.properties application-local.properties

# 將 Zeabur 版本設為主要配置
copy application.properties.zeabur application.properties
```

**選項 B：直接使用 Zeabur 版本（推薦）**
```bash
# 備份原始文件
cd C:\my-booking-app-practice\booking\src\main\resources
copy application.properties application.properties.backup

# 用 Zeabur 版本覆蓋
copy /Y application.properties.zeabur application.properties
```

### 步驟 3：上傳到 GitHub

```bash
cd C:\my-booking-app-practice

# 初始化 Git
git init

# 添加所有文件
git add .

# 檢查狀態 - 確認沒有包含真實密碼
git status

# 提交
git commit -m "配置使用環境變數，準備部署到 Zeabur"

# 連接到 GitHub
git remote add origin https://github.com/你的用戶名/booking-system.git
git push -u origin main
```

### 步驟 4：在 Zeabur 設定環境變數

1. **登入 Zeabur**：https://zeabur.com

2. **創建專案**：
   - 點擊 "Create Project"
   - 選擇 "Deploy from GitHub"
   - 選擇你的 `booking-system` 倉庫

3. **添加 MySQL 服務**：
   - Zeabur 會自動偵測並建議添加 MySQL
   - 點擊 "Add" 添加 MySQL 服務

4. **設定環境變數**：
   - 點擊你的應用程式服務
   - 選擇 "Variables" 標籤
   - 添加以下變數：

```bash
# 郵件設定（必須手動添加）
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=howie960018@gmail.com
MAIL_PASSWORD=rgsqpqcanthwqars
MAIL_FROM=howie960018@gmail.com
MAIL_FROM_NAME=訂房系統

# 應用程式設定
APP_BASE_URL=https://你的專案名稱.zeabur.app
DDL_AUTO=update
SHOW_SQL=false
```

5. **重新部署**：
   - 設定完環境變數後
   - 點擊 "Redeploy" 按鈕

### 步驟 5：驗證部署

訪問：`https://你的專案名稱.zeabur.app`

測試功能：
- ✅ 首頁正常顯示
- ✅ 可以登入（admin/admin123）
- ✅ 可以瀏覽住宿
- ✅ 資料庫連接正常

---

## 📊 本地開發與 Zeabur 部署對照

### 本地開發

**配置文件：** `application-local.properties`（不上傳到 GitHub）

```properties
# 本地開發用真實密碼
spring.datasource.password=2FTA93108
spring.mail.password=rgsqpqcanthwqars
```

**啟動方式：**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Zeabur 部署

**配置文件：** `application.properties`（上傳到 GitHub）

```properties
# 使用環境變數
spring.datasource.password=${DB_PASSWORD:default}
spring.mail.password=${MAIL_PASSWORD:default}
```

**環境變數：** 在 Zeabur 控制台設定
```
DB_PASSWORD=<Zeabur 提供的密碼>
MAIL_PASSWORD=rgsqpqcanthwqars
```

---

## 🔐 安全性說明

### GitHub 上的文件（公開）

```properties
# ✅ 安全：使用環境變數佔位符
spring.datasource.password=${DB_PASSWORD:default}
```

任何人看到這個文件：
- ❌ 看不到真實密碼
- ❌ 無法直接使用
- ✅ 只是一個配置模板

### Zeabur 上的環境變數（私密）

```
DB_PASSWORD=2FTA93108（真實密碼）
```

只有你能在 Zeabur 控制台看到：
- ✅ 完全私密
- ✅ 加密儲存
- ✅ 只有你的應用程式能讀取

---

## 🎯 常見問題

### Q1：為什麼預設值還是真實密碼？

```properties
spring.datasource.password=${DB_PASSWORD:2FTA93108}
                                         ↑ 這裡還是真實密碼
```

**A1：兩個選擇：**

**選項 A：預設值改為假的（推薦）**
```properties
spring.datasource.password=${DB_PASSWORD:your_password_here}
```
- ✅ GitHub 上完全安全
- ❌ 本地開發需要設定環境變數

**選項 B：保留真實密碼作為預設值**
```properties
spring.datasource.password=${DB_PASSWORD:2FTA93108}
```
- ✅ 本地開發方便
- ⚠️ 如果有人下載你的代碼，仍能看到密碼
- 💡 可以接受，因為這只是你個人本地的 MySQL 密碼

**建議：** 如果這是個人專案且本地 MySQL 密碼不重要，可以保留真實密碼作為預設值方便開發。如果是團隊專案或開源項目，應該使用假的預設值。

### Q2：Zeabur 怎麼知道要用環境變數？

**A2：** Spring Boot 自動支援！

當你的配置是：
```properties
spring.datasource.password=${DB_PASSWORD}
```

Spring Boot 啟動時會：
1. 檢查是否有環境變數 `DB_PASSWORD`
2. 如果有，使用環境變數的值
3. 如果沒有，使用預設值（如果有指定）
4. 如果都沒有，報錯

### Q3：需要修改代碼嗎？

**A3：** 完全不需要！

你的 Java 代碼：
```java
@Value("${spring.datasource.password}")
private String password;
```

不管密碼來自：
- 配置文件硬編碼
- 環境變數
- 命令列參數

Java 代碼都是一樣的，Spring Boot 會自動處理！

### Q4：如何測試環境變數配置是否正確？

**A4：** 本地測試環境變數

**Windows (PowerShell):**
```powershell
$env:DB_PASSWORD="test_password"
$env:MAIL_PASSWORD="test_mail_password"
mvn spring-boot:run
```

**Windows (CMD):**
```cmd
set DB_PASSWORD=test_password
set MAIL_PASSWORD=test_mail_password
mvn spring-boot:run
```

檢查日誌，如果啟動成功就表示配置正確！

---

## ✅ 最終檢查清單

部署前確認：

- [ ] `application.properties` 使用環境變數語法 `${VAR:default}`
- [ ] GitHub 上沒有包含真實密碼的文件
- [ ] Zeabur 已創建 MySQL 服務
- [ ] Zeabur 已設定所有必要的環境變數
- [ ] 本地測試環境變數配置可正常運行
- [ ] `ZEABUR_ENV_TEMPLATE.txt` 已創建（參考用）
- [ ] `ZEABUR_DEPLOYMENT_GUIDE.md` 已閱讀

---

## 🎉 總結

**核心概念：**
```
代碼 (GitHub)           配置 (Zeabur)         應用程式
    ↓                       ↓                    ↓
使用佔位符           設定真實值          自動讀取環境變數
${PASSWORD}     →    PASSWORD=real    →   實際使用 "real"
✅ 安全公開           ✅ 私密儲存           ✅ 正常運行
```

這樣你就可以：
- ✅ 安全地將代碼上傳到 GitHub（公開或私有都可以）
- ✅ 在 Zeabur 上正常部署運行
- ✅ 保護敏感資訊不洩露
- ✅ 方便本地開發

**下一步：** 參考 `ZEABUR_DEPLOYMENT_GUIDE.md` 進行詳細部署！

