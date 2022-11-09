package com.bjit.salon.reservation.service.service;

import com.bjit.salon.reservation.service.entity.Reservation;
import com.bjit.salon.reservation.service.entity.ReservationStatus;
import com.bjit.salon.reservation.service.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessedReservation implements ReservationManager {

    @Autowired
    private ReservationRepository reservationRepository;

    @Override
    public Reservation update(Reservation reservation) {
        reservation.setReservationStatus(ReservationStatus.PROCESSING);
        return reservationRepository.save(reservation);
    }
}
