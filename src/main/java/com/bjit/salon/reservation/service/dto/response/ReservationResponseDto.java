package com.bjit.salon.reservation.service.dto.response;

import com.bjit.salon.reservation.service.entity.PaymentMethod;
import com.bjit.salon.reservation.service.entity.ReservationStatus;
import lombok.*;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class ReservationResponseDto {

    private Long id;
    private long staffId;
    private long consumerId;
    private Instant reservationStartAt;
    private Instant reservationEndAt;
    private ReservationStatus reservationStatus;
    private PaymentMethod paymentMethod;
    private double totalPayableAmount;
    private List<CatalogResponse> services;
}
