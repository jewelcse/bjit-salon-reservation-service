package com.bjit.salon.reservation.service.dto.response;

import com.bjit.salon.reservation.service.entity.Catalog;
import com.bjit.salon.reservation.service.entity.EPaymentMethod;
import com.bjit.salon.reservation.service.entity.EWorkingStatus;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class ReservationResponseDto {

    private Long id;
    private long staffId;
    private long consumerId;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private EWorkingStatus workingStatus;
    private EPaymentMethod paymentMethod;
    private double totalPayableAmount;
    private List<CatalogResponse> services;
}
