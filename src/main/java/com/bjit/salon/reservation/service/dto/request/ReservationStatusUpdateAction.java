package com.bjit.salon.reservation.service.dto.request;


import com.bjit.salon.reservation.service.entity.WorkingStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@ToString
// todo: Change class name to: ReservationStatusUpdateDto
public class ReservationStatusUpdateAction {
    // todo: change id to reservationId
    //todo: Validate
    private long id; // reservation id
    private long staffId;
    private WorkingStatus status;
}
