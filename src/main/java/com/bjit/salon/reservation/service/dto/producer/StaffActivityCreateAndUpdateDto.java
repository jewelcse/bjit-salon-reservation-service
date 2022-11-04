package com.bjit.salon.reservation.service.dto.producer;

import com.bjit.salon.reservation.service.entity.WorkingStatus;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class StaffActivityCreateAndUpdateDto implements Serializable {
    private static final long serialVersionUID = 9178661439383356177L;
    private Long staffId;
    private Long consumerId;
    private Long reservationId;
    private Instant reservationStartAt;
    private Instant reservationEndAt;
    private WorkingStatus workingStatus;
}