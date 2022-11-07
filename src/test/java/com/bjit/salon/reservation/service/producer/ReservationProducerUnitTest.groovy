package com.bjit.salon.reservation.service.producer

import com.bjit.salon.reservation.service.dto.producer.StaffActivityDto
import com.bjit.salon.reservation.service.entity.ReservationStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import spock.lang.Specification

import static com.bjit.salon.reservation.service.util.ConstraintsUtil.STAFF_NEW_ACTIVITY_TOPIC

@SpringBootTest
class ReservationProducerUnitTest extends Specification {

    private KafkaTemplate<String, StaffActivityDto> kafkaTemplate;
    private ReservationProducer producer;

    def setup(){
        kafkaTemplate = Mock()
        producer = new ReservationProducer(kafkaTemplate)
    }

    def "should update and create the staff new activity"() {
        given:
        def updateRequest = StaffActivityDto.builder()
                .staffId(1L)
                .reservationId(1L)
                .workingDate(null)
                .workingStatus(ReservationStatus.ALLOCATED)
                .startTime(null)
                .endTime(null)
                .consumerId(1L)
                .build()

        def message = MessageBuilder
                .withPayload(updateRequest)
                .setHeader(KafkaHeaders.TOPIC,STAFF_NEW_ACTIVITY_TOPIC)
                .build();

        kafkaTemplate.send(message) >> updateRequest

        when:
        def response = producer.createNewActivityAndUpdateActivityStatus(updateRequest)

        then:
        response.getConsumerId() == 1
        response.getReservationStatus() ==ReservationStatus.ALLOCATED
    }


}
