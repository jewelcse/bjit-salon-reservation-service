package com.bjit.salon.reservation.service.service;

import com.bjit.salon.reservation.service.dto.request.ReservationCreateDto;
import com.bjit.salon.reservation.service.dto.request.ReservationStatusUpdateAction;
import com.bjit.salon.reservation.service.dto.response.ReservationResponseDto;
import com.bjit.salon.reservation.service.entity.Reservation;

import java.util.List;

public interface ReservationService {
    void createReservation(ReservationCreateDto reservationCreateDto);

    List<ReservationResponseDto> getAllReservationByStaff(long id);

    void updateStatus(ReservationStatusUpdateAction reservationStatusUpdateAction);

    ReservationResponseDto makeNewReservation(ReservationCreateDto reservationCreateDto);
}
