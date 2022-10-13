package com.bjit.salon.reservation.service.dto.request;

import lombok.*;

import javax.persistence.Column;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class CatalogRequest {

    private String name;
    private String description;
    private int approximateTimeForCompletion;
    private double payableAmount;
}
