# ✅ Zeabur 環境變數設定 - 最終版本

## 🎯 正確的環境變數配置

請在 Zeabur 控制台的 **Variables** 標籤中設定以下環境變數：

---

### 📊 完整環境變數列表

| 變數名稱 | 值 | 說明 |
|---------|-----|------|
| `DATABASE_URL` | `jdbc:mysql://hkg1.clusters.zeabur.com:31426/booking_db?serverTimezone=Asia/Taipei&useSSL=false&allowPublicKeyRetrieval=true` | MySQL 連接 URL |
| `DB_USERNAME` | `root` | MySQL 用戶名 |
| `DB_PASSWORD` | `JzXcLTld6yH7Ora34gKnE5uo90t8U21w` | MySQL 密碼 |
| `MAIL_HOST` | `smtp.gmail.com` | Gmail SMTP 主機 |
| `MAIL_PORT` | `587` | SMTP 端口 |
| `MAIL_USERNAME` | `howie960018@gmail.com` | Gmail 帳號 |
| `MAIL_PASSWORD` | `rgsqpqcanthwqars` | Gmail 應用程式密碼 |
| `MAIL_FROM` | `howie960018@gmail.com` | 寄件者郵箱 |
| `MAIL_FROM_NAME` | `訂房系統` | 寄件者名稱 |
| `APP_BASE_URL` | `https://你的專案名稱.zeabur.app` | 應用程式網址 |
| `DDL_AUTO` | `update` | Hibernate DDL 模式 |
| `SHOW_SQL` | `false` | 是否顯示 SQL |
| `PORT` | `${WEB_PORT}` | 服務端口（Zeabur 自動） |

---

## 🔧 設定步驟

### 1. 登入 Zeabur
訪問：https://zeabur.com

### 2. 選擇專案
找到你的訂房系統專案

### 3. 進入環境變數設定
- 點擊你的服務
- 選擇 **Variables** 標籤

### 4. 逐一添加環境變數

**複製貼上格式（每行一個）：**

```
DATABASE_URL=jdbc:mysql://hkg1.clusters.zeabur.com:31426/booking_db?serverTimezone=Asia/Taipei&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=JzXcLTld6yH7Ora34gKnE5uo90t8U21w
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=howie960018@gmail.com
MAIL_PASSWORD=rgsqpqcanthwqars
MAIL_FROM=howie960018@gmail.com
MAIL_FROM_NAME=訂房系統
APP_BASE_URL=https://你的專案名稱.zeabur.app
DDL_AUTO=update
SHOW_SQL=false
PORT=${WEB_PORT}
```

**注意：** 請將 `https://你的專案名稱.zeabur.app` 替換為你實際的 Zeabur 網址！

### 5. 刪除錯誤的變數（如果存在）

❌ 刪除以下變數：
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `PASSWORD`（不需要的變數）

### 6. 儲存並重新部署

點擊 **Redeploy** 或等待自動部署

---

## 🔍 為什麼你的設定有問題？

### ❌ 錯誤設定：
```
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=JzXcLTld6yH7Ora34gKnE5uo90t8U21w
SPRING_DATASOURCE_URL=jdbc:mysql://...
```

### ✅ 正確設定：
```
DB_USERNAME=root
DB_PASSWORD=JzXcLTld6yH7Ora34gKnE5uo90t8U21w
DATABASE_URL=jdbc:mysql://...
```

**原因：**
你的 `application.properties` 使用的是：
```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

所以環境變數名稱必須是 `DATABASE_URL`、`DB_USERNAME`、`DB_PASSWORD`，而不是 `SPRING_DATASOURCE_XXX`。

---

## 📊 變數對應關係

| application.properties | 環境變數名稱 | Zeabur 設定值 |
|----------------------|-------------|--------------|
| `spring.datasource.url=${DATABASE_URL}` | `DATABASE_URL` | `jdbc:mysql://hkg1.clusters.zeabur.com:31426/booking_db?...` |
| `spring.datasource.username=${DB_USERNAME}` | `DB_USERNAME` | `root` |
| `spring.datasource.password=${DB_PASSWORD}` | `DB_PASSWORD` | `JzXcLTld6yH7Ora34gKnE5uo90t8U21w` |
| `spring.mail.host=${MAIL_HOST:smtp.gmail.com}` | `MAIL_HOST` | `smtp.gmail.com` |
| `spring.mail.username=${MAIL_USERNAME}` | `MAIL_USERNAME` | `howie960018@gmail.com` |
| `spring.mail.password=${MAIL_PASSWORD}` | `MAIL_PASSWORD` | `rgsqpqcanthwqars` |

---

## ✅ 設定完成檢查清單

- [ ] 已刪除 `SPRING_DATASOURCE_*` 變數
- [ ] 已刪除 `PASSWORD` 變數
- [ ] 已添加 `DATABASE_URL`
- [ ] 已添加 `DB_USERNAME`
- [ ] 已添加 `DB_PASSWORD`
- [ ] 已添加所有 `MAIL_*` 變數
- [ ] 已添加 `APP_BASE_URL`（並替換為實際網址）
- [ ] 已添加 `DDL_AUTO=update`
- [ ] 已添加 `SHOW_SQL=false`
- [ ] 已添加 `PORT=${WEB_PORT}`
- [ ] 已點擊 Redeploy

---

## 🚀 部署後驗證

### 1. 查看部署日誌

在 Zeabur 控制台查看 **Logs** 標籤，應該看到：

```
Started BookingApplication in X.XXX seconds
Tomcat started on port(s): XXXX (http)
```

### 2. 測試應用程式

訪問你的 Zeabur 網址：
```
https://你的專案名稱.zeabur.app
```

**預期結果：**
- ✅ 看到首頁
- ✅ 看到住宿列表
- ✅ 可以登入

### 3. 測試資料庫連接

嘗試登入：
- 用戶名：`admin`
- 密碼：`admin123`

如果能成功登入，代表資料庫連接正常！

### 4. 測試郵件功能（可選）

嘗試「忘記密碼」功能，看是否能收到郵件。

---

## ❌ 常見錯誤

### 錯誤 1：應用程式無法啟動

**日誌顯示：**
```
Error creating bean with name 'dataSource'
```

**原因：** 環境變數名稱不正確

**解決：** 確認使用 `DATABASE_URL`、`DB_USERNAME`、`DB_PASSWORD`

### 錯誤 2：資料庫連接失敗

**日誌顯示：**
```
Communications link failure
```

**原因：** DATABASE_URL 不正確或缺少參數

**解決：** 使用完整的 URL：
```
jdbc:mysql://hkg1.clusters.zeabur.com:31426/booking_db?serverTimezone=Asia/Taipei&useSSL=false&allowPublicKeyRetrieval=true
```

### 錯誤 3：郵件發送失敗

**原因：** `MAIL_PASSWORD` 不正確或缺失

**解決：** 確認 Gmail 應用程式密碼正確

---

## 💡 重要提醒

### DATABASE_URL 必須包含的參數

```
jdbc:mysql://主機:端口/資料庫名稱?參數1&參數2&參數3
                                    ↑
                                   必須有
```

**必要參數：**
- `serverTimezone=Asia/Taipei` - 時區設定
- `useSSL=false` - 關閉 SSL（Zeabur 內部連接）
- `allowPublicKeyRetrieval=true` - 允許公鑰檢索

### APP_BASE_URL 的重要性

這個 URL 用於：
- 生成密碼重置連結
- 郵件中的連結
- 其他需要完整網址的功能

**必須設定為你的實際 Zeabur 網址！**

---

## 🎯 快速修正指令

在 Zeabur Variables 頁面，一次性貼上（記得修改 APP_BASE_URL）：

```bash
DATABASE_URL=jdbc:mysql://hkg1.clusters.zeabur.com:31426/booking_db?serverTimezone=Asia/Taipei&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=JzXcLTld6yH7Ora34gKnE5uo90t8U21w
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=howie960018@gmail.com
MAIL_PASSWORD=rgsqpqcanthwqars
MAIL_FROM=howie960018@gmail.com
MAIL_FROM_NAME=訂房系統
APP_BASE_URL=https://你的專案名稱.zeabur.app
DDL_AUTO=update
SHOW_SQL=false
PORT=${WEB_PORT}
```

---

## 📞 需要幫助？

如果設定後仍有問題：

1. **查看 Zeabur Logs**
   - 在服務頁面點擊 "Logs"
   - 查看錯誤訊息

2. **常見問題**
   - 資料庫連接失敗 → 檢查 DATABASE_URL
   - 應用程式無法啟動 → 檢查環境變數名稱
   - 郵件發送失敗 → 檢查 MAIL_PASSWORD

3. **重新部署**
   - 修改環境變數後
   - 務必點擊 "Redeploy"

---

**祝你部署成功！** 🎊

