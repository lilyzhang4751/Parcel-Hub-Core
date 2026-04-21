package com.lily.parcelhubcore.parcel.infrastructure.kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {

    public static final String TOPIC_PARCEL_NOTIFY = "parcel.notify";
    public static final String TOPIC_PARCEL_OP_SYNC = "parcel.op.sync";

    @Bean
    public KafkaAdmin.NewTopics parcelTopics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(TOPIC_PARCEL_NOTIFY).partitions(3).replicas(1).build(),
                TopicBuilder.name(TOPIC_PARCEL_OP_SYNC).partitions(3).replicas(1).build()
        );
    }
}
