package com.example.restaurantreservation.repository;

import com.example.restaurantreservation.entity.Picture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PictureRepository extends JpaRepository<Picture, Long> {

    Optional<Picture> findPictureByTableId(Long tableId);
}
