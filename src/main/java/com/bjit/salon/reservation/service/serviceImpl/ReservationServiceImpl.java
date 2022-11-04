package com.bjit.salon.reservation.service.serviceImpl;


import com.bjit.salon.reservation.service.dto.producer.StaffActivityCreateAndUpdateDto;
import com.bjit.salon.reservation.service.dto.request.CatalogRequest;
import com.bjit.salon.reservation.service.dto.request.ReservationCreateDto;
import com.bjit.salon.reservation.service.dto.request.ReservationStatusUpdateDto;
import com.bjit.salon.reservation.service.dto.response.ReservationResponseDto;
import com.bjit.salon.reservation.service.entity.WorkingStatus;
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
    public StaffActivityCreateAndUpdateDto updateStatus(ReservationStatusUpdateDto reservationStatusUpdateDto) {
        Optional<Reservation> currentReservation = reservationRepository.findById(reservationStatusUpdateDto.getId());
        // todo: fix this code
        if (currentReservation.isEmpty()) {
            // todo: Reservation {id} is not found
            throw new ReservationNotFoundException("The reservation not found for id: " + reservationStatusUpdateDto.getId());
        }

        if (reservationStatusUpdateDto.getStatus() == WorkingStatus.CANCELLED) {
            cancelReservation(currentReservation.get());
        }
        if (reservationStatusUpdateDto.getStatus() == WorkingStatus.ALLOCATED) {
            allocateReservation(currentReservation.get());
        }
        if (reservationStatusUpdateDto.getStatus() == WorkingStatus.PROCESSING){
            processReservation(currentReservation.get());
        }
        if (reservationStatusUpdateDto.getStatus() == WorkingStatus.COMPLETED) {
            completeReservation(currentReservation.get());
        }

        StaffActivityCreateAndUpdateDto newActivityAndUpdateStatus = StaffActivityCreateAndUpdateDto
                .builder()
                .staffId(currentReservation.get().getStaffId())
                .consumerId(currentReservation.get().getConsumerId())
                .reservationId(currentReservation.get().getId())
                .reservationStartAt(currentReservation.get().getReservationStartAt())
                .reservationEndAt(currentReservation.get().getReservationEndAt())
                .workingStatus(currentReservation.get().getWorkingStatus())
                .build();
        return reservationProducer.createNewActivityAndUpdateActivityStatus(newActivityAndUpdateStatus);

    }

    private Reservation completeReservation(Reservation reservation){
        if (reservation.getWorkingStatus() != WorkingStatus.CANCELLED
                && reservation.getWorkingStatus() == WorkingStatus.PROCESSING){
            reservation.setWorkingStatus(WorkingStatus.COMPLETED);
            reservation = reservationRepository.save(reservation);
        }
        return reservation;
    }

    private Reservation processReservation(Reservation reservation){
        if (reservation.getWorkingStatus() != WorkingStatus.CANCELLED
                && reservation.getWorkingStatus() == WorkingStatus.ALLOCATED){
            reservation.setWorkingStatus(WorkingStatus.PROCESSING);
            reservation = reservationRepository.save(reservation);
        }
        return reservation;
    }

    private Reservation allocateReservation(Reservation reservation) {
        if (reservation.getWorkingStatus() != WorkingStatus.CANCELLED
                && reservation.getWorkingStatus() == WorkingStatus.INITIATED){
            reservation.setWorkingStatus(WorkingStatus.ALLOCATED);
            reservation = reservationRepository.save(reservation);
        }
        return reservation;
    }

    private Reservation cancelReservation(Reservation reservation) {
        // todo: checking WorkingStatus: CANCELLED
        // todo: Change class name: WorkingStatus to ReservationStatus
        if (reservation.getWorkingStatus() != WorkingStatus.CANCELLED
                && reservation.getWorkingStatus() == WorkingStatus.INITIATED) {
            reservation.setWorkingStatus(WorkingStatus.CANCELLED);
            reservation = reservationRepository.save(reservation);
        }
        return reservation;
        //todo: give seperate message for: ALLOCATE, PROCESSING, COMPLETED, CANCELLED
    }


    // todo: debug the issue
//    private boolean isNotReservable(WorkingStatus status) {
//        return status != WorkingStatus.ALLOCATED || status != WorkingStatus.PROCESSING || status != WorkingStatus.COMPLETED;
//    }


    @Override
    public ReservationResponseDto save(ReservationCreateDto reservationCreateDto) {

        int totalApproximatedTime = getApproximateTotalTimeInMinutes(reservationCreateDto.getServices());
        Instant approximateEndTime = reservationCreateDto.getReservationStartAt().plus(totalApproximatedTime, ChronoUnit.MINUTES);

        if (isReserved(reservationCreateDto.getReservationStartAt(), approximateEndTime)) {
            // todo: Change the message to : Reservation slot has already been filled up at: reservation time
            throw new StaffAlreadyEngagedException("The reservation has already taken on " + reservationCreateDto.getReservationStartAt());
        }
        Reservation newReservation = Reservation.builder()
                .staffId(reservationCreateDto.getStaffId())
                .consumerId(reservationCreateDto.getConsumerId())
                .reservationStartAt(reservationCreateDto.getReservationStartAt())
                .reservationEndAt(approximateEndTime)
                .workingStatus(WorkingStatus.INITIATED)
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
