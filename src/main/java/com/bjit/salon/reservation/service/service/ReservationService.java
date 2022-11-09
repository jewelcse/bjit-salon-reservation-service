package com.bjit.salon.reservation.service.service;

import com.bjit.salon.reservation.service.dto.request.ReservationCreateDto;
import com.bjit.salon.reservation.service.dto.request.ReservationStatusUpdateDto;
import com.bjit.salon.reservation.service.dto.response.ReservationResponseDto;
import com.bjit.salon.reservation.service.entity.Reservation;

import java.util.List;

public interface ReservationService {

    List<ReservationResponseDto> getReservationsStaffId(long id);

    Reservation updateStatus(ReservationStatusUpdateDto reservationStatusUpdateDto);

    ReservationResponseDto save(ReservationCreateDto reservationCreateDto);
}
