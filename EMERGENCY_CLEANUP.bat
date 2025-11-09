@echo off
chcp 65001 >nul
echo ========================================
echo  🚨 緊急清理：從 GitHub 刪除敏感資訊
echo ========================================
echo.
echo ⚠️  警告：此腳本將：
echo    1. 刪除 Git 歷史記錄
echo    2. 替換配置文件為環境變數版本
echo    3. 重新初始化 Git 倉庫
echo.
echo ✅  執行前請確認：
echo    - 已備份重要資料
echo    - 已準備好更改所有密碼
echo    - 已準備刪除 GitHub 上的舊倉庫
echo.
set /p confirm="確定要繼續嗎？(輸入 YES 繼續): "
if /i not "%confirm%"=="YES" (
    echo 已取消操作
    pause
    exit /b 0
)

echo.
echo ========================================
echo  開始清理流程...
echo ========================================
echo.

REM 切換到專案目錄
cd /d "%~dp0"

echo [步驟 1/6] 備份當前代碼...
if not exist "..\my-booking-app-practice-backup" (
    echo     正在備份到 my-booking-app-practice-backup...
    xcopy . ..\my-booking-app-practice-backup /E /I /H /Y >nul 2>&1
    if %ERRORLEVEL% == 0 (
        echo     ✓ 備份完成
    ) else (
        echo     ✗ 備份失敗
        pause
        exit /b 1
    )
) else (
    echo     ! 備份目錄已存在，跳過備份
)
echo.

echo [步驟 2/6] 刪除 .git 目錄（移除所有歷史記錄）...
if exist ".git" (
    rmdir /S /Q .git >nul 2>&1
    if %ERRORLEVEL% == 0 (
        echo     ✓ Git 歷史已刪除
    ) else (
        echo     ✗ 刪除失敗，可能需要管理員權限
        pause
        exit /b 1
    )
) else (
    echo     ! .git 目錄不存在
)
echo.

echo [步驟 3/6] 替換配置文件為環境變數版本...
cd booking\src\main\resources
if exist "application.properties.new" (
    if exist "application.properties" (
        del application.properties >nul 2>&1
    )
    copy /Y application.properties.new application.properties >nul 2>&1
    echo     ✓ 配置文件已替換為環境變數版本
) else (
    echo     ✗ 找不到 application.properties.new
    cd ..\..\..\..
    pause
    exit /b 1
)
cd ..\..\..\..
echo.

echo [步驟 4/6] 重新初始化 Git 倉庫...
git init >nul 2>&1
if %ERRORLEVEL% == 0 (
    echo     ✓ Git 倉庫已初始化
) else (
    echo     ✗ Git 初始化失敗
    pause
    exit /b 1
)
echo.

echo [步驟 5/6] 添加所有文件到 Git...
git add . >nul 2>&1
if %ERRORLEVEL% == 0 (
    echo     ✓ 文件已添加
) else (
    echo     ✗ 添加文件失敗
    pause
    exit /b 1
)
echo.

echo [步驟 6/6] 創建初始提交...
git commit -m "Initial commit: 訂房系統（已移除敏感資訊，使用環境變數）" >nul 2>&1
if %ERRORLEVEL% == 0 (
    echo     ✓ 初始提交完成
) else (
    echo     ! 提交失敗（可能沒有變更）
)
echo.

echo ========================================
echo  ✅ 本地清理完成！
echo ========================================
echo.
echo 📋 接下來請手動執行：
echo.
echo 1. 更改所有密碼：
echo    - MySQL 資料庫密碼
echo    - Gmail 應用程式密碼
echo.
echo 2. 在 GitHub 上刪除舊倉庫：
echo    - 訪問倉庫頁面
echo    - Settings → Danger Zone → Delete repository
echo.
echo 3. 創建新倉庫並推送：
echo    - 在 GitHub 創建新倉庫
echo    - 執行以下命令：
echo.
echo    git remote add origin https://github.com/你的用戶名/新倉庫名.git
echo    git branch -M main
echo    git push -u origin main
echo.
echo 4. 更新 Zeabur 環境變數：
echo    - 使用新的密碼
echo.
echo ========================================
echo.
echo 詳細說明請參考：EMERGENCY_CLEANUP_GUIDE.md
echo.
pause

