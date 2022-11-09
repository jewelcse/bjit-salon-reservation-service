package com.bjit.salon.reservation.service.producer;


import com.bjit.salon.reservation.service.dto.producer.UpdateStatusProducer;
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

    private final KafkaTemplate<String, UpdateStatusProducer> template;

    public UpdateStatusProducer updateStatus(UpdateStatusProducer producer){
        Message<UpdateStatusProducer> message = MessageBuilder
                .withPayload(producer)
                .setHeader(KafkaHeaders.TOPIC,STAFF_NEW_ACTIVITY_TOPIC)
                .build();
        template.send(message);
        log.info("Published the Status Update Event: "+producer);
        return producer;
    }
}
