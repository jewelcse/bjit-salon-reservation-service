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

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Optional<Reservation> currentReservation = reservationRepository.findById(reservationStatusUpdateAction.getId());

        if (currentReservation.isEmpty()) {
            throw new ReservationNotFoundException("The reservation not found for id: " + reservationStatusUpdateAction.getId());
        }

        currentReservation.get().setWorkingStatus(reservationStatusUpdateAction.getStatus());
        reservationRepository.save(currentReservation.get());

        // event publishes to the staff service for creating a new activity,
        // updating the current status

        StaffActivityCreateAndUpdateDto newActivityAndUpdateStatus = StaffActivityCreateAndUpdateDto
                .builder()
                .staffId(currentReservation.get().getStaffId())
                .consumerId(currentReservation.get().getConsumerId())
                .reservationId(currentReservation.get().getId())
                .startTime(currentReservation.get().getStartTime())
                .endTime(currentReservation.get().getEndTime())
                .workingDate(currentReservation.get().getReservationDate())
                .workingStatus(currentReservation.get().getWorkingStatus())
                .build();

        return reservationProducer.createNewActivityAndUpdateActivityStatus(newActivityAndUpdateStatus);
    }

    @Override
    public ReservationResponseDto makeNewReservation(ReservationCreateDto reservationCreateDto) {
        int totalRequiredMinutes = getApproximateTotalTimeInMinutes(reservationCreateDto.getServices());
        LocalTime approximateEndTime = getApproximateEndTime(reservationCreateDto.getStartTime(),totalRequiredMinutes);
        reservationCreateDto.setEndTime(approximateEndTime);
        Reservation reservation = saveReservation(reservationCreateDto);
        return reservationMapper.toReservationResponse(reservation);
    }
    private Reservation saveReservation(ReservationCreateDto reservationCreateDto) {
        boolean alreadyHasReservation = reservationRepository
                .existsByReservationDateAndStartTimeAndEndTime(
                        reservationCreateDto.getReservationDate(),
                        reservationCreateDto.getStartTime(),
                        reservationCreateDto.getEndTime());
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
        log.info("Reservation completed with details: {}",newReservation.toString());
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
    private int getApproximateTotalTimeInMinutes(List<CatalogRequest>services) {
        return services.stream().filter(service -> service.getApproximateTimeForCompletion() != 0)
                .mapToInt(CatalogRequest::getApproximateTimeForCompletion).sum();
    }

}
