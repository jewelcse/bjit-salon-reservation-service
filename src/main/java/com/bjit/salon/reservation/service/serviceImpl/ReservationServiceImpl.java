package com.bjit.salon.reservation.service.serviceImpl;


import com.bjit.salon.reservation.service.dto.producer.StaffActivityDto;
import com.bjit.salon.reservation.service.dto.request.CatalogRequest;
import com.bjit.salon.reservation.service.dto.request.ReservationCreateDto;
import com.bjit.salon.reservation.service.dto.request.ReservationStatusUpdateDto;
import com.bjit.salon.reservation.service.dto.response.ReservationResponseDto;
import com.bjit.salon.reservation.service.entity.ReservationStatus;
import com.bjit.salon.reservation.service.entity.Reservation;
import com.bjit.salon.reservation.service.exception.ReservationNotFoundException;
import com.bjit.salon.reservation.service.exception.StaffAlreadyEngagedException;
import com.bjit.salon.reservation.service.mapper.ReservationMapper;
import com.bjit.salon.reservation.service.producer.ReservationProducer;
import com.bjit.salon.reservation.service.repository.ReservationRepository;
import com.bjit.salon.reservation.service.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationProducer reservationProducer;

    @Override
    public List<ReservationResponseDto> getAllReservationByStaff(long id) {
        return reservationMapper.reservationsToReservationResponses(reservationRepository.findAllByStaffId(id));
    }

    @Override
    public Reservation updateStatus(ReservationStatusUpdateDto reservationStatusUpdateDto) {
        Optional<Reservation> currentReservation = reservationRepository.findById(reservationStatusUpdateDto.getReservationId());
        if (currentReservation.isEmpty()) {
            throw new ReservationNotFoundException("Reservation id is not found: " + reservationStatusUpdateDto.getReservationId());
        }
        Reservation updatedReservation = null;
        if (reservationStatusUpdateDto.getStatus() == ReservationStatus.CANCELLED) {
            updatedReservation = cancelReservation(currentReservation.get());
        }
        if (reservationStatusUpdateDto.getStatus() == ReservationStatus.ALLOCATED) {
            updatedReservation = allocateReservation(currentReservation.get());
        }
        if (reservationStatusUpdateDto.getStatus() == ReservationStatus.PROCESSING){
            updatedReservation = processReservation(currentReservation.get());
        }
        if (reservationStatusUpdateDto.getStatus() == ReservationStatus.COMPLETED) {
            updatedReservation = completeReservation(currentReservation.get());
        }

        publishActivity(updatedReservation);

        return updatedReservation;
    }

    private StaffActivityDto publishActivity(Reservation reservation){
        StaffActivityDto newActivityAndUpdateStatus = StaffActivityDto
                .builder()
                .staffId(reservation.getStaffId())
                .consumerId(reservation.getConsumerId())
                .reservationId(reservation.getId())
                .reservationStartAt(reservation.getReservationStartAt())
                .reservationEndAt(reservation.getReservationEndAt())
                .reservationStatus(reservation.getReservationStatus())
                .build();
        return reservationProducer.updateActivity(newActivityAndUpdateStatus);
    }

    private Reservation completeReservation(Reservation reservation){
        if (reservation.getReservationStatus() != ReservationStatus.CANCELLED
                && reservation.getReservationStatus() == ReservationStatus.PROCESSING){
            reservation.setReservationStatus(ReservationStatus.COMPLETED);
            reservation = reservationRepository.save(reservation);
        }
        return reservation;
    }

    private Reservation processReservation(Reservation reservation){
        if (reservation.getReservationStatus() != ReservationStatus.CANCELLED
                && reservation.getReservationStatus() == ReservationStatus.ALLOCATED){
            reservation.setReservationStatus(ReservationStatus.PROCESSING);
            reservation = reservationRepository.save(reservation);
        }
        return reservation;
    }

    private Reservation allocateReservation(Reservation reservation) {
        if (reservation.getReservationStatus() != ReservationStatus.CANCELLED
                && reservation.getReservationStatus() == ReservationStatus.INITIATED){
            reservation.setReservationStatus(ReservationStatus.ALLOCATED);
            reservation = reservationRepository.save(reservation);
        }
        return reservation;
    }

    private Reservation cancelReservation(Reservation reservation) {
        // todo: checking ReservationStatus: CANCELLED
        // todo: Change class name: ReservationStatus to ReservationStatus
        if (reservation.getReservationStatus() != ReservationStatus.CANCELLED
                && reservation.getReservationStatus() == ReservationStatus.INITIATED) {
            reservation.setReservationStatus(ReservationStatus.CANCELLED);
            reservation = reservationRepository.save(reservation);
        }
        return reservation;
        //todo: give separate message for: ALLOCATE, PROCESSING, COMPLETED, CANCELLED
    }


    // todo: debug the issue
//    private boolean isNotReservable(ReservationStatus status) {
//        return status != ReservationStatus.ALLOCATED || status != ReservationStatus.PROCESSING || status != ReservationStatus.COMPLETED;
//    }


    @Override
    public ReservationResponseDto save(ReservationCreateDto reservationCreateDto) {

        int totalApproximatedTime = getApproximateTotalTimeInMinutes(reservationCreateDto.getServices());
        Instant approximateEndTime = reservationCreateDto.getReservationStartAt().plus(totalApproximatedTime, ChronoUnit.MINUTES);

        if (isReserved(reservationCreateDto.getReservationStartAt(), approximateEndTime)) {
            throw new StaffAlreadyEngagedException("Reservation slot has already been filled up at:" + reservationCreateDto.getReservationStartAt());
        }
        Reservation newReservation = Reservation.builder()
                .staffId(reservationCreateDto.getStaffId())
                .consumerId(reservationCreateDto.getConsumerId())
                .reservationStartAt(reservationCreateDto.getReservationStartAt())
                .reservationEndAt(approximateEndTime)
                .reservationStatus(ReservationStatus.INITIATED)
                .paymentMethod(reservationCreateDto.getPaymentMethod())
                .services(reservationMapper.toCatalogs(reservationCreateDto.getServices()))
                .totalPayableAmount(getTotalPayableAmount(reservationCreateDto.getServices()))
                .build();

        return reservationMapper.toReservationResponse(reservationRepository.save(newReservation));
    }

    private boolean isReserved(Instant startTime, Instant endTime) {
        return reservationRepository
                .existsByReservationStartAtAndReservationEndAt(startTime, endTime);
    }

    private double getTotalPayableAmount(List<CatalogRequest> services) {
        return services.stream().filter(service -> service.getPayableAmount() != 0.0)
                .mapToDouble(CatalogRequest::getPayableAmount).sum();
    }

    private int getApproximateTotalTimeInMinutes(List<CatalogRequest> services) {
        return services.stream().filter(service -> service.getApproximateCompletionTime() != 0)
                .mapToInt(CatalogRequest::getApproximateCompletionTime).sum();
    }

}
