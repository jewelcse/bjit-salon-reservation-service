package com.bjit.salon.reservation.service.service;

import com.bjit.salon.reservation.service.dto.request.ReservationCreateDto;
import com.bjit.salon.reservation.service.dto.request.ReservationStartsDto;
import com.bjit.salon.reservation.service.dto.response.ReservationResponseDto;

import java.util.List;

public interface ReservationService {
    void createReservation(ReservationCreateDto reservationCreateDto);

    List<ReservationResponseDto> getAllReservationByStaff(long id);

    void startWorking(ReservationStartsDto reservationStartsDto);
}
