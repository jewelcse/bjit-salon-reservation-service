package com.bjit.salon.reservation.service.serviceImpl

import com.bjit.salon.reservation.service.dto.producer.StaffActivityCreateAndUpdateDto
import com.bjit.salon.reservation.service.dto.request.CatalogRequest
import com.bjit.salon.reservation.service.dto.request.ReservationCreateDto
import com.bjit.salon.reservation.service.dto.request.ReservationStatusUpdateAction
import com.bjit.salon.reservation.service.entity.Catalog
import com.bjit.salon.reservation.service.entity.PaymentMethod
import com.bjit.salon.reservation.service.entity.WorkingStatus
import com.bjit.salon.reservation.service.entity.Reservation
import com.bjit.salon.reservation.service.exception.ReservationNotFoundException
import com.bjit.salon.reservation.service.exception.ReservationTerminatedOrCanceledException
import com.bjit.salon.reservation.service.exception.StaffAlreadyEngagedException
import com.bjit.salon.reservation.service.mapper.ReservationMapper
import com.bjit.salon.reservation.service.producer.ReservationProducer
import com.bjit.salon.reservation.service.repository.ReservationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalTime


@SpringBootTest
class ReservationServiceApplicationUnitTest extends Specification {

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

    def "should return all reservation by staff id"() {

        given:
        def reservation = Reservation.builder()
                .id(1L)
                .staffId(1L)
                .consumerId(1L)
                .startTime(LocalTime.parse("10:00:00"))
                .endTime(LocalTime.parse("12:00:00"))
                .paymentMethod(PaymentMethod.CARD)
                .reservationDate(LocalDate.parse("2022-10-10"))
                .services(null)
                .build()

        def reservationResponse = [reservation, reservation, reservation]
        reservationRepository.findAllByStaffId(1L) >> reservationResponse

        when:
        def size = reservationService.getAllReservationByStaff(1L).size()

        then:
        size == 3

    }

    def "should return null reservation by staff id"() {
        given:
        reservationRepository.findAllByStaffId(1L) >> []

        when:
        def size = reservationService.getAllReservationByStaff(1L).size()

        then:

        size == 0
    }

    def "should create a new reservation on a specific day"() {

        given:

        LocalTime startTime = LocalTime.parse("10:00:00")
        LocalTime endTime = LocalTime.parse("11:20:00")
        LocalDate reservationDate = LocalDate.parse("2024-04-28")


        def catalogReq = CatalogRequest.builder()
                .name("Normal Hair cut")
                .payableAmount(40.00)
                .approximateCompletionTime(60)
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
                .paymentMethod(PaymentMethod.CARD)
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
                .workingStatus(WorkingStatus.INITIATED)
                .paymentMethod(PaymentMethod.CARD)
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
        response.getWorkingStatus() == WorkingStatus.INITIATED
        response.getPaymentMethod() == PaymentMethod.CARD
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
                .approximateCompletionTime(60)
                .description("Normal service")
                .build()

        def requestedServices = [catalogReq, catalogReq, catalogReq]

        def reservationRequest = ReservationCreateDto
                .builder()
                .staffId(1L)
                .consumerId(1L)
                .startTime(startTime)
                .paymentMethod(PaymentMethod.CARD)
                .reservationDate(reservationDate)
                .services(requestedServices)
                .build()

        reservationRepository.existsByReservationDateAndStartTimeAndEndTime(_, _, _) >> true

        when:
        reservationService.makeNewReservation(reservationRequest)

        then:
        def exception = thrown(StaffAlreadyEngagedException)
        exception.message == "The reservation has already taken"

    }

    def "should throw reservation not found exception while updating the reservation status"() {

        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(2L)
                .staffId(1L)
                .status(WorkingStatus.ALLOCATED)
                .build()

        reservationRepository.findById(2L) >> Optional.ofNullable(null)

        when:
        reservationService.updateStatus(updateRequest)

        then:
        def exception = thrown(ReservationNotFoundException)
        exception.message == "The reservation not found for id: 2"

    }

    def "should throw you can not cancel the reservation exception while cancelling the reservation because the reservation already allocated"() {

        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.CANCELLED)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.ALLOCATED)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)

        when:
        reservationService.updateStatus(updateRequest)

        then:
        def exception = thrown(ReservationTerminatedOrCanceledException)
        exception.message == "Yor can't cancel the reservation!"

    }

    def "should throw you can not cancel the reservation exception while cancelling the reservation because the reservation already is processing"() {

        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.CANCELLED)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.PROCESSING)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)

        when:
        reservationService.updateStatus(updateRequest)

        then:
        def exception = thrown(ReservationTerminatedOrCanceledException)
        exception.message == "Yor can't cancel the reservation!"

    }

    def "should throw you can not cancel the reservation exception while cancelling the reservation because the reservation already completed"() {

        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.CANCELLED)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.COMPLETED)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)

        when:
        reservationService.updateStatus(updateRequest)

        then:
        def exception = thrown(ReservationTerminatedOrCanceledException)
        exception.message == "Yor can't cancel the reservation!"

    }

    def "should throw already cancelled reservation exception while cancelling the reservation"() {

        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.CANCELLED)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.CANCELLED)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)

        when:
        reservationService.updateStatus(updateRequest)

        then:
        def exception = thrown(ReservationTerminatedOrCanceledException)
        exception.message == "Already you canceled reservation!"

    }

    def "should cancelled reservation"() {

        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.CANCELLED)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.INITIATED)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)
        reservationRepository.save(reservation)

        when:
        reservationService.updateStatus(updateRequest)

        then:
        def exception = thrown(ReservationTerminatedOrCanceledException)
        exception.message == "The reservation is canceled by staff!"

    }

    def "should throw canceled reservation can't be re-initiated-allocated-processing-completed exception while trying to re-initiated-allocated-processing-completed"(){
        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.ALLOCATED)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.CANCELLED)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)

        when:
        reservationService.updateStatus(updateRequest)

        then:
        def exception = thrown(ReservationTerminatedOrCanceledException)
        exception.message == "Canceled reservation can't be re-initiated/allocated/processing/completed!"
    }

    def "should throw exception while you re-allocate the reservation bcz the reservation already in processing stage"(){
        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.ALLOCATED)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.PROCESSING)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)

        when:
        reservationService.updateStatus(updateRequest)

        then:
        def exception = thrown(ReservationTerminatedOrCanceledException)
        exception.message == "You can't be re-allocate the reservation!"
    }

    def "should throw exception while you re-allocate the reservation bcz the reservation already in completed stage"(){
        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.ALLOCATED)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.COMPLETED)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)

        when:
        reservationService.updateStatus(updateRequest)

        then:
        def exception = thrown(ReservationTerminatedOrCanceledException)
        exception.message == "You can't be re-allocate the reservation!"
    }

    def "should throw exception while you re-allocate the reservation bcz the reservation already in allocated stage"(){
        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.ALLOCATED)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.ALLOCATED)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)

        when:
        reservationService.updateStatus(updateRequest)

        then:
        def exception = thrown(ReservationTerminatedOrCanceledException)
        exception.message == "The reservation is already in ALLOCATED stage!"
    }

    def "should allocate the reservation after initiated it"(){
        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.ALLOCATED)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.INITIATED)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        def updateResponse = StaffActivityCreateAndUpdateDto.builder()
                .staffId(1L)
                .reservationId(1L)
                .workingDate(null)
                .workingStatus(WorkingStatus.ALLOCATED)
                .startTime(null)
                .endTime(null)
                .consumerId(1L)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)
        reservationRepository.save(_) >> reservation

        reservationProducer.createNewActivityAndUpdateActivityStatus(_) >> updateResponse

        when:
        def response = reservationService.updateStatus(updateRequest)

        then:
        response.getStaffId() == 1
        response.getConsumerId() == 1
        response.getWorkingStatus() == WorkingStatus.ALLOCATED

    }

    def "should throw exception while you re-processing the reservation bcz the reservation already in completed stage"(){
        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.PROCESSING)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.COMPLETED)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)

        when:
        reservationService.updateStatus(updateRequest)

        then:
        def exception = thrown(ReservationTerminatedOrCanceledException)
        exception.message == "You can't be re-processing the reservation!"
    }

    def "should throw exception while you re-processing the reservation bcz the reservation already in processing stage"(){
        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.PROCESSING)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.PROCESSING)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)

        when:
        reservationService.updateStatus(updateRequest)

        then:
        def exception = thrown(ReservationTerminatedOrCanceledException)
        exception.message == "The reservation is already in PROCESSING stage!"
    }

    def "should processing the reservation after allocated it"(){
        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.PROCESSING)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.ALLOCATED)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        def updateResponse = StaffActivityCreateAndUpdateDto.builder()
                .staffId(1L)
                .reservationId(1L)
                .workingDate(null)
                .workingStatus(WorkingStatus.PROCESSING)
                .startTime(null)
                .endTime(null)
                .consumerId(1L)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)
        reservationRepository.save(_) >> reservation

        reservationProducer.createNewActivityAndUpdateActivityStatus(_) >> updateResponse

        when:
        def response = reservationService.updateStatus(updateRequest)

        then:
        response.getStaffId() == 1
        response.getConsumerId() == 1
        response.getWorkingStatus() == WorkingStatus.PROCESSING
    }

    def "should throw exception while you re-completing the reservation bcz the reservation already in completed stage"(){
        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.COMPLETED)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.COMPLETED)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)

        when:
        reservationService.updateStatus(updateRequest)

        then:
        def exception = thrown(ReservationTerminatedOrCanceledException)
        exception.message == "The reservation is already in COMPLETED stage!"
    }

    def "should completed the reservation after processing it"(){
        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.COMPLETED)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.PROCESSING)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        def updateResponse = StaffActivityCreateAndUpdateDto.builder()
                .staffId(1L)
                .reservationId(1L)
                .workingDate(null)
                .workingStatus(WorkingStatus.COMPLETED)
                .startTime(null)
                .endTime(null)
                .consumerId(1L)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)
        reservationRepository.save(_) >> reservation

        reservationProducer.createNewActivityAndUpdateActivityStatus(_) >> updateResponse

        when:
        def response = reservationService.updateStatus(updateRequest)

        then:
        response.getStaffId() == 1
        response.getConsumerId() == 1
        response.getWorkingStatus() == WorkingStatus.COMPLETED
    }

    def "should throw exception while processing it before allocation"(){
        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.PROCESSING)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.INITIATED)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)

        when:
        reservationService.updateStatus(updateRequest)

        then:
        def exception = thrown(ReservationTerminatedOrCanceledException)
        exception.message == "You can't be process a reservation without allocated it before"
    }

    def "should throw exception while completed it before processing"(){
        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.COMPLETED)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.ALLOCATED)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)

        when:
        reservationService.updateStatus(updateRequest)

        then:
        def exception = thrown(ReservationTerminatedOrCanceledException)
        exception.message == "You can't be complete a reservation without processed it before"
    }

    def "should throw exception while re initiated since the reservation is already in initiated stage"(){
        given:
        def updateRequest = ReservationStatusUpdateAction.builder()
                .id(1L)
                .staffId(1L)
                .status(WorkingStatus.INITIATED)
                .build()

        def reservation = Reservation
                .builder()
                .id(1L)
                .startTime(null)
                .endTime(null)
                .services(null)
                .consumerId(1L)
                .workingStatus(WorkingStatus.INITIATED)
                .paymentMethod(PaymentMethod.CARD)
                .staffId(1L)
                .reservationDate(null)
                .totalPayableAmount(100.00)
                .build()

        reservationRepository.findById(1L) >> Optional.of(reservation)

        when:
        reservationService.updateStatus(updateRequest)

        then:
        def exception = thrown(ReservationTerminatedOrCanceledException)
        exception.message == "You can't re-initiated the reservation again!"
    }


}