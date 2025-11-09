# 🔐 環境配置指南

本專案包含敏感資訊（資料庫密碼、郵件帳號等），這些資訊**不應該**上傳到 GitHub。

## 📋 配置步驟

### 1. 複製範例配置文件

```bash
cd booking/src/main/resources
cp application.properties.example application.properties
```

### 2. 編輯 `application.properties`

使用文字編輯器打開 `application.properties`，並填入你的實際配置：

#### 資料庫設定

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/booking_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Taipei
spring.datasource.username=你的MySQL用戶名
spring.datasource.password=你的MySQL密碼
```

**說明：**
- 請先在 MySQL 中創建資料庫：`CREATE DATABASE booking_db;`
- 用戶名通常是 `root`
- 密碼是你安裝 MySQL 時設定的密碼

#### 郵件設定（Gmail）

```properties
spring.mail.username=你的Gmail帳號@gmail.com
spring.mail.password=你的Gmail應用程式密碼
app.mail.from=你的Gmail帳號@gmail.com
```

**如何取得 Gmail 應用程式密碼：**

1. 登入 Google 帳戶
2. 前往 [Google 帳戶安全性設定](https://myaccount.google.com/security)
3. 啟用「兩步驟驗證」
4. 搜尋「應用程式密碼」
5. 選擇「郵件」和「Windows 電腦」
6. 複製產生的 16 位密碼（格式：`xxxx xxxx xxxx xxxx`）
7. 移除空格後填入配置文件

**注意：** 
- 應用程式密碼不是你的 Gmail 登入密碼
- 應用程式密碼只會顯示一次，請妥善保存

### 3. 驗證配置

啟動應用程式：

```bash
mvn spring-boot:run
```

檢查是否有以下訊息：
- ✅ 資料庫連接成功
- ✅ 應用程式正常啟動
- ✅ 訪問 http://localhost:8080 可以看到首頁

## 🔒 安全注意事項

### ⚠️ 絕對不要做的事

- ❌ **不要**把 `application.properties` 上傳到 GitHub
- ❌ **不要**在公開場合分享你的資料庫密碼
- ❌ **不要**在截圖中包含敏感資訊
- ❌ **不要**將密碼寫在程式碼或註解中

### ✅ 應該做的事

- ✅ 使用 `application.properties.example` 作為範本
- ✅ 將 `application.properties` 加入 `.gitignore`
- ✅ 使用強密碼
- ✅ 定期更換密碼
- ✅ 不同專案使用不同的資料庫密碼

## 📝 配置文件說明

| 文件名 | 用途 | 是否上傳 GitHub |
|--------|------|-----------------|
| `application.properties` | 實際配置（含敏感資訊） | ❌ 不上傳 |
| `application.properties.example` | 配置範本（不含敏感資訊） | ✅ 上傳 |

## 🐛 常見問題

### Q: 忘記填寫配置導致啟動失敗？

**錯誤訊息：**
```
Access denied for user 'your_mysql_username'@'localhost'
```

**解決方法：**
檢查 `application.properties` 中的資料庫帳號密碼是否正確。

### Q: 郵件發送失敗？

**錯誤訊息：**
```
AuthenticationFailedException: 535-5.7.8 Username and Password not accepted
```

**解決方法：**
1. 確認使用的是「應用程式密碼」，不是 Gmail 登入密碼
2. 檢查是否已啟用「兩步驟驗證」
3. 重新產生應用程式密碼

### Q: 資料庫連接失敗？

**錯誤訊息：**
```
Communications link failure
```

**解決方法：**
1. 確認 MySQL 服務已啟動
2. 檢查 `application.properties` 中的資料庫 URL
3. 確認資料庫 `booking_db` 已創建

## 🔄 環境變數方式（進階）

如果你熟悉環境變數，也可以使用這種方式：

### Windows

```cmd
set DB_USERNAME=root
set DB_PASSWORD=your_password
set MAIL_USERNAME=your_email@gmail.com
set MAIL_PASSWORD=your_app_password
```

### Linux / macOS

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
export MAIL_USERNAME=your_email@gmail.com
export MAIL_PASSWORD=your_app_password
```

然後修改 `application.properties`：

```properties
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
```

## 📚 相關資源

- [Spring Boot Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)
- [Gmail App Passwords](https://support.google.com/accounts/answer/185833)
- [MySQL Documentation](https://dev.mysql.com/doc/)

---

**需要協助？** 請檢查應用程式啟動日誌或查看專案 README.md 的故障排除章節。

