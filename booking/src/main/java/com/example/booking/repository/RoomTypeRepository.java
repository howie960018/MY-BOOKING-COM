package com.example.booking.repository;

import com.example.booking.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    List<RoomType> findByAccommodationId(Long accommodationId);
}
