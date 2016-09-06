package com.stormpath.tutorial.service;

import com.stormpath.tutorial.exception.UnauthorizedException;
import com.stormpath.tutorial.model.Account;
import com.stormpath.tutorial.model.AccountResponse;
import com.stormpath.tutorial.model.BaseResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.lang.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
public class AccountService {

    @Autowired
    SecretService secretService;

    public static final String USERNAME_CLAIM = "userName";

    private static final String BEARER_IDENTIFIER = "Bearer "; // space is important
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private Map<String, Account> accounts;

    @PostConstruct
    void setup() {
        accounts = new HashMap<>();
        accounts.put("anna", new Account("Anna", "Apple", "anna"));
        accounts.put("betty", new Account("Betty", "Baker", "betty"));
        accounts.put("colin", new Account("Colin", "Cooper", "colin"));
    }

    public AccountResponse getAccount(HttpServletRequest req) {
        Assert.notNull(req);

        // get JWT as Authorization header
        String authorization = req.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_IDENTIFIER)) {
            throw new UnauthorizedException("Missing or invalid Authorization header with Bearer type.");
        }

        String jwt = authorization.substring(BEARER_IDENTIFIER.length());

        return getAccount(jwt);
    }

    public AccountResponse getAccount(String jwt) {
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setStatus(BaseResponse.Status.ERROR);

        // verify JWT - will throw JWT Exception if not valid
        Jws<Claims> jws = Jwts.parser()
            .setSigningKeyResolver(secretService.getSigningKeyResolver())
            .parseClaimsJws(jwt);

        // get userName - throw if missing
        String userName;
        if ((userName = (String)jws.getBody().get(USERNAME_CLAIM)) == null) {
            throw new MissingClaimException(
                jws.getHeader(),
                jws.getBody(),
                "Required claim: '" + USERNAME_CLAIM + "' missing on the JWT"
            );
        }

        // see if it exists
        if (accounts.get(userName) == null) {
            String msg = "Account with " + USERNAME_CLAIM + ": " + userName + ", not found";
            log.warn(msg);
            accountResponse.setMessage(msg);
            return accountResponse;
        }

        accountResponse.setMessage("Found Account");
        accountResponse.setStatus(BaseResponse.Status.SUCCESS);
        accountResponse.setAccount(accounts.get(userName));

        return accountResponse;
    }
}
