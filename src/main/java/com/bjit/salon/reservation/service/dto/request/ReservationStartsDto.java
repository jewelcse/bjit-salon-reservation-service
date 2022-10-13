package com.bjit.salon.reservation.service.dto.request;


import com.bjit.salon.reservation.service.entity.EWorkingStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class ReservationStartsDto {

    private long id; // reservation id
    private long staffId; // staff id
    private EWorkingStatus status;
}
