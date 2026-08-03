package com.example.restaurantreservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@jakarta.persistence.Table(name = "tables")
public class Table {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String tableName;

    @OneToMany(cascade = CascadeType.ALL,
            mappedBy = "table",
            orphanRemoval = true)
    @Builder.Default
    List<TimeSlot> timeSlots = new ArrayList<>();

    @OneToOne(orphanRemoval = true,
            mappedBy = "table")
    Picture picture;

    Integer capacity;

    public Table(String tableName, Integer capacity) {
        this.tableName = tableName;
        this.capacity = capacity;
    }

    public void addTimeSlot(TimeSlot timeSlot) {
        if (Objects.nonNull(timeSlot)) {
            if (Objects.nonNull(timeSlots)) {
                assignTimeslot(timeSlot);
            } else {
                timeSlots = new ArrayList<>();
                assignTimeslot(timeSlot);
            }
        }
    }

    private void assignTimeslot(TimeSlot timeSlot) {
        timeSlots.add(timeSlot);
        timeSlot.setTable(this);
    }
}