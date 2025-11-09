package com.example.booking.repository;

import com.example.booking.model.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    // 模糊搜尋地點（忽略大小寫）
    List<Accommodation> findByLocationContainingIgnoreCase(String location);

    // 查詢在指定日期區間內可訂的住宿
    // ❗ 已更新為：透過房型 (RoomType) 與 Booking 的關聯進行過濾
    @Query("""
        SELECT DISTINCT a
        FROM Accommodation a
        JOIN a.roomTypes rt
        WHERE a.id NOT IN (
            SELECT DISTINCT rt.accommodation.id
            FROM Booking b
            WHERE (b.checkIn < :checkOut AND b.checkOut > :checkIn)
        )
        """)
    List<Accommodation> findAvailableAccommodations(
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    // 查詢指定用戶名擁有的所有住宿
    List<Accommodation> findByOwnerUsername(String username);
}
