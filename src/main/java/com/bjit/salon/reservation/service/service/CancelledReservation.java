package com.bjit.salon.reservation.service.service;

import com.bjit.salon.reservation.service.entity.Reservation;
import com.bjit.salon.reservation.service.entity.ReservationStatus;
import org.springframework.stereotype.Component;

public class CancelledReservation implements ReservationManager {
    private final Reservation reservation;

    public CancelledReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    @Override
    public Reservation reserve(ReservationStatus reservationStatus) {
        if (reservationStatus != ReservationStatus.CANCELLED && reservation.getReservationStatus() == ReservationStatus.INITIATED) {
//            updateStatusAndSave(ReservationStatus.CANCELLED, reservation);
        }
        return null;
    }

//    private Reservation updateStatusAndSave(ReservationStatus status, Reservation reservation) {
////        reservation.setReservationStatus(status);
////        return reservationRepository.save(reservation);
//    }
}
