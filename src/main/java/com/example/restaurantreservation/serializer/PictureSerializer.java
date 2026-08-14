package com.example.restaurantreservation.serializer;

import com.example.restaurantreservation.entity.Picture;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Base64;

public class PictureSerializer extends JsonSerializer<Picture> {

    @Override
    public void serialize(Picture picture, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (picture == null) {
            gen.writeNull();
            return;
        }

        gen.writeStartObject();
        gen.writeNumberField("id", picture.getId());
        gen.writeStringField("name", picture.getName());

        if (picture.getImageData() != null && picture.getImageData().length > 0) {
            String base64 = Base64.getEncoder().encodeToString(picture.getImageData());
            gen.writeStringField("imageBase64", base64);
            gen.writeStringField("imageDataUrl", "data:image/jpeg;base64," + base64);
        }

        if (picture.getTable() != null) {
            gen.writeNumberField("tableId", picture.getTable().getId());
        }

        gen.writeEndObject();
    }
}
