package com.bjit.salon.reservation.service

import com.bjit.salon.reservation.service.dto.request.CatalogRequest
import com.bjit.salon.reservation.service.dto.request.ReservationCreateDto
import com.bjit.salon.reservation.service.dto.response.ReservationResponseDto
import com.bjit.salon.reservation.service.entity.Catalog
import com.bjit.salon.reservation.service.entity.EPaymentMethod
import com.bjit.salon.reservation.service.entity.EWorkingStatus
import com.bjit.salon.reservation.service.entity.Reservation
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

    def "should throw the reservation has already taken exception"(){

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

        reservationRepository.existsByStartTimeAndEndTime(startTime, endTime) >> true

        when:
        reservationService.makeNewReservation(reservationRequest)

        then:
        def exception = thrown(StaffAlreadyEngagedException)
        exception.message == "The reservation has already taken"

    }




}