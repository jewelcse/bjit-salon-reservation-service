package com.bjit.salon.reservation.service.config;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import static com.bjit.salon.reservation.service.util.ConstraintsUtil.STAFF_NEW_ACTIVITY_TOPIC;

@Configuration
public class TopicConfiguration {

    @Bean
    public NewTopic createNewActivityTopic() {
        return TopicBuilder
                .name(STAFF_NEW_ACTIVITY_TOPIC)
                .build();
    }
}
