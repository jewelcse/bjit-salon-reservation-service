package com.bjit.salon.reservation.service.serviceImpl;

import com.bjit.salon.reservation.service.entity.Reservation;
import com.bjit.salon.reservation.service.entity.ReservationStatus;
import com.bjit.salon.reservation.service.exception.ReservationTerminatedOrCanceledException;
import com.bjit.salon.reservation.service.repository.ReservationRepository;
import com.bjit.salon.reservation.service.service.ReservationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompletedReservation implements ReservationManager {

    @Autowired
    private ReservationRepository reservationRepository;

    @Override
    public Reservation update(Reservation reservation) {
        if(reservation.getReservationStatus() != ReservationStatus.CANCELLED
                && reservation.getReservationStatus() == ReservationStatus.PROCESSING){
            reservation.setReservationStatus(ReservationStatus.COMPLETED);
        }else{
            throw new ReservationTerminatedOrCanceledException("Invalid try");
        }
        return reservationRepository.save(reservation);
    }
}
