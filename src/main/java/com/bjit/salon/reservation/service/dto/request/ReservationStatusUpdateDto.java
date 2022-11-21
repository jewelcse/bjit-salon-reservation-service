package com.bjit.salon.reservation.service.dto.request;


import com.bjit.salon.reservation.service.entity.ReservationStatus;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class ReservationStatusUpdateDto {


    @NotNull
    @Min(value=1, message = "reservation id can't be less than or equal 0")
    private long reservationId;
    @NotNull
    @Min(value=1, message = "staff id can't be less than or equal 0")
    private long staffId;
    private ReservationStatus status;
}
