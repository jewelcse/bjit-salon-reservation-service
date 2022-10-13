package com.bjit.salon.reservation.service.repository;


import com.bjit.salon.reservation.service.dto.response.ReservationResponseDto;
import com.bjit.salon.reservation.service.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,Long>{
    List<Reservation> findAllByReservationDate(LocalDate reservationDate);
    boolean existsByStartTimeAndEndTime(LocalTime startTime, LocalTime endTime);

    List<Reservation> findAllByStaffId(long id);

    List<Reservation> findAllByStaffIdAndReservationDate(long staffId, LocalDate reservationDate);
}
