package com.example.restaurantreservation.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Picture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    @Lob
    @Column(name = "image_data")
    byte[] imageData;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    Table table;
}