package com.stormpath.tutorial.service;

import com.stormpath.tutorial.model.PublicCreds;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SigningKeyResolver;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.jsonwebtoken.impl.TextCodec;
import io.jsonwebtoken.impl.crypto.RsaProvider;
import io.jsonwebtoken.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SecretService {

    private static final Logger log = LoggerFactory.getLogger(SecretService.class);

    private KeyPair myKeyPair;
    private String kid;

    private Map<String, PublicKey> publicKeys = new HashMap<>();

    @PostConstruct
    public void setup() {
        refreshMyCreds();
    }

    private SigningKeyResolver signingKeyResolver = new SigningKeyResolverAdapter() {
        @Override
        public Key resolveSigningKey(JwsHeader header, Claims claims) {
            String kid = header.getKeyId();
            if (!Strings.hasText(kid)) {
                throw new JwtException("Missing required 'kid' header param in JWT with claims: " + claims);
            }
            Key key = publicKeys.get(kid);
            if (key == null) {
                throw new JwtException("No public key registered for kid: " + kid + ". JWT claims: " + claims);
            }
            return key;
        }
    };

    public SigningKeyResolver getSigningKeyResolver() {
        return signingKeyResolver;
    }

    public PublicCreds getPublicCreds(String kid) {
        return createPublicCreds(kid, publicKeys.get(kid));
    }

    public PublicCreds getMyPublicCreds() {
        return createPublicCreds(this.kid, myKeyPair.getPublic());
    }

    private PublicCreds createPublicCreds(String kid, PublicKey key) {
        return new PublicCreds(kid, TextCodec.BASE64URL.encode(key.getEncoded()));
    }

    // do not expose in controllers
    public PrivateKey getMyPrivateKey() {
        return myKeyPair.getPrivate();
    }

    public PublicCreds refreshMyCreds() {
        myKeyPair = RsaProvider.generateKeyPair(1024);
        kid = UUID.randomUUID().toString();

        PublicCreds publicCreds = getMyPublicCreds();

        // this microservice will trust itself
        addPublicCreds(publicCreds);

        return publicCreds;
    }

    public void addPublicCreds(PublicCreds publicCreds) {
        byte[] encoded = TextCodec.BASE64URL.decode(publicCreds.getB64UrlPublicKey());

        PublicKey publicKey = null;
        try {
            publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Unable to create public key: {}", e.getMessage(), e);
        }

        publicKeys.put(publicCreds.getKid(), publicKey);
    }
}
