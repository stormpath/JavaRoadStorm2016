package com.stormpath.tutorial.service;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Service
@ConditionalOnProperty(name = "kafka.enabled", matchIfMissing = true)
public class SpringBootKafkaProducer {

    @Value("${kafka.broker.address}")
    private String brokerAddress;

    @Value("${topic}")
    private String topic;

    private Producer<String, String> producer;

    @PostConstruct
    public void init() {
        Properties kafkaProps = new Properties();

        kafkaProps.put("bootstrap.servers", brokerAddress);

        kafkaProps.put("key.serializer",
            "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("value.serializer",
            "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("acks", "1");

        kafkaProps.put("retries", "1");
        kafkaProps.put("linger.ms", 5);

        producer = new KafkaProducer<>(kafkaProps);
    }

    public void send(String value) throws ExecutionException, InterruptedException {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, value);

        producer.send(record, (RecordMetadata recordMetadata, Exception e) -> {
            if (e != null) {
                e.printStackTrace();
            }
        });
    }
}