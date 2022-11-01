package com.bjit.salon.reservation.service.serviceImpl;


import com.bjit.salon.reservation.service.dto.producer.StaffActivityCreateAndUpdateDto;
import com.bjit.salon.reservation.service.dto.request.CatalogRequest;
import com.bjit.salon.reservation.service.dto.request.ReservationCreateDto;
import com.bjit.salon.reservation.service.dto.request.ReservationStatusUpdateAction;
import com.bjit.salon.reservation.service.dto.response.ReservationResponseDto;
import com.bjit.salon.reservation.service.entity.Catalog;
import com.bjit.salon.reservation.service.entity.EWorkingStatus;
import com.bjit.salon.reservation.service.entity.Reservation;
import com.bjit.salon.reservation.service.exception.ReservationNotFoundException;
import com.bjit.salon.reservation.service.exception.ReservationTerminatedOrCanceledException;
import com.bjit.salon.reservation.service.exception.ReservationTimeOverlapException;
import com.bjit.salon.reservation.service.exception.StaffAlreadyEngagedException;
import com.bjit.salon.reservation.service.mapper.ReservationMapper;
import com.bjit.salon.reservation.service.producer.ReservationProducer;
import com.bjit.salon.reservation.service.repository.ReservationRepository;
import com.bjit.salon.reservation.service.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.bjit.salon.reservation.service.util.MethodsUtil.minutesToLocalTime;

@RequiredArgsConstructor
@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationProducer reservationProducer;

    @Override
    public List<ReservationResponseDto> getAllReservationByStaff(long id) {
        List<Reservation> allByStaffId = reservationRepository.findAllByStaffId(id);
        return reservationMapper.reservationsToReservationResponses(allByStaffId);
    }

    @Override
    public StaffActivityCreateAndUpdateDto updateStatus(ReservationStatusUpdateAction reservationStatusUpdateAction) {
        Reservation currentReservation = getReservationByReservationId(reservationStatusUpdateAction.getId());
        // check for reservation cancellation
        if (reservationStatusUpdateAction.getStatus() == EWorkingStatus.CANCELLED) {
            if (currentReservation.getWorkingStatus() == EWorkingStatus.ALLOCATED ||
                    currentReservation.getWorkingStatus() == EWorkingStatus.PROCESSING ||
                    currentReservation.getWorkingStatus() == EWorkingStatus.COMPLETED) {
                throw new ReservationTerminatedOrCanceledException("Yor can't cancel the reservation!");
            } else {
                if (currentReservation.getWorkingStatus() == EWorkingStatus.CANCELLED) {
                    throw new ReservationTerminatedOrCanceledException("Already you canceled reservation!");
                }
                // only initiated reservation can be canceled
                // no-need to notify the staff-service, bcz no need to create a new activity
                // since already canceled by staff
                if (currentReservation.getWorkingStatus() == EWorkingStatus.INITIATED) {
                    currentReservation.setWorkingStatus(EWorkingStatus.CANCELLED);
                    reservationRepository.save(currentReservation);
                    throw new ReservationTerminatedOrCanceledException("The reservation is canceled by staff!");
                }
            }
        }

        if (currentReservation.getWorkingStatus() == EWorkingStatus.CANCELLED) {
            throw new ReservationTerminatedOrCanceledException("Canceled reservation can't be re-initiated/allocated/processing/completed!");
        }

        if (reservationStatusUpdateAction.getStatus() == EWorkingStatus.ALLOCATED) {
            if (currentReservation.getWorkingStatus() == EWorkingStatus.PROCESSING
                    || currentReservation.getWorkingStatus() == EWorkingStatus.COMPLETED) {
                throw new ReservationTerminatedOrCanceledException("You can't be re-allocate the reservation!");
            } else if (currentReservation.getWorkingStatus() == EWorkingStatus.ALLOCATED) {
                throw new ReservationTerminatedOrCanceledException("The reservation is already in ALLOCATED stage!");
            } else {
                if (currentReservation.getWorkingStatus() == EWorkingStatus.INITIATED) {
                    currentReservation.setWorkingStatus(EWorkingStatus.ALLOCATED);
                    reservationRepository.save(currentReservation);
                }
            }
        } else if (reservationStatusUpdateAction.getStatus() == EWorkingStatus.PROCESSING) {
            if (currentReservation.getWorkingStatus() == EWorkingStatus.COMPLETED) {
                throw new ReservationTerminatedOrCanceledException("You can't be re-processing the reservation!");
            } else if (currentReservation.getWorkingStatus() == EWorkingStatus.PROCESSING) {
                throw new ReservationTerminatedOrCanceledException("The reservation is already in PROCESSING stage!");
            } else {
                if (currentReservation.getWorkingStatus() == EWorkingStatus.ALLOCATED) {
                    currentReservation.setWorkingStatus(EWorkingStatus.PROCESSING);
                    reservationRepository.save(currentReservation);
                } else {
                    throw new ReservationTerminatedOrCanceledException("You can't be process a reservation without allocated it before");
                }
            }
        } else if (reservationStatusUpdateAction.getStatus() == EWorkingStatus.COMPLETED) {
            if (currentReservation.getWorkingStatus() == EWorkingStatus.COMPLETED) {
                throw new ReservationTerminatedOrCanceledException("The reservation is already in COMPLETED stage!");
            }
            if (currentReservation.getWorkingStatus() == EWorkingStatus.PROCESSING) {
                currentReservation.setWorkingStatus(EWorkingStatus.COMPLETED);
                reservationRepository.save(currentReservation);
            } else {
                throw new ReservationTerminatedOrCanceledException("You can't be complete a reservation without processed it before");
            }
        } else {
            throw new ReservationTerminatedOrCanceledException("You can't re-initiated the reservation again!");
        }
        // notify the staff-service for creating a new activity/ update the status
        StaffActivityCreateAndUpdateDto newActivityAndUpdateStatus = createOrUpdateActivity(currentReservation);
        return reservationProducer.createNewActivityAndUpdateActivityStatus(newActivityAndUpdateStatus);

    }

    @Override
    public ReservationResponseDto makeNewReservation(ReservationCreateDto reservationCreateDto) {
        int totalRequiredMinutes = getApproximateTotalTimeInMinutes(reservationCreateDto.getServices());
        LocalTime approximateEndTime = getApproximateEndTime(reservationCreateDto.getStartTime(), totalRequiredMinutes);
        reservationCreateDto.setEndTime(approximateEndTime);
        Reservation reservation = saveReservation(reservationCreateDto);
        return reservationMapper.toReservationResponse(reservation);
    }

    private Reservation saveReservation(ReservationCreateDto reservationCreateDto) {
        boolean alreadyHasReservation = getReservationByDateAndStartTimeAndEndTime(reservationCreateDto.getReservationDate(),
                reservationCreateDto.getStartTime(),
                reservationCreateDto.getEndTime()
        );
        if (alreadyHasReservation) {
            throw new StaffAlreadyEngagedException("The reservation has already taken");
        }
        double totalPayableAmount = getTotalPayableAmount(reservationCreateDto.getServices());
        reservationCreateDto.setTotalPayableAmount(totalPayableAmount);
        return addReservationToDatabase(reservationCreateDto);
    }

    private Reservation addReservationToDatabase(ReservationCreateDto reservationCreateDto) {
        Reservation newReservation = Reservation.builder()
                .staffId(reservationCreateDto.getStaffId())
                .consumerId(reservationCreateDto.getConsumerId())
                .reservationDate(reservationCreateDto.getReservationDate())
                .startTime(reservationCreateDto.getStartTime())
                .endTime(reservationCreateDto.getEndTime())
                .workingStatus(EWorkingStatus.INITIATED)
                .paymentMethod(reservationCreateDto.getPaymentMethod())
                .services(reservationMapper.toCatalogs(reservationCreateDto.getServices()))
                .totalPayableAmount(reservationCreateDto.getTotalPayableAmount())
                .build();
        log.info("Reservation completed with details: {}", newReservation.toString());
        return reservationRepository.save(newReservation); //mock
    }

    private double getTotalPayableAmount(List<CatalogRequest> services) {
        return services.stream().filter(service -> service.getPayableAmount() != 0.0)
                .mapToDouble(CatalogRequest::getPayableAmount).sum();
    }

    private LocalTime getApproximateEndTime(LocalTime startTime, int totalRequiredMinutes) {
        LocalTime endTime = minutesToLocalTime(totalRequiredMinutes);
        return startTime.plusHours(endTime.getHour())
                .plusMinutes(endTime.getMinute());
    }

    private int getApproximateTotalTimeInMinutes(List<CatalogRequest> services) {
        return services.stream().filter(service -> service.getApproximateTimeForCompletion() != 0)
                .mapToInt(CatalogRequest::getApproximateTimeForCompletion).sum();
    }

    private Reservation getReservationByReservationId(Long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        if (reservation.isEmpty()) {
            throw new ReservationNotFoundException("The reservation not found for id: " + id);
        }
        return reservation.get();
    }

    private boolean getReservationByDateAndStartTimeAndEndTime(LocalDate reservationDate, LocalTime startTime, LocalTime endTime) {
        return reservationRepository
                .existsByReservationDateAndStartTimeAndEndTime(reservationDate, startTime, endTime);
    }

    private StaffActivityCreateAndUpdateDto createOrUpdateActivity(Reservation reservation){
        return StaffActivityCreateAndUpdateDto
                .builder()
                .staffId(reservation.getStaffId())
                .consumerId(reservation.getConsumerId())
                .reservationId(reservation.getId())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .workingDate(reservation.getReservationDate())
                .workingStatus(reservation.getWorkingStatus())
                .build();
    }

}
