package com.example.booking.repository;

import com.example.booking.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 查詢某住宿的所有評論（按時間倒序）
     */
    @Query("SELECT r FROM Review r WHERE r.accommodation.id = :accommodationId ORDER BY r.createdAt DESC")
    List<Review> findByAccommodationId(@Param("accommodationId") Long accommodationId);

    /**
     * 查詢某用戶的所有評論
     */
    @Query("SELECT r FROM Review r WHERE r.user.username = :username ORDER BY r.createdAt DESC")
    List<Review> findByUsername(@Param("username") String username);

    /**
     * 檢查用戶是否已評論過某住宿
     */
    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.accommodation.id = :accommodationId AND r.user.username = :username")
    boolean existsByAccommodationIdAndUsername(@Param("accommodationId") Long accommodationId, @Param("username") String username);
}

