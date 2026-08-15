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
@jakarta.persistence.Table(
        name = "reservation",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reservation_table_date_time",
                        columnNames = {
                                "table_id",
                                "date",
                                "from_time",
                                "to_time"
                        }
                )
        }
)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long tableId;

    @NotNull
    LocalDate date;

    @ValidTimeFormat
    LocalTime fromTime;

    @ValidTimeFormat
    LocalTime toTime;

    @Version
    @Column(name = "version")
    private Long version;
}
