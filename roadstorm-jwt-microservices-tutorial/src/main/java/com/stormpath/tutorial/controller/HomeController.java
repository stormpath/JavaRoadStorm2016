package com.stormpath.tutorial.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class HomeController {

    @RequestMapping("/")
    public String home(
        HttpServletRequest req,
        @RequestParam(required = false) String localUrl, @RequestParam(required = false) String remoteUrl
    ) {
        if (localUrl == null || remoteUrl == null) {
            localUrl = getUrl(req);
            remoteUrl = "<other microservice url>";
        }

        return "Available commands (assumes httpie - https://github.com/jkbrzt/httpie):\n\n" +
            "Key Management Endpoints:\n" +
            "  Use these endpoints to manage local key pairs and to establish trust with other microservices.\n\n" +
            "  http " + localUrl + "/\n\tThis usage message\n\n" +
            "  http " + localUrl + "/ localUrl==<url reference> remoteUrl==<url reference>\n\tThis usage message, with a local and remote url plugged in to show usages between microservices\n\n" +
            "  http " + localUrl + "/refresh-my-creds\n\tCreate a new private/public key pair for this microservice and return the public credentials\n\n" +
            "  http " + localUrl + "/get-my-public-creds\n\tReturn the base64 url encoded public key and key id for this microservice\n\n" +
            "  http POST " + remoteUrl + "/add-public-creds b64UrlPublicKey=<base64 url encoded public key> kid=<key id associated with the public key>\n\tAdd the public credentials from one microservice to another microservice\n\n" +
            "Trust testing endpoints:\n" +
            "  Use these endpoints to test the trust between microservices\n\n" +
            "  http " + localUrl + "/test-build\n\tReturn a JWT from this microservice\n\n" +
            "  http " + remoteUrl + "/test-parse?jwt=<jwt from /test-build from this microservice>\n\tSend the JWT from /test-build from one microservice to be parsed on another microservice\n\n" +
            "Microservice Authorization endpoints:\n" +
            "  Use these endpoints to exercise trusted communication between microservices\n\n" +
            "  http " + localUrl + "/account-request userName=<userName to look up on another microservice>\n\tReturn a JWT to be sent to another microservice\n\n" +
            "  http " + localUrl + "/msg-account-request userName=<userName to look up on another microservice>\n\tReturn a JWT to be sent to another microservice\n\tAlso, publish JWT as a message. The message will be picked up by the consumer running on the other microservice.\n\tNOTE: You must be running Kafka for this functionality.\n\n" +
            "  http " + remoteUrl + "/restricted Authorization:\"Bearer <JWT from /auth-builder from this microservice>\"\n\tReturn the search results for the userName\n\n";
    }

    private String getUrl(HttpServletRequest req) {
        return req.getScheme() + "://" +
            req.getServerName() +
            ((req.getServerPort() == 80 || req.getServerPort() == 443) ? "" : ":" + req.getServerPort());
    }
}
