package com.bjit.salon.reservation.service.dto.request;


import com.bjit.salon.reservation.service.entity.EPaymentMethod;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class ReservationCreateDto {
    // todo: (PR-Review) Remove unnecessary comments
    // todo: (PR-Review) Use Instant instead of LocalData
    // todo: (PR-Review) Add validation for important properties
    private long staffId;   // staff id
    private long consumerId; // user id
    private LocalDate reservationDate;
    private LocalTime startTime;

    private LocalTime endTime;// will be calculated dynamically

    private double totalPayableAmount; // will be calculated dynamically

    // todo: (PR-Review) Remove E from this ENUM name
    private EPaymentMethod paymentMethod; // payment method
    private List<CatalogRequest> services;
}
