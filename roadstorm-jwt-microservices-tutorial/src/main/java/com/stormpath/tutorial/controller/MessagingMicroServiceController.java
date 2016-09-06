package com.stormpath.tutorial.controller;

import com.stormpath.tutorial.model.JWTResponse;
import com.stormpath.tutorial.service.SpringBootKafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
public class MessagingMicroServiceController extends BaseController {

    @Autowired(required = false)
    SpringBootKafkaProducer springBootKafkaProducer;

    private static final Logger log = LoggerFactory.getLogger(MessagingMicroServiceController.class);

    @RequestMapping("/msg-account-request")
    public JWTResponse authBuilder(@RequestBody Map<String, Object> claims) throws ExecutionException, InterruptedException {
        String jwt = createJwt(claims);

        if (springBootKafkaProducer != null) {
            springBootKafkaProducer.send(jwt);
        } else {
            log.warn("Kafka is disabled.");
        }

        return new JWTResponse(jwt);
    }
}
