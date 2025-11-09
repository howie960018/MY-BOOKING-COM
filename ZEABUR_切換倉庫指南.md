# 🔄 Zeabur 切換到新 GitHub 倉庫指南

## ✅ 當前狀態

- ✅ 新 GitHub 倉庫：https://github.com/howie960018/MY-BOOKING-COM.git
- ✅ 代碼已推送（無敏感資訊）
- ✅ 環境變數已修正
- 📋 現在需要：讓 Zeabur 連接到新倉庫

---

## 🎯 兩種方法

### 方法 1：更新現有服務的 Git 連接（推薦）⭐

如果你想保留現有的環境變數和資料庫連接：

#### 步驟：

1. **登入 Zeabur**
   - 訪問：https://zeabur.com

2. **選擇專案**
   - 找到你的訂房系統專案

3. **進入服務設定**
   - 點擊你的應用程式服務
   - 選擇 **Settings** 標籤

4. **更新 Git 倉庫**
   - 找到 **Source** 或 **Repository** 區域
   - 點擊 **Change Repository** 或 **Edit**
   - 選擇新倉庫：`howie960018/MY-BOOKING-COM`
   - 分支：`main`

5. **觸發重新部署**
   - 點擊 **Redeploy** 按鈕
   - 或等待自動部署

6. **完成！**
   - Zeabur 會從新倉庫拉取代碼
   - 使用現有的環境變數
   - 連接現有的資料庫

---

### 方法 2：刪除舊服務，創建新服務

如果方法 1 不可用，或想重新開始：

#### 步驟：

1. **記錄環境變數**
   - 在刪除前，複製所有環境變數到記事本
   - 或參考 `ZEABUR_環境變數設定指南.md`

2. **記錄資料庫連接資訊**
   - MySQL 主機、端口、密碼等

3. **刪除舊服務**
   - 選擇舊的應用程式服務
   - Settings → Delete Service
   - **注意：不要刪除 MySQL 服務！**

4. **創建新服務**
   - 點擊 **Add Service**
   - 選擇 **Deploy from GitHub**
   - 選擇倉庫：`howie960018/MY-BOOKING-COM`
   - 分支：`main`

5. **設定環境變數**
   - 按照之前記錄的環境變數設定
   - 或參考下方的完整清單

6. **連接 MySQL**
   - 確保新服務和 MySQL 在同一個專案中
   - Zeabur 會自動連接

7. **部署**
   - Zeabur 自動開始部署

---

## 📋 完整環境變數清單（方法 2 需要）

如果選擇方法 2，需要重新設定這些環境變數：

```bash
# 資料庫設定
DATABASE_URL=jdbc:mysql://hkg1.clusters.zeabur.com:31426/booking_db?serverTimezone=Asia/Taipei&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=JzXcLTld6yH7Ora34gKnE5uo90t8U21w

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
PORT=${WEB_PORT}
```

**注意：** 
- `DATABASE_URL` 中的主機和端口可能不同，請使用 Zeabur MySQL 服務提供的實際值
- `APP_BASE_URL` 需要替換為你的實際域名

---

## 🔍 如何在 Zeabur 找到相關設定

### 找到 Git 倉庫設定

1. 登入 Zeabur → 選擇專案
2. 點擊應用程式服務
3. 查看上方會顯示當前連接的 GitHub 倉庫
4. 點擊 **Settings** 標籤
5. 找到 **Source** 或 **Repository** 區域

### 找到環境變數

1. 點擊應用程式服務
2. 選擇 **Variables** 標籤
3. 可以看到所有已設定的環境變數

### 找到 MySQL 連接資訊

1. 在專案中找到 MySQL 服務
2. 點擊 MySQL 服務
3. 選擇 **Instructions** 或 **Connect** 標籤
4. 可以看到：
   - Host（主機）
   - Port（端口）
   - Username（用戶名）
   - Password（密碼）
   - Database（資料庫名稱）

---

## 🚀 推薦流程（方法 1 詳細步驟）

### 1. 登入 Zeabur

訪問：https://zeabur.com

輸入帳號密碼登入

### 2. 找到專案

在 Dashboard 中，找到你的訂房系統專案

### 3. 選擇服務

點擊應用程式服務（可能顯示為服務名稱或倉庫名稱）

### 4. 查看當前設定

- 上方應該會顯示當前連接的 Git 倉庫
- 可能是舊的倉庫名稱或已刪除的倉庫

### 5. 更改倉庫連接

**可能的操作位置：**

#### 選項 A：在服務頂部
- 點擊倉庫名稱旁的編輯圖示
- 選擇新倉庫

#### 選項 B：在 Settings 標籤
- 點擊 **Settings** 標籤
- 找到 **Source** 或 **Git Repository** 區域
- 點擊 **Change** 或 **Edit**
- 選擇 `howie960018/MY-BOOKING-COM`

#### 選項 C：重新連接 GitHub
- Settings → **Reconnect GitHub**
- 重新授權 GitHub
- 選擇新倉庫

### 6. 設定分支

確認分支選擇為：**main**

### 7. 觸發部署

- 點擊 **Deploy** 或 **Redeploy** 按鈕
- 或者：推送新代碼到 GitHub 會自動觸發部署

### 8. 查看部署日誌

- 點擊 **Logs** 標籤
- 查看部署進度
- 確認沒有錯誤

---

## ✅ 部署成功標誌

### 在 Logs 中應該看到：

```
Cloning from https://github.com/howie960018/MY-BOOKING-COM.git
Branch: main
...
BUILD SUCCESS
...
Started BookingApplication in X.XXX seconds
Tomcat started on port(s): XXXX (http)
```

### 訪問應用程式

```
https://你的專案名稱.zeabur.app
```

**預期結果：**
- ✅ 首頁正常顯示
- ✅ 住宿列表載入
- ✅ 可以登入（admin/admin123）
- ✅ 所有功能正常

---

## ❌ 可能遇到的問題

### 問題 1：找不到更改倉庫的選項

**原因：** Zeabur UI 可能更新了

**解決方法：**
1. 嘗試刪除服務並重新創建（方法 2）
2. 或聯繫 Zeabur 支援

### 問題 2：環境變數消失

**原因：** 可能在切換時被清除

**解決方法：**
- 重新設定環境變數
- 參考 `ZEABUR_環境變數設定指南.md`

### 問題 3：資料庫連接失敗

**錯誤訊息：**
```
Communications link failure
```

**原因：** DATABASE_URL 不正確

**解決方法：**
1. 檢查 MySQL 服務是否在同一專案
2. 確認 DATABASE_URL 格式正確
3. 使用 Zeabur 提供的實際連接資訊

### 問題 4：應用程式無法啟動

**原因：** 環境變數名稱不正確

**解決方法：**
- 確認使用 `DATABASE_URL`、`DB_USERNAME`、`DB_PASSWORD`
- 而不是 `SPRING_DATASOURCE_*`

---

## 🎯 快速檢查清單

切換倉庫後，確認：

- [ ] Zeabur 服務顯示新倉庫：`howie960018/MY-BOOKING-COM`
- [ ] 分支設定為：`main`
- [ ] 環境變數都正確（13 個變數）
- [ ] MySQL 服務正常運行
- [ ] 部署日誌沒有錯誤
- [ ] 可以訪問應用程式
- [ ] 首頁正常顯示
- [ ] 可以登入
- [ ] 功能測試通過

---

## 📞 需要協助？

### Zeabur 官方資源

- 文檔：https://zeabur.com/docs
- Discord：https://discord.gg/zeabur
- GitHub：https://github.com/zeabur

### 常見操作

**查看部署日誌：**
- 服務頁面 → Logs 標籤

**重新部署：**
- 服務頁面 → Deploy/Redeploy 按鈕

**環境變數管理：**
- 服務頁面 → Variables 標籤

**MySQL 連接資訊：**
- MySQL 服務 → Instructions 標籤

---

## 🎉 完成！

切換成功後：

1. ✅ **Zeabur 連接新倉庫**
2. ✅ **環境變數正確**
3. ✅ **資料庫連接正常**
4. ✅ **應用程式運行中**

**未來更新：**
- 本地修改代碼
- 推送到 GitHub
- Zeabur 自動部署
- 完成！

---

**現在去 Zeabur 切換倉庫吧！** 🚀

推薦使用 **方法 1**（更新現有服務），更快更簡單！

