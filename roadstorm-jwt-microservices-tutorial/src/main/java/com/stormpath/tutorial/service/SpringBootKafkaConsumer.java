package com.stormpath.tutorial.service;

import com.stormpath.tutorial.model.AccountResponse;
import io.jsonwebtoken.JwtException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Properties;

@Service
@ConditionalOnProperty(name = "kafka.enabled", matchIfMissing = true)
public class SpringBootKafkaConsumer {

    @Value("${kafka.broker.address}")
    private String brokerAddress;

    @Value("${topic}")
    private String topic;

    @Autowired
    AccountService accountService;

    private Properties kafkaProps;
    private Consumer<String, String> consumer;

    private static final Logger log = LoggerFactory.getLogger(SpringBootKafkaConsumer.class);


    @PostConstruct
    public void init() {
        kafkaProps = new Properties();

        kafkaProps.put("bootstrap.servers", brokerAddress);

        kafkaProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProps.put("group.id", "consumer-tutorial");
        kafkaProps.put("acks", "1");

        kafkaProps.put("retries", "1");
        kafkaProps.put("linger.ms", 5);
    }

    public void consume() {
        log.info("Starting consumer...");

        consumer = new KafkaConsumer<>(kafkaProps);
        consumer.subscribe(Collections.singletonList(topic));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(1000);
            for (ConsumerRecord<String, String> record : records) {
                log.info("record offset: {}, record value: {}", record.offset(), record.value());
                AccountResponse accountResponse = null;
                try {
                    accountResponse = accountService.getAccount(record.value());
                } catch (JwtException e) {
                    log.error("Unable to get account: {}", e.getMessage());
                }
                if (accountResponse != null && accountResponse.getAccount() != null) {
                    log.info("Account name extracted from JWT: {}", accountResponse.getAccount().getFirstName() + " " + accountResponse.getAccount().getLastName());
                }
            }
        }
    }
}
