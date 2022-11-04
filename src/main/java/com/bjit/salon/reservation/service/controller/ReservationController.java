package com.bjit.salon.reservation.service.controller;


import com.bjit.salon.reservation.service.dto.request.ReservationCreateDto;
import com.bjit.salon.reservation.service.dto.request.ReservationStatusUpdateDto;
import com.bjit.salon.reservation.service.dto.response.ReservationResponseDto;
import com.bjit.salon.reservation.service.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("reservation-service/api/v1/reservations")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    @PostMapping()
    public ResponseEntity<ReservationResponseDto> create(@Valid @RequestBody ReservationCreateDto reservationCreateDto) {
        // todo: Change making to creating
        log.info("Making a reservation by consumerId:{} in:{}",
                reservationCreateDto.getConsumerId(),
                reservationCreateDto.getReservationStartAt());
        return new ResponseEntity<>(reservationService.save(reservationCreateDto), HttpStatus.CREATED);
    }

    @GetMapping("/staffs/{id}")
    public ResponseEntity<List<ReservationResponseDto>> getAssignedReservations(@PathVariable("id") long id){
        // todo: Change getAllReservations to getReservationsStaffId
        List<ReservationResponseDto> reservations = reservationService.getAllReservationByStaff(id);
        log.info("Fetched {} reservations by staffId {}", reservations.size(),id);
        return ResponseEntity.ok(reservations);
    }

    @PostMapping("/status/update")
    public ResponseEntity<String> updateReservationStatus(@RequestBody ReservationStatusUpdateDto reservationStatusUpdateDto) {
        log.info("Updating reservation status by staff for id: {}", reservationStatusUpdateDto.getStaffId());
        // todo: give response from this method
        reservationService.updateStatus(reservationStatusUpdateDto);
        // todo: change message: reservation {id} status update to {status}
        return ResponseEntity.ok("Updated status");
    }


}
