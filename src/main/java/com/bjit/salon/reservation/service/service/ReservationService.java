package com.bjit.salon.reservation.service.service;

import com.bjit.salon.reservation.service.dto.producer.StaffActivityCreateAndUpdateDto;
import com.bjit.salon.reservation.service.dto.request.ReservationCreateDto;
import com.bjit.salon.reservation.service.dto.request.ReservationStatusUpdateAction;
import com.bjit.salon.reservation.service.dto.response.ReservationResponseDto;
import com.bjit.salon.reservation.service.entity.Reservation;

import java.util.List;

public interface ReservationService {

    List<ReservationResponseDto> getAllReservationByStaff(long id);

    StaffActivityCreateAndUpdateDto updateStatus(ReservationStatusUpdateAction reservationStatusUpdateAction);

    ReservationResponseDto save(ReservationCreateDto reservationCreateDto);
}
