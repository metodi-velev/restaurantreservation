package com.example.restaurantreservation.entity;

import com.example.restaurantreservation.validator.ValidTimeFormat;
import jakarta.persistence.*;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@jakarta.persistence.Table(name = "timeslots")
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotNull
    LocalDate date;

    @ValidTimeFormat
    LocalTime fromTime;

    @ValidTimeFormat
    LocalTime toTime;

    @ManyToOne
    @JoinColumn(name = "table_id")
    Table table;

    boolean reserved;
}
