@echo off
echo ========================================
echo  替換 application.properties 為環境變數版本
echo ========================================
echo.

cd /d "%~dp0src\main\resources"

echo 目前位置: %CD%
echo.

echo [1/3] 備份原始配置文件...
if exist application.properties (
    copy /Y application.properties application.properties.backup >nul
    echo     ✓ 已備份到 application.properties.backup
) else (
    echo     ! 警告: 找不到 application.properties
)
echo.

echo [2/3] 替換為環境變數版本...
if exist application.properties.new (
    copy /Y application.properties.new application.properties >nul
    echo     ✓ 已替換為使用環境變數的版本
) else (
    echo     ! 錯誤: 找不到 application.properties.new
    pause
    exit /b 1
)
echo.

echo [3/3] 驗證配置文件...
findstr /C:"${DB_PASSWORD" application.properties >nul
if %ERRORLEVEL% == 0 (
    echo     ✓ 配置文件使用環境變數 ✓
) else (
    echo     ! 警告: 配置文件可能未正確使用環境變數
)
echo.

echo ========================================
echo  替換完成！
echo ========================================
echo.
echo 已創建的文件:
echo   - application.properties (使用環境變數) ← 主要配置
echo   - application.properties.backup (原始配置) ← 備份
echo.
echo 下一步:
echo   1. 檢查 application.properties 內容
echo   2. 推送到 GitHub
echo   3. 在 Zeabur 設定環境變數
echo.
echo 詳細說明請參考:
echo   - ZEABUR_QUICK_START.md
echo   - ZEABUR_DEPLOYMENT_GUIDE.md
echo.
pause

