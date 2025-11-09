# ☁️ Zeabur 部署指南

本指南將幫助你將訂房系統部署到 Zeabur 雲端平台。

---

## 📋 前置需求

- GitHub 帳號
- Zeabur 帳號（使用 GitHub 登入）
- Gmail 帳號（用於發送郵件）

---

## 🚀 部署步驟

### 1. 準備 GitHub 倉庫

確保你的代碼已推送到 GitHub：

```bash
git add .
git commit -m "準備部署"
git push
```

---

### 2. 登入 Zeabur

1. 訪問：https://zeabur.com
2. 點擊 "Sign in with GitHub"
3. 授權 Zeabur 訪問你的 GitHub

---

### 3. 創建專案

1. 點擊 "**Create Project**"
2. 輸入專案名稱（例如：`booking-system`）
3. 選擇地區（建議：Hong Kong）
4. 點擊 "Create"

---

### 4. 添加 MySQL 服務

1. 在專案中點擊 "**Add Service**"
2. 選擇 "**Prebuilt**"
3. 選擇 "**MySQL**"
4. 等待 MySQL 服務啟動（約 1 分鐘）

---

### 5. 部署應用程式

1. 點擊 "**Add Service**"
2. 選擇 "**Deploy from GitHub**"
3. 選擇你的倉庫：`MY-BOOKING-COM`
4. 選擇分支：`main`
5. Zeabur 會自動檢測 `pom.xml` 並使用 Java 構建
6. 等待部署完成（約 2-3 分鐘）

---

### 6. 配置環境變數

在應用程式服務頁面，點擊 "**Variables**" 標籤：

#### 📌 必要的環境變數

```bash
# === 資料庫配置 ===
# Zeabur MySQL 會自動提供這些變數，直接引用即可
DATABASE_URL=${MYSQL_URL}
DB_USERNAME=${MYSQL_USERNAME}  
DB_PASSWORD=${MYSQL_PASSWORD}

# === 郵件配置 ===
# 需要手動添加 Gmail 設定
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password

# === 應用程式配置 ===
# 替換為你的實際 Zeabur 網址
APP_BASE_URL=https://your-project.zeabur.app
DDL_AUTO=update
SHOW_SQL=false
```

#### 如何添加變數

1. 點擊 "**Add Variable**"
2. 輸入 **Name**（變數名稱）
3. 輸入 **Value**（變數值）
4. 點擊 "**Save**"
5. 重複以上步驟添加所有變數

---

### 7. 取得 Gmail 應用程式密碼

#### 步驟

1. **啟用兩步驟驗證**
   - 訪問：https://myaccount.google.com/security
   - 啟用「兩步驟驗證」

2. **產生應用程式密碼**
   - 搜尋「應用程式密碼」
   - 選擇「郵件」→「其他裝置」
   - 輸入名稱：「訂房系統」
   - 點擊「產生」

3. **複製密碼**
   - 複製 16 位密碼（例如：`abcd efgh ijkl mnop`）
   - 在 `MAIL_PASSWORD` 中使用此密碼

---

### 8. 重新部署

添加環境變數後：

1. 點擊 "**Redeploy**" 按鈕
2. 等待部署完成（約 2 分鐘）

---

### 9. 訪問應用程式

1. 在服務頁面找到 "**Domains**"
2. 複製 Zeabur 提供的網址（例如：`https://xxx.zeabur.app`）
3. 在瀏覽器中訪問

---

## ✅ 驗證部署

### 測試清單

- [ ] 首頁可以正常訪問
- [ ] 可以查看住宿列表
- [ ] 可以使用測試帳號登入（`admin` / `password`）
- [ ] 可以創建訂單
- [ ] 郵件功能正常（測試忘記密碼）

---

## 📊 環境變數完整列表

| 變數名稱 | 說明 | 範例值 | 必填 |
|---------|------|--------|------|
| `DATABASE_URL` | MySQL 連接 URL | `${MYSQL_URL}` | ✅ |
| `DB_USERNAME` | 資料庫用戶名 | `${MYSQL_USERNAME}` | ✅ |
| `DB_PASSWORD` | 資料庫密碼 | `${MYSQL_PASSWORD}` | ✅ |
| `MAIL_USERNAME` | Gmail 帳號 | `your@gmail.com` | ✅ |
| `MAIL_PASSWORD` | Gmail 應用程式密碼 | `abcd efgh ijkl mnop` | ✅ |
| `APP_BASE_URL` | 應用程式網址 | `https://xxx.zeabur.app` | ✅ |
| `DDL_AUTO` | Hibernate DDL 模式 | `update` | ✅ |
| `SHOW_SQL` | 是否顯示 SQL | `false` | ⭕ |
| `MAIL_HOST` | SMTP 主機 | `smtp.gmail.com` | ⭕ |
| `MAIL_PORT` | SMTP 端口 | `587` | ⭕ |

**說明：**
- ✅ = 必填
- ⭕ = 選填（有預設值）
- `${MYSQL_URL}` = 引用 Zeabur MySQL 服務提供的變數

---

## 🔧 常見問題

### 1. 應用程式無法啟動

**可能原因：**
- 環境變數設定錯誤
- 資料庫連接失敗

**解決方法：**
1. 檢查 "Logs" 標籤中的錯誤訊息
2. 確認所有必要的環境變數都已設定
3. 確認 MySQL 服務正在運行

---

### 2. 資料庫連接失敗

**錯誤訊息：** `Communications link failure`

**解決方法：**
1. 確認 MySQL 服務已啟動
2. 確認 `DATABASE_URL` 使用 `${MYSQL_URL}`
3. 確認應用程式和 MySQL 在同一個專案中

---

### 3. 郵件發送失敗

**錯誤訊息：** `Authentication failed`

**解決方法：**
1. 確認使用的是 **Gmail 應用程式密碼**，不是帳號密碼
2. 確認 Gmail 已啟用兩步驟驗證
3. 重新產生應用程式密碼並更新環境變數

---

### 4. 如何查看日誌

1. 進入應用程式服務頁面
2. 點擊 "**Logs**" 標籤
3. 查看實時日誌輸出

---

### 5. 如何重新部署

1. 修改代碼後推送到 GitHub
2. Zeabur 會**自動檢測**並重新部署
3. 或手動點擊 "**Redeploy**" 按鈕

---

## 🎯 部署後設定

### 1. 設定自訂域名（選填）

1. 在 Zeabur 購買域名或使用現有域名
2. 在 "Domains" 標籤中添加域名
3. 更新 `APP_BASE_URL` 環境變數

### 2. 啟用 HTTPS

Zeabur 自動為所有應用程式啟用 HTTPS，無需額外設定。

### 3. 監控與日誌

定期檢查：
- "Logs" - 應用程式日誌
- "Metrics" - 性能指標
- "Environment Variables" - 環境變數

---

## 📝 部署檢查清單

- [ ] GitHub 倉庫已創建並推送代碼
- [ ] Zeabur 專案已創建
- [ ] MySQL 服務已添加並啟動
- [ ] 應用程式服務已部署
- [ ] 所有必要的環境變數已設定
- [ ] Gmail 應用程式密碼已取得
- [ ] `APP_BASE_URL` 已更新為實際網址
- [ ] 應用程式已重新部署
- [ ] 可以正常訪問首頁
- [ ] 測試帳號可以登入
- [ ] 郵件功能測試通過

---

## 🆘 需要幫助？

### Zeabur 官方資源
- **文件**: https://zeabur.com/docs
- **Discord**: https://discord.gg/zeabur
- **GitHub**: https://github.com/zeabur/zeabur

### 專案相關
- **GitHub Issues**: https://github.com/howie960018/MY-BOOKING-COM/issues
- **README**: 查看專案 README.md

---

## 🎉 部署成功！

恭喜你成功將訂房系統部署到 Zeabur！

現在你可以：
- ✅ 分享網址給其他人使用
- ✅ 繼續開發新功能
- ✅ 推送代碼自動部署

**專案網址：** `https://your-project.zeabur.app`

---

**最後更新**: 2025-11-09

