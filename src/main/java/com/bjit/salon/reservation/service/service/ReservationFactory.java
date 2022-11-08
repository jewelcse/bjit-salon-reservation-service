package com.bjit.salon.reservation.service.service;

import com.bjit.salon.reservation.service.entity.Reservation;
import com.bjit.salon.reservation.service.entity.ReservationStatus;

public class ReservationFactory {

    public Reservation reserve(ReservationStatus status, Reservation reservation) {
        ReservationManager reservationManager = null;
        if (status == ReservationStatus.CANCELLED)
            reservationManager = new CancelledReservation(reservation);
        return reservationManager.reserve(ReservationStatus.CANCELLED);
    }
}
