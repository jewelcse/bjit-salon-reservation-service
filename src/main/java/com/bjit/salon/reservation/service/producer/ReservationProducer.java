package com.bjit.salon.reservation.service.producer;


import com.bjit.salon.reservation.service.dto.producer.StaffActivityCreateAndUpdateDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import static com.bjit.salon.reservation.service.util.Utils.STAFF_NEW_ACTIVITY_TOPIC;

@RequiredArgsConstructor
@Service
public class ReservationProducer {
    private final static Logger log = LoggerFactory.getLogger(ReservationProducer.class);

    private final KafkaTemplate<String, StaffActivityCreateAndUpdateDto> kafkaTemplate;

    public StaffActivityCreateAndUpdateDto createNewActivityAndUpdateActivityStatus(StaffActivityCreateAndUpdateDto staffActivity){
        Message<StaffActivityCreateAndUpdateDto> message = MessageBuilder
                .withPayload(staffActivity)
                .setHeader(KafkaHeaders.TOPIC,STAFF_NEW_ACTIVITY_TOPIC)
                .build();
        kafkaTemplate.send(message);
        log.info("producer: "+staffActivity);
        return staffActivity;
    }
}
