package com.bjit.salon.reservation.service

import com.bjit.salon.reservation.service.dto.producer.StaffActivityCreateAndUpdateDto
import com.bjit.salon.reservation.service.dto.request.CatalogRequest
import com.bjit.salon.reservation.service.dto.request.ReservationCreateDto
import com.bjit.salon.reservation.service.dto.request.ReservationStatusUpdateAction
import com.bjit.salon.reservation.service.entity.Catalog
import com.bjit.salon.reservation.service.entity.EPaymentMethod
import com.bjit.salon.reservation.service.entity.EWorkingStatus
import com.bjit.salon.reservation.service.entity.Reservation
import com.bjit.salon.reservation.service.exception.ReservationNotFoundException
import com.bjit.salon.reservation.service.exception.StaffAlreadyEngagedException
import com.bjit.salon.reservation.service.mapper.ReservationMapper
import com.bjit.salon.reservation.service.producer.ReservationProducer
import com.bjit.salon.reservation.service.repository.ReservationRepository
import com.bjit.salon.reservation.service.serviceImpl.ReservationServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalTime


@SpringBootTest
class ReservationServiceApplicationTests extends Specification {

    private ReservationServiceImpl reservationService;
    private ReservationRepository reservationRepository;
    private ReservationProducer reservationProducer;
    @Autowired
    private ReservationMapper reservationMapper;

    def setup() {
        reservationRepository = Mock(ReservationRepository)
        reservationProducer = Mock(ReservationProducer)
        reservationService = new ReservationServiceImpl(reservationRepository, reservationMapper, reservationProducer)
    }

    def "should create a new reservation on a specific day"() {

        given:

        LocalTime startTime = LocalTime.parse("10:00:00")
        LocalTime endTime = LocalTime.parse("11:20:00")
        LocalDate reservationDate = LocalDate.parse("2024-04-28")


        def catalogReq = CatalogRequest.builder()
                .name("Normal Hair cut")
                .payableAmount(40.00)
                .approximateTimeForCompletion(60)
                .description("Normal service")
                .build()

        def catalogRes = Catalog.builder()
                .name("Normal Hair cut")
                .payableAmount(40.00)
                .approximateTimeForCompletion(60)
                .description("Normal service")
                .build()

        def requestedServices = [catalogReq, catalogReq, catalogReq]
        def responseServices = [catalogRes, catalogRes, catalogRes]

        def reservationRequest = ReservationCreateDto
                .builder()
                .staffId(1L)
                .consumerId(1L)
                .startTime(startTime)
                .paymentMethod(EPaymentMethod.CARD)
                .reservationDate(reservationDate)
                .services(requestedServices)
                .build()


        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(startTime)
                .endTime(endTime)
                .services(responseServices)
                .consumerId(1L)
                .workingStatus(EWorkingStatus.INITIATED)
                .paymentMethod(EPaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(reservationDate)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.existsByStartTimeAndEndTime(startTime, endTime) >> false
        reservationRepository.save(_) >> reservation

        when:
        def response = reservationService.makeNewReservation(reservationRequest)

        then:
        response.getConsumerId() == 1
        response.getServices().size() == 3
        response.getStartTime() == LocalTime.parse("10:00:00")
        response.getEndTime() == LocalTime.parse("11:20:00")
        response.getWorkingStatus() == EWorkingStatus.INITIATED
        response.getPaymentMethod() == EPaymentMethod.CARD
        response.getStaffId() == 1
        response.getReservationDate() == reservationDate
        response.getTotalPayableAmount() == (double) 100.00
    }

    def "should throw the reservation has already taken exception"() {

        given:
        LocalTime startTime = LocalTime.parse("10:00:00")
        LocalTime endTime = LocalTime.parse("11:20:00")
        LocalDate reservationDate = LocalDate.parse("2024-04-28")


        def catalogReq = CatalogRequest.builder()
                .name("Normal Hair cut")
                .payableAmount(40.00)
                .approximateTimeForCompletion(60)
                .description("Normal service")
                .build()

        def requestedServices = [catalogReq, catalogReq, catalogReq]

        def reservationRequest = ReservationCreateDto
                .builder()
                .staffId(1L)
                .consumerId(1L)
                .startTime(startTime)
                .paymentMethod(EPaymentMethod.CARD)
                .reservationDate(reservationDate)
                .services(requestedServices)
                .build()

        reservationRepository.existsByReservationDateAndStartTimeAndEndTime(_,_, _) >> true

        when:
        reservationService.makeNewReservation(reservationRequest)

        then:
        def exception = thrown(StaffAlreadyEngagedException)
        exception.message == "The reservation has already taken"

    }

    def "should return all reservation by staff id"() {

        given:

        def reservation = Reservation.builder()
                .build()

        def reservationResponse = [reservation, reservation, reservation]
        reservationRepository.findAllByStaffId(1L) >> reservationResponse

        when:

        def size = reservationRepository.findAllByStaffId(1L).size()

        then:

        size == 3

    }

    def "should return null reservation by staff id"() {
        given:
        reservationRepository.findAllByStaffId(1L) >> []

        when:
        def size = reservationRepository.findAllByStaffId(1L).size()

        then:

        size == 0
    }

    def "should update the working status by staff"() {

        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(EWorkingStatus.ALLOCATED)
                .build()

        def updateResponse = StaffActivityCreateAndUpdateDto.builder()
                .staffId(1L)
                .reservationId(1L)
                .workingDate(null)
                .workingStatus(EWorkingStatus.ALLOCATED)
                .startTime(null)
                .endTime(null)
                .consumerId(1L)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(EWorkingStatus.INITIATED)
                .paymentMethod(EPaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)
        reservationRepository.save(_) >> reservation
        reservationProducer.createNewActivityAndUpdateActivityStatus(_) >> updateResponse

        when:
        def response = reservationService.updateStatus(updateRequest)

        then:
        response.getStaffId() == 1
        response.getConsumerId() == 1
        response.getWorkingStatus() == EWorkingStatus.ALLOCATED

    }

    def "should throw reservation not found exception while updating the activity status"(){

        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(2L)
                .staffId(1L)
                .status(EWorkingStatus.ALLOCATED)
                .build()

        def reservation = Reservation
                .builder()
                .id(2L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(EWorkingStatus.INITIATED)
                .paymentMethod(EPaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.findById(2L) >> {throw new ReservationNotFoundException("The reservation not found for id: 2")}

        when:
        reservationService.updateStatus(updateRequest)

        then:
        def exception = thrown(ReservationNotFoundException)
        exception.message == "The reservation not found for id: 2"

    }


}