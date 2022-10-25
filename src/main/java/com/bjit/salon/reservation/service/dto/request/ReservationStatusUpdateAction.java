package com.bjit.salon.reservation.service.dto.request;


import com.bjit.salon.reservation.service.entity.EWorkingStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class ReservationStatusUpdateAction {

    private long id; // reservation id
    private long staffId; // staff id
    private EWorkingStatus status;
}
