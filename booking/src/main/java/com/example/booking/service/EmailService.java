package com.example.booking.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 郵件服務
 * 處理系統的郵件發送功能
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@booking.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:訂房系統}")
    private String fromName;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 發送簡單文字郵件
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            logger.info("簡單郵件已發送至 {}", to);
        } catch (Exception e) {
            logger.error("發送郵件失敗: {}", e.getMessage());
            throw new RuntimeException("郵件發送失敗", e);
        }
    }

    /**
     * 發送 HTML 郵件
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true 表示是 HTML

            mailSender.send(message);
            logger.info("HTML 郵件已發送至 {}", to);
        } catch (MessagingException e) {
            logger.error("發送 HTML 郵件失敗: {}", e.getMessage());
            throw new RuntimeException("郵件發送失敗", e);
        } catch (Exception e) {
            logger.error("發送郵件時發生錯誤: {}", e.getMessage());
            throw new RuntimeException("郵件發送失敗", e);
        }
    }

    /**
     * 發送密碼重設郵件
     */
    public void sendPasswordResetEmail(String to, String username, String resetToken) {
        String resetLink = baseUrl + "/user/reset-password?token=" + resetToken;

        String subject = "【訂房系統】密碼重設通知";

        String htmlContent = buildPasswordResetEmailHtml(username, resetLink);

        sendHtmlEmail(to, subject, htmlContent);
        logger.info("密碼重設郵件已發送至 {} (用戶: {})", to, username);
    }

    /**
     * 構建密碼重設郵件的 HTML 內容
     */
    private String buildPasswordResetEmailHtml(String username, String resetLink) {
        // 使用 String.format 並手動處理，避免 CSS 中的 # 被誤認為格式化符號
        String htmlTemplate = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .container {
                        background-color: #f9f9f9;
                        border-radius: 10px;
                        padding: 30px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 30px;
                    }
                    .header h1 {
                        color: #667eea;
                        margin: 0;
                    }
                    .content {
                        background-color: white;
                        padding: 25px;
                        border-radius: 8px;
                        margin-bottom: 20px;
                    }
                    .button {
                        display: inline-block;
                        padding: 12px 30px;
                        background: linear-gradient(135deg, rgb(102, 126, 234) 0%%, rgb(118, 75, 162) 100%%);
                        color: white;
                        text-decoration: none;
                        border-radius: 5px;
                        font-weight: bold;
                        margin: 20px 0;
                    }
                    .button:hover {
                        opacity: 0.9;
                    }
                    .warning {
                        background-color: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .footer {
                        text-align: center;
                        color: #666;
                        font-size: 12px;
                        margin-top: 30px;
                    }
                    .info-box {
                        background-color: #e7f3ff;
                        border-left: 4px solid #2196F3;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🏨 訂房系統</h1>
                        <p>密碼重設通知</p>
                    </div>
                    
                    <div class="content">
                        <p>親愛的 <strong>%s</strong>，您好：</p>
                        
                        <p>我們收到了您的密碼重設請求。請點擊下方按鈕來重設您的密碼：</p>
                        
                        <div style="text-align: center;">
                            <a href="%s" class="button">🔒 重設密碼</a>
                        </div>
                        
                        <div class="info-box">
                            <strong>📋 如果按鈕無法點擊，請複製以下連結到瀏覽器：</strong><br>
                            <a href="%s" style="word-break: break-all; color: #2196F3;">%s</a>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ 重要提示：</strong>
                            <ul style="margin: 10px 0; padding-left: 20px;">
                                <li>此連結將在 <strong>24 小時</strong>後失效</li>
                                <li>如果您沒有請求重設密碼，請忽略此郵件</li>
                                <li>請勿將此連結分享給他人</li>
                                <li>重設密碼後，舊密碼將立即失效</li>
                            </ul>
                        </div>
                        
                        <p style="margin-top: 20px;">
                            如有任何問題，請聯繫我們的客服團隊。
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p>此郵件由系統自動發送，請勿直接回覆</p>
                        <p>&copy; 2025 訂房系統 - 版權所有</p>
                    </div>
                </div>
            </body>
            </html>
            """;

        return String.format(htmlTemplate, username, resetLink, resetLink, resetLink);
    }

    /**
     * 發送歡迎郵件（註冊成功時）
     */
    public void sendWelcomeEmail(String to, String username) {
        String subject = "【訂房系統】歡迎加入！";

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .container { background-color: #f9f9f9; border-radius: 10px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; margin-bottom: 30px; }
                    .header h1 { color: #667eea; margin: 0; }
                    .content { background-color: white; padding: 25px; border-radius: 8px; }
                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 30px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🏨 訂房系統</h1>
                        <h2>歡迎加入！</h2>
                    </div>
                    <div class="content">
                        <p>親愛的 <strong>%s</strong>，您好：</p>
                        <p>感謝您註冊訂房系統！您的帳號已成功建立。</p>
                        <p>現在您可以開始：</p>
                        <ul>
                            <li>🔍 瀏覽各地精選住宿</li>
                            <li>❤️ 收藏喜愛的住宿</li>
                            <li>📅 線上訂房</li>
                            <li>📋 管理訂單</li>
                        </ul>
                        <p>祝您使用愉快！</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2025 訂房系統 - 版權所有</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username);

        sendHtmlEmail(to, subject, htmlContent);
        logger.info("歡迎郵件已發送至 {} (用戶: {})", to, username);
    }

    /**
     * 發送訂單確認郵件
     */
    public void sendBookingConfirmationEmail(String to, String username, String accommodationName,
                                            String checkIn, String checkOut, String totalPrice) {
        String subject = "【訂房系統】訂單確認通知";

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .container { background-color: #f9f9f9; border-radius: 10px; padding: 30px; }
                    .header { text-align: center; margin-bottom: 30px; }
                    .header h1 { color: #28a745; margin: 0; }
                    .content { background-color: white; padding: 25px; border-radius: 8px; }
                    .booking-info { background-color: #e7f3ff; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 30px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ 訂房成功！</h1>
                    </div>
                    <div class="content">
                        <p>親愛的 <strong>%s</strong>，您好：</p>
                        <p>您的訂房已確認，以下是訂單詳情：</p>
                        <div class="booking-info">
                            <p><strong>🏨 住宿名稱：</strong>%s</p>
                            <p><strong>📅 入住日期：</strong>%s</p>
                            <p><strong>📅 退房日期：</strong>%s</p>
                            <p><strong>💰 總金額：</strong>NT$ %s</p>
                        </div>
                        <p>期待您的光臨！如有任何問題，歡迎隨時聯繫我們。</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2025 訂房系統 - 版權所有</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username, accommodationName, checkIn, checkOut, totalPrice);

        sendHtmlEmail(to, subject, htmlContent);
        logger.info("訂單確認郵件已發送至 {} (用戶: {})", to, username);
    }
}

