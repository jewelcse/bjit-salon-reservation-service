package com.bjit.salon.reservation.service.service;

import com.bjit.salon.reservation.service.entity.Reservation;
import com.bjit.salon.reservation.service.entity.ReservationStatus;
import org.springframework.stereotype.Service;

@Service
public interface ReservationManager {
    Reservation reserve(ReservationStatus reservationStatus);
}
