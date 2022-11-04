package com.bjit.salon.reservation.service.dto.response;

import com.bjit.salon.reservation.service.entity.PaymentMethod;
import com.bjit.salon.reservation.service.entity.WorkingStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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
    private WorkingStatus workingStatus;
    private PaymentMethod paymentMethod;
    private double totalPayableAmount;
    private List<CatalogResponse> services;
}
