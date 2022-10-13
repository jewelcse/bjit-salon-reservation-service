package com.bjit.salon.reservation.service.exception;

public class ReservationTimeOverlapException extends RuntimeException {
    public ReservationTimeOverlapException(String message){
        super(message);
    }
}
