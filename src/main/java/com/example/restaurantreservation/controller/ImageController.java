package com.example.restaurantreservation.controller;

import com.example.restaurantreservation.entity.Picture;
import com.example.restaurantreservation.entity.Table;
import com.example.restaurantreservation.repository.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private TableRepository tableRepository;

    @GetMapping("/table/{tableId}")
    public ResponseEntity<byte[]> getTableImage(@PathVariable Long tableId) {
        Table table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        Picture picture = table.getPicture();
        if (picture == null || picture.getImageData() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(picture.getImageData().length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(picture.getImageData());
    }
}