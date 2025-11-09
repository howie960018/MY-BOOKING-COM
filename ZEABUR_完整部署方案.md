# 🚀 Zeabur 部署完整方案

## 📋 問題與解決方案

### ❓ 你的疑問
```
如果把配置文件隱藏了，Zeabur 連接 GitHub 後怎麼讀得到配置？
```

### ✅ 解決方案
```
使用「環境變數」機制：
  代碼（GitHub）  ═══>  只包含佔位符 ${變數名}
  配置（Zeabur）  ═══>  在控制台設定真實值
  運行（應用）    ═══>  自動讀取環境變數
```

---

## 🎯 三種配置方式對照

### 方式 1：硬編碼（❌ 不安全）
```properties
spring.datasource.password=2FTA93108
```
- ❌ 密碼暴露在代碼中
- ❌ 上傳 GitHub 會洩漏
- ❌ 無法用於生產環境

### 方式 2：環境變數（✅ 推薦）
```properties
spring.datasource.password=${DB_PASSWORD:2FTA93108}
```
- ✅ 代碼中沒有真實密碼
- ✅ 可以安全上傳 GitHub
- ✅ 本地開發使用預設值
- ✅ Zeabur 部署讀取環境變數

### 方式 3：完全環境變數（✅ 最安全）
```properties
spring.datasource.password=${DB_PASSWORD}
```
- ✅ 沒有任何真實密碼
- ✅ 完全依賴環境變數
- ⚠️ 本地開發也需設定環境變數

---

## 📂 你現在擁有的文件

### 配置文件（在 `booking/src/main/resources/`）

```
├── application.properties.new         ✅ 使用環境變數（推薦使用）
├── application.properties.backup      📦 原始配置（備份）
├── application.properties.zeabur      📄 Zeabur 範本
├── application.properties.example     📄 公開範本
└── application.properties            ⚠️ 需要替換
```

### 說明文件（在專案根目錄）

```
├── ZEABUR_QUICK_START.md            ⭐ 快速上手（先看這個）
├── ZEABUR_DEPLOYMENT_GUIDE.md       📖 詳細部署教學
├── ZEABUR_ENV_TEMPLATE.txt          📋 環境變數清單
├── CONFIGURATION_GUIDE.md           🔧 本地配置指南
└── SECURITY_REPORT.md               🔐 安全檢查報告
```

### 工具腳本

```
└── booking/替換配置文件.bat          🔄 一鍵替換工具
```

---

## 🔄 完整部署流程

### 階段 1：準備配置文件

#### 選項 A：使用腳本（推薦）
```cmd
cd C:\my-booking-app-practice\booking
替換配置文件.bat
```

#### 選項 B：手動替換
```cmd
cd C:\my-booking-app-practice\booking\src\main\resources
del application.properties
ren application.properties.new application.properties
```

### 階段 2：推送到 GitHub

```bash
cd C:\my-booking-app-practice

# 初始化（如果還沒有）
git init

# 添加所有文件
git add .

# 提交
git commit -m "環境變數配置 - 準備部署 Zeabur"

# 推送
git remote add origin https://github.com/你的用戶名/booking-system.git
git push -u origin main
```

### 階段 3：在 Zeabur 創建專案

1. 訪問 https://zeabur.com
2. 點擊 "Create Project"
3. 選擇 "Deploy from GitHub"
4. 選擇你的倉庫

### 階段 4：添加 MySQL 服務

Zeabur 會自動偵測並建議添加 MySQL

### 階段 5：設定環境變數

複製以下內容到 Zeabur Variables：

```bash
# 郵件設定
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

### 階段 6：部署

點擊 "Deploy" 或 "Redeploy"

---

## 🔍 環境變數運作原理

### 本地開發時

```
application.properties 內容:
  password=${DB_PASSWORD:2FTA93108}
              ↓
  檢查環境變數 DB_PASSWORD
              ↓
  沒有找到環境變數
              ↓
  使用預設值: 2FTA93108 ✅
```

### Zeabur 部署時

```
application.properties 內容:
  password=${DB_PASSWORD:2FTA93108}
              ↓
  檢查環境變數 DB_PASSWORD
              ↓
  找到環境變數 = xyz123 (Zeabur 設定的)
              ↓
  使用環境變數: xyz123 ✅
```

---

## 📊 安全性對照

### GitHub 上的代碼（公開可見）

```properties
# ✅ 任何人看到的：
spring.datasource.password=${DB_PASSWORD:default}
spring.mail.password=${MAIL_PASSWORD:default}
```

**安全性：**
- ✅ 沒有真實密碼
- ✅ 只有佔位符
- ✅ 無法直接使用

### Zeabur 上的環境變數（私密）

```
DB_PASSWORD=2FTA93108         (實際資料庫密碼)
MAIL_PASSWORD=rgsqpqcanthwqars  (實際郵件密碼)
```

**安全性：**
- ✅ 只有你能看到
- ✅ 加密儲存
- ✅ 不會出現在代碼中

---

## ✅ 檢查清單

### 部署前

- [ ] 已使用 `替換配置文件.bat` 或手動替換
- [ ] `application.properties` 使用環境變數語法
- [ ] 原始配置已備份
- [ ] 已閱讀 `ZEABUR_QUICK_START.md`
- [ ] 已準備好 Gmail 應用程式密碼

### GitHub 上傳

- [ ] 已初始化 Git
- [ ] 已添加所有文件
- [ ] 確認沒有真實密碼在代碼中
- [ ] 已推送到 GitHub

### Zeabur 部署

- [ ] 已創建 Zeabur 專案
- [ ] 已連接 GitHub 倉庫
- [ ] 已添加 MySQL 服務
- [ ] 已設定所有環境變數
- [ ] 已點擊部署

### 驗證

- [ ] 應用程式成功啟動
- [ ] 可以訪問首頁
- [ ] 資料庫連接正常
- [ ] 郵件功能正常
- [ ] 所有功能測試通過

---

## 🎉 成功指標

部署成功後，你應該能：

1. ✅ 訪問 `https://你的專案名稱.zeabur.app`
2. ✅ 看到首頁正常顯示
3. ✅ 使用 admin/admin123 登入
4. ✅ 瀏覽所有住宿資料
5. ✅ 測試忘記密碼功能（收到郵件）
6. ✅ 創建新訂單
7. ✅ 所有 API 正常運作

---

## 💡 常見問題

### Q1：為什麼預設值還是真實密碼？
```properties
password=${DB_PASSWORD:2FTA93108}
                        ↑ 這裡還是真實的
```

**A：** 兩個選擇：
- **方便開發**：保留真實密碼（只是本地 MySQL 密碼）
- **完全安全**：改為假的密碼，本地也用環境變數

### Q2：Zeabur 怎麼知道要讀環境變數？

**A：** Spring Boot 自動支援！
- 不需要修改 Java 代碼
- 不需要額外配置
- Spring Boot 啟動時自動讀取

### Q3：本地開發怎麼辦？

**A：** 三種方式：
1. **使用預設值**（最簡單）
   - 配置：`${VAR:預設值}`
   - 直接 `mvn spring-boot:run`
   
2. **設定環境變數**
   ```cmd
   set DB_PASSWORD=123
   mvn spring-boot:run
   ```

3. **使用本地配置**
   - 創建 `application-local.properties`
   - 啟動：`mvn spring-boot:run -Dspring-boot.run.profiles=local`

### Q4：要上傳哪些文件？

**✅ 上傳：**
- `application.properties` (使用環境變數)
- `application.properties.example`
- `pom.xml`
- 所有 `.java` 文件
- 所有 `.html` 文件
- `README.md` 等文件

**❌ 不上傳：**
- `application.properties.backup` (含真實密碼)
- `application-local.properties` (本地配置)
- `target/` (編譯輸出)
- `data/` (資料庫文件)

---

## 🎯 下一步行動

### 立即執行

1. **運行替換腳本**
   ```cmd
   cd C:\my-booking-app-practice\booking
   替換配置文件.bat
   ```

2. **檢查配置文件**
   ```cmd
   notepad src\main\resources\application.properties
   ```
   確認看到 `${DB_PASSWORD:...}` 語法

3. **推送到 GitHub**
   ```bash
   cd C:\my-booking-app-practice
   git init
   git add .
   git commit -m "環境變數配置"
   git push
   ```

4. **部署到 Zeabur**
   - 登入 https://zeabur.com
   - 創建專案
   - 設定環境變數
   - 開始部署！

### 詳細教學

請參考：
- ⭐ `ZEABUR_QUICK_START.md` - 快速上手
- 📖 `ZEABUR_DEPLOYMENT_GUIDE.md` - 詳細教學

---

**祝你部署順利！** 🚀

有任何問題可以查看文檔或檢查 Zeabur 日誌。

