package com.example.booking.repository;

import com.example.booking.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    /**
     * 查詢用戶的所有收藏
     */
    @Query("""
           SELECT f FROM Favorite f
           JOIN FETCH f.accommodation a
           JOIN FETCH a.owner
           WHERE f.user.username = :username
           ORDER BY f.createdAt DESC
           """)
    List<Favorite> findByUserUsername(@Param("username") String username);

    /**
     * 查詢用戶是否已收藏某住宿
     */
    @Query("""
           SELECT f FROM Favorite f
           WHERE f.user.username = :username
           AND f.accommodation.id = :accommodationId
           """)
    Optional<Favorite> findByUserUsernameAndAccommodationId(
            @Param("username") String username,
            @Param("accommodationId") Long accommodationId
    );

    /**
     * 刪除用戶的收藏
     */
    void deleteByUserUsernameAndAccommodationId(String username, Long accommodationId);

    /**
     * 檢查是否已收藏
     */
    boolean existsByUserUsernameAndAccommodationId(String username, Long accommodationId);
}

