package com.bjit.salon.reservation.service.dto.request;


import com.bjit.salon.reservation.service.entity.ReservationStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class ReservationStatusUpdateDto {

    //todo: Validate
    private long reservationId;
    private long staffId;
    private ReservationStatus status;
}
