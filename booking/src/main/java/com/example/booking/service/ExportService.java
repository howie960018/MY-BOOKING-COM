package com.example.booking.service;

import com.example.booking.dto.BookingExportDTO;
import com.example.booking.model.Booking;
import com.example.booking.repository.BookingRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);

    @Autowired
    private BookingRepository bookingRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Convert Booking to DTO
     */
    private BookingExportDTO convertToDTO(Booking booking) {
        return new BookingExportDTO(
            booking.getId(),
            booking.getCreatedAt(),
            booking.getUser().getUsername(),
            booking.getRoomType().getAccommodation().getName(),
            booking.getRoomType().getName(),
            booking.getCheckIn(),
            booking.getCheckOut(),
            booking.getBookedQuantity(),
            booking.getRoomType().getPricePerNight(),
            booking.getTotalPrice(),
            booking.getStatus()
        );
    }

    /**
     * Export all bookings (Admin)
     */
    public byte[] exportAllBookings(LocalDate startDate, LocalDate endDate, String status) throws IOException {
        logger.info("管理員匯出所有訂單 - 開始日期: {}, 結束日期: {}, 狀態: {}", startDate, endDate, status);

        List<Booking> bookings = bookingRepository.findAll();

        // Filter conditions
        List<BookingExportDTO> data = bookings.stream()
            .filter(b -> startDate == null || !b.getCheckIn().isBefore(startDate))
            .filter(b -> endDate == null || !b.getCheckIn().isAfter(endDate))
            .filter(b -> status == null || status.isEmpty() || b.getStatus().equals(status))
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        byte[] result = generateBookingExcel(data, true);
        logger.info("管理員匯出完成 - 共 {} 筆訂單", data.size());
        return result;
    }

    /**
     * Export user bookings
     */
    public byte[] exportUserBookings(String username, LocalDate startDate, LocalDate endDate) throws IOException {
        logger.info("用戶 {} 匯出自己的訂單 - 開始日期: {}, 結束日期: {}", username, startDate, endDate);

        List<Booking> bookings = bookingRepository.findByUserUsername(username);

        List<BookingExportDTO> data = bookings.stream()
            .filter(b -> startDate == null || !b.getCheckIn().isBefore(startDate))
            .filter(b -> endDate == null || !b.getCheckIn().isAfter(endDate))
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        byte[] result = generateBookingExcel(data, false);
        logger.info("用戶 {} 匯出完成 - 共 {} 筆訂單", username, data.size());
        return result;
    }

    /**
     * Export owner bookings
     */
    public byte[] exportOwnerBookings(String ownerUsername, LocalDate startDate, LocalDate endDate) throws IOException {
        logger.info("房東 {} 匯出訂單 - 開始日期: {}, 結束日期: {}", ownerUsername, startDate, endDate);

        // 以 JOIN FETCH 並依房東帳號過濾，避免 NPE 與 Lazy 問題
        List<Booking> bookings = bookingRepository.findByOwnerUsernameFetchAll(ownerUsername);

        List<BookingExportDTO> data = bookings.stream()
            .filter(b -> startDate == null || !b.getCheckIn().isBefore(startDate))
            .filter(b -> endDate == null || !b.getCheckIn().isAfter(endDate))
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        byte[] result = generateBookingExcel(data, true);
        logger.info("房東 {} 匯出完成 - 共 {} 筆訂單", ownerUsername, data.size());
        return result;
    }

    /**
     * Generate Excel file
     */
    private byte[] generateBookingExcel(List<BookingExportDTO> data, boolean includeStatistics) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        // Sheet 1: Booking Details
        createBookingDetailsSheet(workbook, data);

        // Sheet 2: Statistics Summary (Admin/Owner only)
        if (includeStatistics) {
            createStatisticsSheet(workbook, data);
        }

        // Convert to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * Create Booking Details Sheet
     */
    private void createBookingDetailsSheet(Workbook workbook, List<BookingExportDTO> data) {
        Sheet sheet = workbook.createSheet("訂單明細");

        // Create styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "訂單編號", "訂單日期", "客戶姓名", "住宿名稱", "房型名稱",
            "入住日期", "退房日期", "住宿天數", "房間數量", "單晚價格", "訂單總額", "訂單狀態"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 填入資料
        int rowNum = 1;

        // 如果沒有資料，顯示提示訊息
        if (data.isEmpty()) {
            Row emptyRow = sheet.createRow(rowNum);
            Cell emptyCell = emptyRow.createCell(0);
            emptyCell.setCellValue("目前沒有符合條件的訂單資料");

            CellStyle messageStyle = workbook.createCellStyle();
            messageStyle.setAlignment(HorizontalAlignment.CENTER);
            Font messageFont = workbook.createFont();
            messageFont.setItalic(true);
            messageFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            messageStyle.setFont(messageFont);
            emptyCell.setCellStyle(messageStyle);

            // 合併儲存格（跨所有欄位）
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum, rowNum, 0, headers.length - 1));
        } else {
            // 原本的填入資料邏輯
            for (BookingExportDTO dto : data) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(dto.getId());

                Cell createdAtCell = row.createCell(1);
                if (dto.getCreatedAt() != null) {
                    createdAtCell.setCellValue(dto.getCreatedAt().format(DATETIME_FORMATTER));
                } else {
                    createdAtCell.setCellValue("");
                }
                createdAtCell.setCellStyle(dateStyle);

                row.createCell(2).setCellValue(dto.getCustomerName());
                row.createCell(3).setCellValue(dto.getAccommodationName());
                row.createCell(4).setCellValue(dto.getRoomTypeName());

                Cell checkInCell = row.createCell(5);
                checkInCell.setCellValue(dto.getCheckIn().format(DATE_FORMATTER));
                checkInCell.setCellStyle(dateStyle);

                Cell checkOutCell = row.createCell(6);
                checkOutCell.setCellValue(dto.getCheckOut().format(DATE_FORMATTER));
                checkOutCell.setCellStyle(dateStyle);

                row.createCell(7).setCellValue(dto.getNights());
                row.createCell(8).setCellValue(dto.getQuantity());

                Cell priceCell = row.createCell(9);
                priceCell.setCellValue(dto.getPricePerNight().doubleValue());
                priceCell.setCellStyle(currencyStyle);

                Cell totalCell = row.createCell(10);
                totalCell.setCellValue(dto.getTotalPrice().doubleValue());
                totalCell.setCellStyle(currencyStyle);

                row.createCell(11).setCellValue(dto.getStatusText());
            }
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }

        // Freeze header row
        sheet.createFreezePane(0, 1);
    }

    /**
     * Create Statistics Sheet
     */
    private void createStatisticsSheet(Workbook workbook, List<BookingExportDTO> data) {
        Sheet sheet = workbook.createSheet("統計摘要");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        // Statistics data
        long totalCount = data.size();
        long pendingCount = data.stream().filter(d -> "PENDING".equals(d.getStatus())).count();
        long confirmedCount = data.stream().filter(d -> "CONFIRMED".equals(d.getStatus())).count();
        long cancelledCount = data.stream().filter(d -> "CANCELLED".equals(d.getStatus())).count();

        BigDecimal totalRevenue = data.stream()
            .filter(d -> "CONFIRMED".equals(d.getStatus()))
            .map(BookingExportDTO::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        double avgOrderAmount = confirmedCount > 0
            ? totalRevenue.doubleValue() / confirmedCount
            : 0;

        // Create header row
        Row headerRow = sheet.createRow(0);
        Cell headerCell1 = headerRow.createCell(0);
        headerCell1.setCellValue("統計指標");
        headerCell1.setCellStyle(headerStyle);

        Cell headerCell2 = headerRow.createCell(1);
        headerCell2.setCellValue("數值");
        headerCell2.setCellStyle(headerStyle);

        // Fill statistics data
        int rowNum = 1;

        createStatRow(sheet, rowNum++, "總訂單數", totalCount);
        createStatRow(sheet, rowNum++, "待確認訂單數", pendingCount);
        createStatRow(sheet, rowNum++, "已確認訂單數", confirmedCount);
        createStatRow(sheet, rowNum++, "已取消訂單數", cancelledCount);

        Row revenueRow = sheet.createRow(rowNum++);
        revenueRow.createCell(0).setCellValue("總營收");
        Cell revenueCell = revenueRow.createCell(1);
        revenueCell.setCellValue(totalRevenue.doubleValue());
        revenueCell.setCellStyle(currencyStyle);

        Row avgRow = sheet.createRow(rowNum++);
        avgRow.createCell(0).setCellValue("平均訂單金額");
        Cell avgCell = avgRow.createCell(1);
        avgCell.setCellValue(avgOrderAmount);
        avgCell.setCellStyle(currencyStyle);

        // Adjust column widths
        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 4000);
    }

    /**
     * Create statistics row
     */
    private void createStatRow(Sheet sheet, int rowNum, String label, long value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    /**
     * Create header style
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        // Borders
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * Create date style
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Create currency style
     */
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("NT$ #,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }
}
