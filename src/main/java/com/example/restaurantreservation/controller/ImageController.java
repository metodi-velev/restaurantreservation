package com.example.restaurantreservation.controller;

import com.example.restaurantreservation.dto.ErrorDto;
import com.example.restaurantreservation.entity.Picture;
import com.example.restaurantreservation.entity.Table;
import com.example.restaurantreservation.repository.TableRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Table Images", description = "Endpoints for retrieving table images")
@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private TableRepository tableRepository;

    @Operation(
            summary = "Get table image",
            description = "Fetches the JPEG image binary associated with the given table ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Table image successfully retrieved",
                    content = @Content(mediaType = "image/jpeg", schema = @Schema(type = "string", format = "binary"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Table or image not found",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))
            )
    })
    @GetMapping("/table/{tableId}")
    public ResponseEntity<byte[]> getTableImage(
            @Parameter(description = "ID of the table whose image is to be retrieved", example = "1")
            @PathVariable Long tableId) {
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