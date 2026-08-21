package com.example.restaurantreservation.mapper;

import com.example.restaurantreservation.dto.ReservationDto;
import com.example.restaurantreservation.entity.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {
    public ReservationDto reservationToReservationDtoMapper(Reservation reservation) {
        return ReservationDto.builder()
                .tableId(reservation.getTableId())
                .date(reservation.getDate())
                .fromTime(reservation.getFromTime())
                .toTime(reservation.getToTime())
                .build();
    }

    public Reservation reservationDtoToReservation(ReservationDto reservation) {
        return Reservation.builder()
                .tableId(reservation.tableId())
                .date(reservation.date())
                .fromTime(reservation.fromTime())
                .toTime(reservation.toTime())
                .build();
    }
}
