package com.bjit.salon.reservation.service.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class CatalogResponse {

    private String name;
    private String description;
    private int approximateTimeForCompletion;
    private double payableAmount;
}
