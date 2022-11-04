package com.bjit.salon.reservation.service.dto.request;


import com.bjit.salon.reservation.service.entity.PaymentMethod;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class ReservationCreateDto {
    @NotNull(message = "consumer id can't be null")
    private long staffId;
    @NotNull(message = "consumer id can't be null")
    private long consumerId;
    @NotNull(message = "reservation time can't be null")
    private Instant reservationStartAt;
    @NotNull(message = "payment method can't be null")
    private PaymentMethod paymentMethod;
    @NotEmpty(message = "services can't be null")
    private List<CatalogRequest> services;
}
