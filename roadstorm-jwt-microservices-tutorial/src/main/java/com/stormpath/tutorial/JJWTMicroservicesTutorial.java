package com.stormpath.tutorial;

import com.stormpath.tutorial.service.SpringBootKafkaConsumer;
import kafka.admin.AdminUtils;
import kafka.common.TopicExistsException;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

@SpringBootApplication
public class JJWTMicroservicesTutorial {

    @Value("${topic}")
    private String topic;

    @Value("${zookeeper.address}")
    private String zookeeperAddress;

    private static final Logger log = LoggerFactory.getLogger(JJWTMicroservicesTutorial.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(JJWTMicroservicesTutorial.class, args);

        boolean shouldConsume = context
            .getEnvironment()
            .getProperty("kafka.consumer.enabled", Boolean.class, Boolean.FALSE);


        if (shouldConsume && context.containsBean("springBootKafkaConsumer")) {
            SpringBootKafkaConsumer springBootKafkaConsumer =
                context.getBean("springBootKafkaConsumer", SpringBootKafkaConsumer.class);

            springBootKafkaConsumer.consume();
        }
    }

    @Bean
    @ConditionalOnProperty(name = "kafka.enabled", matchIfMissing = true)
    public TopicCreator topicCreator() {
        return new TopicCreator(this.topic, this.zookeeperAddress);
    }

    private static class TopicCreator implements SmartLifecycle {

        private final String topic;

        private final String zkAddress;

        private volatile boolean running;

        public TopicCreator(String topic, String zkAddress) {
            this.topic = topic;
            this.zkAddress = zkAddress;
        }

        @Override
        public void start() {
            ZkUtils zkUtils = new ZkUtils(
                new ZkClient(this.zkAddress, 6000, 6000, ZKStringSerializer$.MODULE$), null, false
            );
            try {
                AdminUtils.createTopic(zkUtils, topic, 1, 1, new Properties());
            } catch (TopicExistsException e) {
                log.info("Topic: {} already exists.", topic);
            }
            this.running = true;
        }

        @Override
        public void stop() {}

        @Override
        public boolean isRunning() {
            return this.running;
        }

        @Override
        public int getPhase() {
            return Integer.MIN_VALUE;
        }

        @Override
        public boolean isAutoStartup() {
            return true;
        }

        @Override
        public void stop(Runnable callback) {
            callback.run();
        }

    }
}
