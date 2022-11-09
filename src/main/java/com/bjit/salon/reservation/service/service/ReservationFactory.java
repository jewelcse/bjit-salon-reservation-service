package com.bjit.salon.reservation.service.service;

import com.bjit.salon.reservation.service.entity.Reservation;
import com.bjit.salon.reservation.service.entity.ReservationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class ReservationFactory {

    private Reservation reservation;

    @Autowired
    private CancelledReservation cancelReservation;
    @Autowired
    private AllocatedReservation allocateReservation;
    @Autowired
    private ProcessedReservation processReservation;
    @Autowired
    private CompletedReservation completeReservation;

    public Reservation updateReservationStatus(Reservation rs, ReservationStatus status ){
        this.reservation = rs;
        if(status == ReservationStatus.CANCELLED){
            return updateReservation(cancelReservation);
        }else if(status == ReservationStatus.ALLOCATED){
            return updateReservation(allocateReservation);
        }else if(status == ReservationStatus.PROCESSING){
           return updateReservation(processReservation);
        }else if(status == ReservationStatus.COMPLETED){
           return updateReservation(completeReservation);
        }
        return null;
    }

    public Reservation updateReservation(ReservationManager manager) {
        return manager.update(reservation);
    }
}
