package com.bjit.salon.reservation.service.controller;


import com.bjit.salon.reservation.service.dto.request.ReservationCreateDto;
import com.bjit.salon.reservation.service.dto.request.ReservationStatusUpdateDto;
import com.bjit.salon.reservation.service.dto.response.ReservationResponseDto;
import com.bjit.salon.reservation.service.entity.Reservation;
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
        log.info("Creating a reservation by consumerId:{} in:{}",
                reservationCreateDto.getConsumerId(),
                reservationCreateDto.getReservationStartAt());
        return new ResponseEntity<>(reservationService.save(reservationCreateDto), HttpStatus.CREATED);
    }

    @GetMapping("/staffs/{id}")
    public ResponseEntity<List<ReservationResponseDto>> getAssignedReservations(@PathVariable("id") long id) {
        List<ReservationResponseDto> reservations = reservationService.getReservationsStaffId(id);
        log.info("Fetched {} reservations by staffId {}", reservations.size(), id);
        return ResponseEntity.ok(reservations);
    }

    @PostMapping("/status/update")
    // todo: We can make the name short, change the message
    public ResponseEntity<String> updateReservationStatus(@Valid @RequestBody ReservationStatusUpdateDto reservationStatusUpdateDto) {
//        log.info("reservation {} of staff {}, status is updated to {}", reservationStatusUpdateDto.getReservationId(), reservationStatusUpdateDto.getStaffId(), reservationStatusUpdateDto.getStatus());
        log.info("Updating reservation status by staff for id: {}", reservationStatusUpdateDto.getStaffId());
        //todo: Use dto class instead of entity, respond with Dto
        Reservation reservation = reservationService.updateStatus(reservationStatusUpdateDto);
        return ResponseEntity.ok("Reservation "+ reservation.getId() +" status update to "+reservation.getReservationStatus());
    }

    // delete  ->
    // create -> Obj
    // update -> Obj


}
