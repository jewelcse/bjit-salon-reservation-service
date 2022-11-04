package com.bjit.salon.reservation.service.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class CatalogRequest {

    private String name;
    private String description;
    private int approximateCompletionTime;
    private double payableAmount;
}
