package com.example.booking.service;

import com.example.booking.model.Booking;
import com.example.booking.repository.AccommodationRepository;
import com.example.booking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;

    // Simple memory cache (Redis or Caffeine recommended for production)
    private Map<String, Object> cache = new ConcurrentHashMap<>();
    private Map<String, Long> cacheTime = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

    /**
     * Check if cache is valid
     */
    private boolean isCacheValid(String key) {
        Long timestamp = cacheTime.get(key);
        return timestamp != null && (System.currentTimeMillis() - timestamp) < CACHE_DURATION;
    }

    /**
     * 取得快取資料或執行計算
     */
    @SuppressWarnings("unchecked")
    private <T> T getFromCacheOrCompute(String key, java.util.function.Supplier<T> supplier) {
        if (isCacheValid(key)) {
            logger.debug("Retrieved data from cache: {}", key);
            return (T) cache.get(key);
        }

        logger.debug("Recalculating data: {}", key);
        T result = supplier.get();
        cache.put(key, result);
        cacheTime.put(key, System.currentTimeMillis());
        return result;
    }

    /**
     * 檢查訂單是否屬於指定房東
     */
    private boolean isBookingOwnedBy(Booking booking, String ownerUsername) {
        try {
            return booking.getRoomType() != null
                   && booking.getRoomType().getAccommodation() != null
                   && booking.getRoomType().getAccommodation().getOwner() != null
                   && ownerUsername.equals(booking.getRoomType().getAccommodation().getOwner().getUsername());
        } catch (Exception e) {
            logger.warn("Unable to get owner info for booking {}: {}", booking.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * 取得訂單狀態分布統計
     * @return Map<狀態, 數量>
     */
    public Map<String, Long> getOrderStatusDistribution() {
        return getFromCacheOrCompute("orderStatusDistribution", () -> {
            logger.info("Calculating order status distribution");

            List<Booking> bookings = bookingRepository.findAll();

            Map<String, Long> statusMap = bookings.stream()
                .collect(Collectors.groupingBy(
                    b -> b.getStatus() != null ? b.getStatus() : "PENDING",
                    Collectors.counting()
                ));

            // 確保所有狀態都有值（即使是 0）
            statusMap.putIfAbsent("PENDING", 0L);
            statusMap.putIfAbsent("CONFIRMED", 0L);
            statusMap.putIfAbsent("CANCELLED", 0L);

            logger.info("Order status distribution calculation completed: {}", statusMap);
            return statusMap;
        });
    }

    /**
     * 取得房東的訂單狀態分布
     */
    public Map<String, Long> getOwnerOrderStatusDistribution(String ownerUsername) {
        logger.info("Calculating order status distribution for owner: {}", ownerUsername);

        List<Booking> bookings = bookingRepository.findAll().stream()
            .filter(b -> isBookingOwnedBy(b, ownerUsername))
            .collect(Collectors.toList());

        Map<String, Long> statusMap = bookings.stream()
            .collect(Collectors.groupingBy(
                b -> b.getStatus() != null ? b.getStatus() : "PENDING",
                Collectors.counting()
            ));

        statusMap.putIfAbsent("PENDING", 0L);
        statusMap.putIfAbsent("CONFIRMED", 0L);
        statusMap.putIfAbsent("CANCELLED", 0L);

        return statusMap;
    }

    /**
     * 取得近 N 天的訂單趨勢
     * @param days 天數
     * @return List of Map，每個 Map 包含 date, new, confirmed, cancelled
     */
    public List<Map<String, Object>> getOrdersTrend(int days) {
        logger.info("Calculating orders trend for recent {} days", days);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Booking> bookings = bookingRepository.findAll();

        // 建立日期對應的訂單 Map
        Map<LocalDate, List<Booking>> bookingsByDate = new LinkedHashMap<>();

        // 初始化所有日期
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            bookingsByDate.put(date, new ArrayList<>());
        }

        // 將訂單分組到對應日期（使用 createdAt 的日期）
        for (Booking booking : bookings) {
            if (booking.getCreatedAt() != null) {
                LocalDate bookingDate = booking.getCreatedAt().toLocalDate();
                if (!bookingDate.isBefore(startDate) && !bookingDate.isAfter(endDate)) {
                    bookingsByDate.get(bookingDate).add(booking);
                }
            }
        }

        // 轉換為前端需要的格式
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (Map.Entry<LocalDate, List<Booking>> entry : bookingsByDate.entrySet()) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", entry.getKey().format(formatter));

            List<Booking> dayBookings = entry.getValue();
            dayData.put("new", (long) dayBookings.size());
            dayData.put("confirmed", dayBookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus())).count());
            dayData.put("cancelled", dayBookings.stream()
                .filter(b -> "CANCELLED".equals(b.getStatus())).count());

            result.add(dayData);
        }

        logger.info("Orders trend calculation completed, total {} days", result.size());
        return result;
    }

    /**
     * 取得熱門住宿 Top N
     * @param limit 取前幾名
     * @return List of Map，每個 Map 包含 name, count
     */
    public List<Map<String, Object>> getTopAccommodations(int limit) {
        logger.info("Calculating top {} popular accommodations", limit);

        List<Booking> bookings = bookingRepository.findAll();

        // 統計每個住宿的訂單數
        Map<String, Long> accommodationCounts = bookings.stream()
            .collect(Collectors.groupingBy(
                b -> b.getRoomType().getAccommodation().getName(),
                Collectors.counting()
            ));

        // 排序並取前 N 名
        List<Map<String, Object>> result = accommodationCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                item.put("name", entry.getKey());
                item.put("count", entry.getValue());
                return item;
            })
            .collect(Collectors.toList());

        logger.info("Top accommodations calculation completed, total {} items", result.size());
        return result;
    }

    /**
     * 取得近 N 個月的月度營收
     * @param months 月數
     * @return List of Map，每個 Map 包含 month, revenue
     */
    public List<Map<String, Object>> getMonthlyRevenue(int months) {
        logger.info("Calculating monthly revenue for recent {} months", months);

        List<Booking> bookings = bookingRepository.findAll();

        YearMonth currentMonth = YearMonth.now();
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = months - 1; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);

            BigDecimal monthRevenue = bookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()))
                .filter(b -> {
                    if (b.getCheckIn() == null) return false;
                    YearMonth bookingMonth = YearMonth.from(b.getCheckIn());
                    return bookingMonth.equals(month);
                })
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            monthData.put("revenue", monthRevenue.doubleValue());

            result.add(monthData);
        }

        logger.info("Monthly revenue calculation completed, total {} months", result.size());
        return result;
    }

    /**
     * 取得房東的月度營收
     */
    public List<Map<String, Object>> getOwnerMonthlyRevenue(String ownerUsername, int months) {
        logger.info("Calculating monthly revenue for owner {} in recent {} months", ownerUsername, months);

        List<Booking> bookings = bookingRepository.findAll().stream()
            .filter(b -> isBookingOwnedBy(b, ownerUsername))
            .collect(Collectors.toList());

        YearMonth currentMonth = YearMonth.now();
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = months - 1; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);

            BigDecimal monthRevenue = bookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()))
                .filter(b -> {
                    if (b.getCheckIn() == null) return false;
                    YearMonth bookingMonth = YearMonth.from(b.getCheckIn());
                    return bookingMonth.equals(month);
                })
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            monthData.put("revenue", monthRevenue.doubleValue());

            result.add(monthData);
        }

        return result;
    }

    /**
     * 取得房東的住宿營收佔比
     */
    public List<Map<String, Object>> getOwnerAccommodationRevenue(String ownerUsername) {
        logger.info("Calculating accommodation revenue distribution for owner: {}", ownerUsername);

        List<Booking> bookings = bookingRepository.findAll().stream()
            .filter(b -> "CONFIRMED".equals(b.getStatus()))
            .filter(b -> isBookingOwnedBy(b, ownerUsername))
            .collect(Collectors.toList());

        // 統計每個住宿的營收
        Map<String, BigDecimal> accommodationRevenue = bookings.stream()
            .collect(Collectors.groupingBy(
                b -> b.getRoomType().getAccommodation().getName(),
                Collectors.reducing(BigDecimal.ZERO, Booking::getTotalPrice, BigDecimal::add)
            ));

        // 轉換為前端格式
        List<Map<String, Object>> result = accommodationRevenue.entrySet().stream()
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                item.put("name", entry.getKey());
                item.put("revenue", entry.getValue().doubleValue());
                return item;
            })
            .sorted((a, b) -> Double.compare(
                (Double) b.get("revenue"),
                (Double) a.get("revenue")
            ))
            .collect(Collectors.toList());

        logger.info("Accommodation revenue distribution calculation completed, total {} accommodations", result.size());
        return result;
    }

    /**
     * 取得房東的房型銷售排行
     */
    public List<Map<String, Object>> getOwnerRoomTypeSales(String ownerUsername) {
        logger.info("Calculating room type sales ranking for owner: {}", ownerUsername);

        List<Booking> bookings = bookingRepository.findAll().stream()
            .filter(b -> isBookingOwnedBy(b, ownerUsername))
            .collect(Collectors.toList());

        // 統計每個房型的訂單數
        Map<String, Long> roomTypeCounts = bookings.stream()
            .collect(Collectors.groupingBy(
                b -> {
                    try {
                        return b.getRoomType().getAccommodation().getName() + " - " + b.getRoomType().getName();
                    } catch (Exception e) {
                        return "Unknown Accommodation - Unknown Room Type";
                    }
                },
                Collectors.counting()
            ));

        // 排序並轉換為前端格式
        List<Map<String, Object>> result = roomTypeCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                item.put("name", entry.getKey());
                item.put("count", entry.getValue());
                return item;
            })
            .collect(Collectors.toList());

        logger.info("Room type sales ranking calculation completed, total {} room types", result.size());
        return result;
    }

    /**
     * 取得房東的入住率趨勢（近 N 天）
     */
    public List<Map<String, Object>> getOwnerOccupancyRate(String ownerUsername, int days) {
        logger.info("Calculating occupancy rate for owner {} in recent {} days", ownerUsername, days);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // 取得該房東的所有訂單
        List<Booking> bookings = bookingRepository.findAll().stream()
            .filter(b -> "CONFIRMED".equals(b.getStatus()))
            .filter(b -> isBookingOwnedBy(b, ownerUsername))
            .collect(Collectors.toList());

        // 計算總房間數
        int totalRooms = accommodationRepository.findByOwnerUsername(ownerUsername).stream()
            .flatMap(acc -> acc.getRoomTypes().stream())
            .mapToInt(rt -> rt.getTotalRooms())
            .sum();

        if (totalRooms == 0) {
            logger.warn("Owner {} has no rooms", ownerUsername);
            return new ArrayList<>();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;

            // 計算當天有多少房間被預訂
            int bookedRooms = bookings.stream()
                .filter(b -> !b.getCheckIn().isAfter(currentDate) && b.getCheckOut().isAfter(currentDate))
                .mapToInt(Booking::getBookedQuantity)
                .sum();

            double rate = (bookedRooms * 100.0) / totalRooms;

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", currentDate.format(formatter));
            dayData.put("rate", Math.round(rate * 10) / 10.0); // 保留一位小數

            result.add(dayData);
        }

        logger.info("Occupancy rate trend calculation completed, total {} days", result.size());
        return result;
    }
}
