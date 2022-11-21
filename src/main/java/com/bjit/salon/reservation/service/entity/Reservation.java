package com.bjit.salon.reservation.service.entity;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long staffId;
    private long consumerId;

    private Instant reservationStartAt;

    private Instant reservationEndAt;

    @Column(name = "working_status")
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;
    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    @Column(name = "total_payable_amount")
    private double totalPayableAmount;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "reservation_id", referencedColumnName = "id")
    private List<Catalog> services;

}
