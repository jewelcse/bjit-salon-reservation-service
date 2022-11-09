package com.bjit.salon.reservation.service.service;

import com.bjit.salon.reservation.service.entity.Reservation;
import com.bjit.salon.reservation.service.entity.ReservationStatus;


public class ReservationFactory {

    private final Reservation reservation;
    private final ReservationStatus status;

    public ReservationFactory(Reservation reservation, ReservationStatus status) {
        this.reservation = reservation;
        this.status = status;
    }

    public Reservation updateReservationStatus(){
        Reservation updatedReservation = new Reservation();
        if(status == ReservationStatus.CANCELLED){
            updatedReservation = updateReservation(new CancelledReservation());
        }else if(status == ReservationStatus.ALLOCATED){
            updatedReservation = updateReservation(new AllocatedReservation());
        }else if(status == ReservationStatus.PROCESSING){
            updatedReservation = updateReservation(new ProcessedReservation());
        }else if(status == ReservationStatus.COMPLETED){
            updatedReservation = updateReservation(new CompletedReservation());
        }
        return updatedReservation;
    }

    public Reservation updateReservation(ReservationManager manager) {
        return manager.update(reservation);
    }
}
