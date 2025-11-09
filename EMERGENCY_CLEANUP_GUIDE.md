# 🚨 緊急：從 GitHub 刪除敏感資訊

## ⚠️ 情況說明

你的 `application.properties` 文件（包含資料庫密碼和 Gmail 密碼）已經被推送到 GitHub。

**發現的提交記錄：**
- 至少 13 次提交包含此文件
- 最早提交：`1ae626c`
- 最新提交：`e799604`

## 🔐 必須立即執行的步驟

### 步驟 1：更改所有洩露的密碼（最重要！）

即使刪除 Git 歷史，已經洩露的密碼仍然不安全。**必須立即更改：**

#### A. MySQL 資料庫密碼
```sql
-- 連接到 MySQL
mysql -u root -p

-- 更改密碼
ALTER USER 'root'@'localhost' IDENTIFIED BY '新的強密碼';
FLUSH PRIVILEGES;
```

#### B. Gmail 應用程式密碼
1. 登入 Google 帳戶：https://myaccount.google.com/security
2. 找到「應用程式密碼」
3. **撤銷舊的應用程式密碼**
4. 產生新的應用程式密碼
5. 更新到新的配置文件

### 步驟 2：從 Git 歷史中刪除敏感文件

我提供三種方法，由簡到難：

---

## 方法 1：完全重置倉庫（最簡單、最安全）⭐ 推薦

### 優點
- ✅ 最簡單、最徹底
- ✅ 保證沒有任何殘留
- ✅ 不需要複雜工具

### 缺點
- ⚠️ 會失去所有 Git 提交歷史
- ⚠️ 需要重新初始化

### 執行步驟

#### 1. 備份當前代碼
```cmd
cd C:\
xcopy C:\my-booking-app-practice C:\my-booking-app-practice-backup /E /I /H
```

#### 2. 刪除 .git 目錄
```cmd
cd C:\my-booking-app-practice
rmdir /S /Q .git
```

#### 3. 替換為環境變數版本
```cmd
cd booking\src\main\resources
del application.properties
copy application.properties.new application.properties
```

#### 4. 重新初始化 Git
```cmd
cd C:\my-booking-app-practice
git init
git add .
git commit -m "Initial commit: 訂房系統（已移除敏感資訊）"
```

#### 5. 刪除 GitHub 上的舊倉庫
1. 訪問 GitHub 倉庫頁面
2. Settings → Danger Zone → Delete this repository
3. 輸入倉庫名稱確認刪除

#### 6. 創建新倉庫並推送
```cmd
# 在 GitHub 創建新倉庫後
git remote add origin https://github.com/你的用戶名/新倉庫名.git
git branch -M main
git push -u origin main
```

---

## 方法 2：使用 BFG Repo-Cleaner（保留歷史）

### 優點
- ✅ 保留所有提交歷史
- ✅ 只刪除敏感文件

### 缺點
- ⚠️ 需要下載額外工具
- ⚠️ 操作較複雜

### 執行步驟

#### 1. 下載 BFG Repo-Cleaner
訪問：https://rtyley.github.io/bfg-repo-cleaner/
下載：bfg-1.14.0.jar

#### 2. 創建要刪除的文件清單
```cmd
cd C:\my-booking-app-practice
echo application.properties > files-to-delete.txt
```

#### 3. 運行 BFG
```cmd
# 確保 Java 已安裝
java -version

# 運行 BFG 刪除文件
java -jar bfg-1.14.0.jar --delete-files application.properties .git
```

#### 4. 清理 Git 歷史
```cmd
cd C:\my-booking-app-practice
git reflog expire --expire=now --all
git gc --prune=now --aggressive
```

#### 5. 添加新的配置文件
```cmd
cd booking\src\main\resources
copy application.properties.new application.properties
git add application.properties
git commit -m "使用環境變數替代硬編碼密碼"
```

#### 6. 強制推送
```cmd
git push --force origin master
```

---

## 方法 3：使用 git filter-branch（手動）

### 優點
- ✅ 不需要額外工具
- ✅ Git 內建命令

### 缺點
- ⚠️ 命令複雜
- ⚠️ 容易出錯

### 執行步驟

```cmd
cd C:\my-booking-app-practice

# 從所有提交中刪除文件
git filter-branch --force --index-filter "git rm --cached --ignore-unmatch booking/src/main/resources/application.properties" --prune-empty --tag-name-filter cat -- --all

# 清理
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# 添加新文件
cd booking\src\main\resources
copy application.properties.new application.properties
git add application.properties
git commit -m "使用環境變數配置"

# 強制推送
git push --force origin master
```

---

## 🎯 推薦執行方案

我強烈推薦使用 **方法 1：完全重置倉庫**

### 理由
1. ✅ 最簡單、最安全
2. ✅ 100% 保證沒有殘留
3. ✅ 不需要複雜工具
4. ✅ 提交歷史對於學習專案不重要

### 完整執行腳本

我已經為你準備了自動化腳本，請看下一個文件：
`EMERGENCY_CLEANUP.bat`

---

## ✅ 清理完成後的檢查清單

- [ ] 已更改 MySQL 密碼
- [ ] 已撤銷舊的 Gmail 應用程式密碼
- [ ] 已產生新的 Gmail 應用程式密碼
- [ ] 已從 Git 歷史刪除 application.properties
- [ ] 已替換為環境變數版本
- [ ] 已推送到 GitHub
- [ ] 在 GitHub 上確認文件內容正確
- [ ] 本地測試應用程式仍可運行
- [ ] Zeabur 環境變數已更新

---

## 🔒 防止未來再次發生

### 1. 使用 git-secrets

安裝 git-secrets 防止意外提交密碼：
```cmd
# 安裝（需要 Git Bash）
git clone https://github.com/awslabs/git-secrets.git
cd git-secrets
./install.sh
```

### 2. 使用 .gitignore

確保 `.gitignore` 包含：
```
# 敏感配置文件
application.properties
application-local.properties
.env
.env.local
```

### 3. 使用環境變數

永遠使用：
```properties
password=${ENV_VAR:default}
```

而不是：
```properties
password=actual_password
```

---

## 📞 需要幫助？

如果在執行過程中遇到問題：
1. 停止操作
2. 不要繼續推送
3. 檢查錯誤訊息
4. 參考 Git 文檔或尋求協助

---

**⚠️ 記住：刪除 Git 歷史後，仍需更改所有密碼！**

