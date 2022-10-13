package com.bjit.salon.reservation.service.controller;


import com.bjit.salon.reservation.service.dto.request.ReservationCreateDto;
import com.bjit.salon.reservation.service.dto.request.ReservationStartsDto;
import com.bjit.salon.reservation.service.dto.response.ReservationResponseDto;
import com.bjit.salon.reservation.service.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.bjit.salon.reservation.service.util.ConstraintsUtil.RESERVATION_SERVICE_APPLICATION_BASE_API;


@RequiredArgsConstructor
@RestController
@RequestMapping(RESERVATION_SERVICE_APPLICATION_BASE_API)
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    @PostMapping("/reservations")
    public ResponseEntity<String> makeReservation(@RequestBody ReservationCreateDto reservationCreateDto) {
        log.info("Making reservation by consumer for staff with: {}", reservationCreateDto.toString());
        reservationService.createReservation(reservationCreateDto);
        return ResponseEntity.ok("Reservation created success");
    }


    @GetMapping("/reservations/staff/{id}")
    public ResponseEntity<List<ReservationResponseDto>> getStaffReservations(@PathVariable("id") long id){
        List<ReservationResponseDto> allReservationByStaff = reservationService.getAllReservationByStaff(id);
        log.info("Getting all reservations by staff with size: {}", allReservationByStaff.size());
        return ResponseEntity.ok(allReservationByStaff);
    }

    @PostMapping("/reservations/starts")
    public ResponseEntity<String> starts(@RequestBody ReservationStartsDto reservationStartsDto) {
        log.info("Updating reservation status by staff for id: {}", reservationStartsDto.getStaffId());
        reservationService.startWorking(reservationStartsDto);
        return ResponseEntity.ok("Updated status");
    }


}
