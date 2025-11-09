# 📋 上傳 GitHub 前的檢查清單

## ✅ 已完成的安全措施

1. ✅ **創建 .gitignore** 
   - 路徑：`booking/.gitignore`
   - 已忽略 `application.properties` 等敏感文件

2. ✅ **創建配置範本**
   - 路徑：`booking/src/main/resources/application.properties.example`
   - 已移除所有敏感資訊

3. ✅ **創建配置指南**
   - 路徑：`CONFIGURATION_GUIDE.md`
   - 詳細說明如何設定環境

## 🔐 需要隱藏的敏感資訊

在 `application.properties` 中發現以下敏感資訊：

| 項目 | 原始值 | 處理方式 |
|------|--------|----------|
| MySQL 密碼 | `2FTA93108` | ✅ 已加入 .gitignore |
| MySQL 用戶名 | `root` | ✅ 已加入 .gitignore |
| Gmail 帳號 | `howie960018@gmail.com` | ✅ 已加入 .gitignore |
| Gmail 應用程式密碼 | `rgsqpqcanthwqars` | ✅ 已加入 .gitignore |

## 📝 上傳前必做事項

### 1. 確認 .gitignore 生效

```bash
cd C:\my-booking-app-practice\booking

# 檢查 git 狀態
git status

# 確認 application.properties 不在待提交清單中
# 如果出現，執行：
git rm --cached src/main/resources/application.properties
```

### 2. 檢查是否還有其他敏感資訊

```bash
# 搜尋可能的密碼
git grep -i password

# 搜尋可能的 API key
git grep -i "api.key"
git grep -i "secret"
```

### 3. 初始化 Git 倉庫（如果尚未初始化）

```bash
cd C:\my-booking-app-practice

# 初始化 git
git init

# 添加所有文件（.gitignore 會自動過濾敏感文件）
git add .

# 確認暫存區內容
git status

# 提交
git commit -m "Initial commit: 訂房系統完整版"
```

### 4. 連接到 GitHub

```bash
# 添加遠端倉庫（替換成你的 GitHub 倉庫 URL）
git remote add origin https://github.com/你的用戶名/booking-system.git

# 推送到 GitHub
git branch -M main
git push -u origin main
```

## ⚠️ 重要警告

### 如果已經不小心上傳了敏感資訊

**方法 1：刪除歷史記錄（徹底方式）**

```bash
# 使用 BFG Repo-Cleaner
# 下載：https://rtyley.github.io/bfg-repo-cleaner/

# 刪除包含密碼的文件
bfg --delete-files application.properties

# 清理 git 歷史
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# 強制推送
git push --force
```

**方法 2：刪除倉庫重新上傳（簡單方式）**

1. 在 GitHub 上刪除倉庫
2. 修改本地密碼（資料庫、郵件等）
3. 重新創建倉庫並上傳

### 上傳後應立即更改的密碼

- ✅ MySQL 資料庫密碼
- ✅ Gmail 應用程式密碼
- ✅ 任何其他可能洩露的憑證

## 📂 應該上傳的文件

### ✅ 可以安全上傳

- `README.md`
- `CONFIGURATION_GUIDE.md`
- `API_DOCUMENTATION_GUIDE.md`
- `.gitignore`
- `pom.xml`
- `src/main/java/**/*.java`
- `src/main/resources/templates/**/*.html`
- `src/main/resources/static/**/*`
- `src/main/resources/application.properties.example`
- `src/main/resources/data.sql`
- `Dockerfile`

### ❌ 不應該上傳

- `application.properties`（含實際密碼）
- `target/`（編譯輸出）
- `data/`（資料庫文件）
- `.idea/`（IDE 設定）
- `*.iml`（IntelliJ 項目文件）
- 任何包含密碼、API key 的文件

## 🔍 最終檢查

在推送到 GitHub 前，執行以下檢查：

```bash
# 1. 檢查暫存區
git status

# 2. 查看即將提交的差異
git diff --cached

# 3. 確認沒有敏感資訊
git diff --cached | grep -i password
git diff --cached | grep -i secret

# 4. 查看提交歷史（確保沒有敏感資訊）
git log --oneline
git show HEAD
```

## 📄 README.md 更新建議

在 README.md 中添加配置說明：

```markdown
## 🔧 環境配置

本專案需要配置資料庫和郵件服務。請參考 [配置指南](CONFIGURATION_GUIDE.md) 進行設定。

**快速開始：**

1. 複製配置範本：
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```

2. 編輯 `application.properties` 並填入你的配置

3. 啟動應用程式：
   ```bash
   mvn spring-boot:run
   ```
```

## ✨ 上傳後的 GitHub 倉庫結構

```
your-repo/
├── README.md                          ✅ 公開
├── CONFIGURATION_GUIDE.md             ✅ 公開
├── API_DOCUMENTATION_GUIDE.md         ✅ 公開
├── booking/
│   ├── .gitignore                     ✅ 公開
│   ├── pom.xml                        ✅ 公開
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/                  ✅ 公開
│   │   │   └── resources/
│   │   │       ├── application.properties.example  ✅ 公開
│   │   │       ├── application.properties          ❌ 不上傳（.gitignore）
│   │   │       ├── data.sql           ✅ 公開
│   │   │       ├── static/            ✅ 公開
│   │   │       └── templates/         ✅ 公開
│   │   └── test/                      ✅ 公開
│   └── target/                        ❌ 不上傳（.gitignore）
└── data/                              ❌ 不上傳（.gitignore）
```

## 🎯 最後步驟

完成以下步驟後，即可安全上傳到 GitHub：

- [ ] 1. 確認 `.gitignore` 已創建
- [ ] 2. 確認 `application.properties.example` 已創建
- [ ] 3. 執行 `git status` 檢查
- [ ] 4. 確認 `application.properties` 不在待提交清單
- [ ] 5. 檢查沒有其他敏感資訊
- [ ] 6. 提交並推送到 GitHub
- [ ] 7. 在 GitHub 上檢查倉庫內容
- [ ] 8. 確認 `application.properties` 不存在於倉庫中

---

**準備好了嗎？開始上傳到 GitHub 吧！** 🚀

如有問題，請參考 [配置指南](CONFIGURATION_GUIDE.md)。

