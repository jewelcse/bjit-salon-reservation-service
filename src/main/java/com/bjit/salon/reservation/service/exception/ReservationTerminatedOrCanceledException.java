package com.bjit.salon.reservation.service.exception;

public class ReservationTerminatedOrCanceledException extends RuntimeException {
    public ReservationTerminatedOrCanceledException(String message){
        super(message);
    }
}
