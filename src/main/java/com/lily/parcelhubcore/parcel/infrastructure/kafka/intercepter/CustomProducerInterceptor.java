package com.lily.parcelhubcore.parcel.infrastructure.kafka.intercepter;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.github.f4b6a3.ulid.UlidCreator;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomProducerInterceptor implements ProducerInterceptor<String, String> {
    private static final Logger log = LoggerFactory.getLogger(CustomProducerInterceptor.class);
    private AtomicLong successCount = new AtomicLong(0);
    private AtomicLong errorCount = new AtomicLong(0);

    /**
     * 消息发送前的预处理，为消息增加唯一id
     */
    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
        log.info("拦截器 - onSend 被调用，原始消息: {}", record.value());

        // 修改消息：为消息添加一个唯一id
        long now = System.currentTimeMillis();
        return new ProducerRecord<>(
                record.topic(),
                record.partition(),
                record.timestamp(),
                record.key(),
                record.value(),
                record.headers().add("uniqueId", UlidCreator.getMonotonicUlid().toString().getBytes())
        );
    }

    /**
     * 消息发送后（收到Broker确认或发生异常时）被调用
     */
    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        if (exception == null && metadata != null) {
            long count = successCount.incrementAndGet();
            log.info("拦截器 - 消息发送成功，topic: {}, partition: {}, offset: {}. 累计成功: {}",
                    metadata.topic(), metadata.partition(), metadata.offset(), count);
        } else {
            long count = errorCount.incrementAndGet();
            log.error("拦截器 - 消息发送失败，原因: {}. 累计失败: {}", exception.getMessage(), count);
        }
    }

    /**
     * 拦截器关闭时调用，用于释放资源。
     */
    @Override
    public void close() {
        log.info("拦截器关闭，最终统计：成功 = {}, 失败 = {}", successCount.get(), errorCount.get());
    }

    @Override
    public void configure(Map<String, ?> map) {
    }
}
