package com.bjit.salon.reservation.service.service;

import com.bjit.salon.reservation.service.dto.producer.StaffActivityCreateAndUpdateDto;
import com.bjit.salon.reservation.service.dto.request.ReservationCreateDto;
import com.bjit.salon.reservation.service.dto.request.ReservationStatusUpdateDto;
import com.bjit.salon.reservation.service.dto.response.ReservationResponseDto;

import java.util.List;

public interface ReservationService {

    List<ReservationResponseDto> getAllReservationByStaff(long id);

    StaffActivityCreateAndUpdateDto updateStatus(ReservationStatusUpdateDto reservationStatusUpdateDto);

    ReservationResponseDto save(ReservationCreateDto reservationCreateDto);
}
