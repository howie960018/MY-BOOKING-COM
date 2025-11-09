@echo off
echo ========================================
echo  本地開發環境啟動
echo ========================================
echo.
echo 正在設定環境變數...

REM 設定資料庫環境變數
set DATABASE_URL=jdbc:mysql://localhost:3306/booking_db?useSSL=false^&allowPublicKeyRetrieval=true^&serverTimezone=Asia/Taipei
set DB_USERNAME=root
set DB_PASSWORD=2FTA93108

REM 設定郵件環境變數
set MAIL_HOST=smtp.gmail.com
set MAIL_PORT=587
set MAIL_USERNAME=howie960018@gmail.com
set MAIL_PASSWORD=rgsqpqcanthwqars
set MAIL_FROM=howie960018@gmail.com
set MAIL_FROM_NAME=Booking Service

REM 設定應用程式環境變數
set APP_BASE_URL=http://localhost:8080
set DDL_AUTO=create-drop
set SHOW_SQL=true
set PORT=8080

echo ✓ 環境變數設定完成
echo.
echo ========================================
echo  啟動 Spring Boot 應用程式
echo ========================================
echo.

cd /d "%~dp0booking"
mvn spring-boot:run

pause

