package com.lily.parcelhubcore.parcel.infrastructure.kafka.config;

import com.lily.parcelhubcore.parcel.infrastructure.kafka.intercepter.CustomProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerInterceptor<String, String> customProducerInterceptor() {
        return new CustomProducerInterceptor();
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(
            ProducerFactory<String, String> producerFactory,
            ProducerInterceptor<String, String> producerInterceptor
    ) {
        KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);
        template.setProducerInterceptor(producerInterceptor);
        return template;
    }
}
