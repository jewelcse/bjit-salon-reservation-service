package com.bjit.salon.reservation.service.exception;

public class StaffAlreadyEngagedException extends RuntimeException {
    public StaffAlreadyEngagedException(String message){
        super(message);
    }
}
