package com.bjit.salon.reservation.service.repository;


import com.bjit.salon.reservation.service.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,Long>{
    List<Reservation> findAllByStaffId(long id);
    boolean existsByReservationStartAtAndReservationEndAt(Instant startTime, Instant endTime);
}
