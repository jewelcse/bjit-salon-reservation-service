package com.bjit.salon.reservation.service.dto.producer;

import com.bjit.salon.reservation.service.entity.ReservationStatus;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class UpdateStatusProducer implements Serializable {
    private static final long serialVersionUID = 9178661439383356177L;
    private Long staffId;
    private Long consumerId;
    private Long reservationId;
    private Instant reservationStartAt;
    private Instant reservationEndAt;
    private ReservationStatus reservationStatus;
}