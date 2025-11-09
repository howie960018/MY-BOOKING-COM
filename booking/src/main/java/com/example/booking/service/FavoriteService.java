package com.example.booking.service;

import com.example.booking.model.Accommodation;
import com.example.booking.model.Favorite;
import com.example.booking.model.User;
import com.example.booking.repository.AccommodationRepository;
import com.example.booking.repository.FavoriteRepository;
import com.example.booking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏服務
 * 處理用戶收藏住宿的功能
 */
@Service
public class FavoriteService {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteService.class);

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;

    /**
     * 添加收藏
     */
    @Transactional
    public Favorite addFavorite(String username, Long accommodationId) {
        logger.info("用戶 {} 添加收藏住宿 {}", username, accommodationId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("找不到用戶：" + username));

        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new RuntimeException("找不到住宿 ID=" + accommodationId));

        // 檢查是否已收藏
        if (favoriteRepository.existsByUserUsernameAndAccommodationId(username, accommodationId)) {
            throw new RuntimeException("已經收藏過此住宿");
        }

        Favorite favorite = new Favorite(user, accommodation);
        Favorite saved = favoriteRepository.save(favorite);

        logger.info("用戶 {} 成功添加收藏住宿 {}", username, accommodationId);
        return saved;
    }

    /**
     * 取消收藏
     */
    @Transactional
    public void removeFavorite(String username, Long accommodationId) {
        logger.info("用戶 {} 取消收藏住宿 {}", username, accommodationId);

        if (!favoriteRepository.existsByUserUsernameAndAccommodationId(username, accommodationId)) {
            throw new RuntimeException("尚未收藏此住宿");
        }

        favoriteRepository.deleteByUserUsernameAndAccommodationId(username, accommodationId);

        logger.info("用戶 {} 成功取消收藏住宿 {}", username, accommodationId);
    }

    /**
     * 切換收藏狀態（收藏/取消收藏）
     */
    @Transactional
    public boolean toggleFavorite(String username, Long accommodationId) {
        logger.info("用戶 {} 切換收藏狀態 住宿 {}", username, accommodationId);

        boolean isFavorited = favoriteRepository.existsByUserUsernameAndAccommodationId(username, accommodationId);

        if (isFavorited) {
            removeFavorite(username, accommodationId);
            return false; // 已取消收藏
        } else {
            addFavorite(username, accommodationId);
            return true; // 已添加收藏
        }
    }

    /**
     * 取得用戶的所有收藏
     */
    public List<Accommodation> getFavorites(String username) {
        logger.info("查詢用戶 {} 的收藏列表", username);

        List<Favorite> favorites = favoriteRepository.findByUserUsername(username);

        return favorites.stream()
                .map(Favorite::getAccommodation)
                .collect(Collectors.toList());
    }

    /**
     * 檢查是否已收藏
     */
    public boolean isFavorited(String username, Long accommodationId) {
        return favoriteRepository.existsByUserUsernameAndAccommodationId(username, accommodationId);
    }

    /**
     * 取得收藏數量
     */
    public long getFavoriteCount(String username) {
        return favoriteRepository.findByUserUsername(username).size();
    }
}

