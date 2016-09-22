package io.jsonwebtoken.jjwtfun.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    @RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.ALL_VALUE)
    public @ResponseBody String restHome(HttpServletRequest req) {
        String requestUrl = getUrl(req);
        return "Available commands (assumes httpie - https://github.com/jkbrzt/httpie):\n\n" +
            "  http " + requestUrl + "/\n\tThis usage message\n\n" +
            "  http " + requestUrl + "/static-builder\n\tbuild JWT from hardcoded claims\n\n" +
            "  http POST " + requestUrl + "/dynamic-builder-general claim-1=value-1 ... [claim-n=value-n]\n\tbuild JWT from passed in claims (using general claims map)\n\n" +
            "  http POST " + requestUrl + "/dynamic-builder-specific claim-1=value-1 ... [claim-n=value-n]\n\tbuild JWT from passed in claims (using specific claims methods)\n\n" +
            "  http POST " + requestUrl + "/dynamic-builder-compress claim-1=value-1 ... [claim-n=value-n]\n\tbuild DEFLATE compressed JWT from passed in claims\n\n" +
            "  http " + requestUrl + "/parser?jwt=<jwt>\n\tParse passed in JWT\n\n" +
            "  http " + requestUrl + "/parser-enforce?jwt=<jwt>\n\tParse passed in JWT enforcing the 'iss' registered claim and the 'hasMotorcycle' custom claim\n\n" +
            "  http " + requestUrl + "/get-secrets\n\tShow the signing keys currently in use.\n\n" +
            "  http " + requestUrl + "/refresh-secrets\n\tGenerate new signing keys and show them.\n\n" +
            "  http POST " + requestUrl + "/set-secrets HS256=base64-encoded-value HS384=base64-encoded-value HS512=base64-encoded-value\n\tExplicitly set secrets to use in the application.";
    }

    @RequestMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE, consumes = MediaType.ALL_VALUE)
    public String home() {
        return "redirect:/jwt-csrf-form";
    }

    private String getUrl(HttpServletRequest req) {
        return req.getScheme() + "://" +
            req.getServerName() +
            ((req.getServerPort() == 80 || req.getServerPort() == 443) ? "" : ":" + req.getServerPort());
    }
}
