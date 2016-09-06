# Securing Microservices with JJWT Tutorial

The purpose of this tutorial is to demonstrate how the [JJWT](https://github.com/jwtk/jjwt) library can be used to secure microservices.

The only dependencies for interacting purely with HTTP are the Spring Boot Web Starter and the JJWT library.

There's also a messaging mode (disabled by default) that requires [Kafka](http://kafka.apache.org/documentation.html#quickstart). 
All you need to do is follow steps one and two in the Kafka quickstart to get it setup for use with this tutorial.

Wondering what JWTs and/or the JJWT library is all about? Click [here](https://java.jsonwebtoken.io).

## What Does the App Do?

This application demonstrates many of the critical functions of microservices that need to communicate with each other.

This includes:

* Creation of private/public key pair
* Registration of public key from one service to another service
* Creation of JWTs signed with private key
* Verification of JWTs using public key
* Example of Account Resolution Service using signed JWTs
* Example of JWT communication between microservices using Kafka messaging

## Building the App

Easy peasy:

```
mvn clean install
```

## Running the App 

To exercise the communication between microservices, you'll want to run at least two instances of the application.

Building the app creates a fully standalone executable jar. You can run multiple instances like so:

```
target/*.jar --server.port=8080 &
target/*.jar --server.port=8081 &
```

This will run one instance on port `8080` and one on `8081` and they will both be put in the background.

You can also use the purple Heroku button below to deploy to your own Heroku account. Setup two different instances
so you can communicate between them.

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)

## Service Registry

Note: all service to service communication examples below use [httpie](https://github.com/jkbrzt/httpie)

When the application is launched, a private/public keypair is automatically created. All operations involving keys
are handled via the `SecretService` service and exposed via endpoints in the `SecretServiceController`.

Below are the available endpoints from `SecretServiceController`:

1. `/refresh-my-creds` - Create a new private/public key pair for this microservice instance.
2. `/get-my-public-creds` - Return the Base64 URL Encoded version of this microservice instance's Public Key and its `kid`.
3. `/add-public-creds` - Register the Public Key of one microservice instance on another microservice instance.
4. `/test-build` - Returns a JWS signed with the instance's private key. The JWS includes the instance's `kid` as a header param.
5. `/test-parse` - Takes a JWS as a parameter and attempts to parse it by looking up the public key identified by the `kid`.

Let's look at this in action:

Let's first try to have one microservice communicate with the other *without* establishing trust:

`http localhost:8080/test-build`

    HTTP/1.1 200 OK
    Content-Type: application/json;charset=UTF-8
    Date: Mon, 18 Jul 2016 04:42:09 GMT
    Server: Apache-Coyote/1.1
    Transfer-Encoding: chunked
    
    {
        "jwt": "eyJraWQiOiI5NzYzMWI5YS0yZjM0LTRhYzQtOGMxYy1kN2U3MmZkYTExMGYiLCJhbGciOiJSUzI1NiJ9...",
        "status": "SUCCESS"
    }

`http localhost:8081/test-parse?jwt=eyJraWQiOiI5NzYzMWI5YS0yZjM0LTRhYzQtOGMxYy1kN2U3MmZkYTExMGYiLCJhbGciOiJSUzI1NiJ9...`

    HTTP/1.1 400 Bad Request
    Connection: close
    Content-Type: application/json;charset=UTF-8
    Date: Mon, 18 Jul 2016 04:42:32 GMT
    Server: Apache-Coyote/1.1
    Transfer-Encoding: chunked
    
    {
        "exceptionType": "io.jsonwebtoken.JwtException",
        "message": "No public key registered for kid: 97631b9a-2f34-4ac4-8c1c-d7e72fda110f. JWT claims: {iss=Stormpath, sub=msilverman, name=Micah Silverman, hasMotorcycle=true, iat=1466796822, exp=4622470422}",
        "status": "ERROR"
    }
    
Notice that our second microservice cannot parse the JWT since it doesn't have the public key in its registry.

Now, let's register the first microservice's public key with the second microservice and then try the above operation again:

`http localhost:8080/get-my-public-creds`

    HTTP/1.1 200 OK
    Content-Type: application/json;charset=UTF-8
    Date: Mon, 18 Jul 2016 04:47:26 GMT
    Server: Apache-Coyote/1.1
    Transfer-Encoding: chunked
    
    {
        "b64UrlPublicKey": "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCo6Lfrn...",
        "kid": "97631b9a-2f34-4ac4-8c1c-d7e72fda110f"
    }
    
```
http POST localhost:8081/add-public-creds \
  b64UrlPublicKey="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCo6Lfrn..." \
  kid="97631b9a-2f34-4ac4-8c1c-d7e72fda110f"
```

    HTTP/1.1 200 OK
    Content-Type: application/json;charset=UTF-8
    Date: Mon, 18 Jul 2016 04:51:25 GMT
    Server: Apache-Coyote/1.1
    Transfer-Encoding: chunked
    
    {
        "b64UrlPublicKey": "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCo6Lfrn...",
        "kid": "97631b9a-2f34-4ac4-8c1c-d7e72fda110f"
    }
    
Now, we can re-run our `/test-parse` endpoint using the same JWT from before:
    
`http localhost:8081/test-parse?jwt=eyJraWQiOiI5NzYzMWI5YS0yZjM0LTRhYzQtOGMxYy1kN2U3MmZkYTExMGYiLCJhbGciOiJSUzI1NiJ9...`
    
    HTTP/1.1 200 OK
    Content-Type: application/json;charset=UTF-8
    Date: Mon, 18 Jul 2016 04:52:47 GMT
    Server: Apache-Coyote/1.1
    Transfer-Encoding: chunked
    
    {
        "jws": {
            "body": {
                "exp": 4622470422,
                "hasMotorcycle": true,
                "iat": 1466796822,
                "iss": "Stormpath",
                "name": "Micah Silverman",
                "sub": "msilverman"
            },
            "header": {
                "alg": "RS256",
                "kid": "97631b9a-2f34-4ac4-8c1c-d7e72fda110f"
            },
            "signature": "phsExAX5CflcLJJQ-q4xYEOq9gbtu7DxzokMq_yPKz2Bx-TQz72EdG25HssNGnkiOCCDVH7iSnaARoiIBPgRKj4W8FstVBR1I3hreIS4MrqMZBaDrS62xwyVnCU1HIMvsqOj6hHBwIowQwlTld887C1hznpTjk74Q1__Vk_wZJU"
        },
        "status": "SUCCESS"
    }
    
This time, our second microservice is able to parse the JWT from the first microservice since we registered the public key with it.

## Account Resolution

In this part of the tutorial, we introduce an `AccountResolver`. This interface exposes an `INSTANCE` that can then be used to lookup an `Account`.
For the purposes of the tutorial, three accounts are setup that represent the "database" of accounts.

The `AccountResolver` implementation expects a JWT that has a `userName` claim that will be used to lookup the account.

The microservice that is doing the account resolution will need to retrieve the bearer token from the request (the JWT) and it will need to be able to parse the JWT to pull out the `userName` claim. 
Like before, the public key of the microservice that created the JWT will need to be registered with the microservice that will be parsing the JWT.
 
The `MicroServiceController` exposes two endpoints to manage these interactions:

1. `/account-request` - Generate a JWT with a 60-second expiration. It can take in any number of claims. `userName` claim is required.
2. `/restricted` - Return an `Account` based on processing a bearer token
 
Let's see this in action. Note: this assumes that you've registered the public key from the first microservice with the second microservice.

`http POST localhost:8080/account-request username=anna`

    HTTP/1.1 200 OK
    Content-Type: application/json;charset=UTF-8
    Date: Mon, 18 Jul 2016 05:13:56 GMT
    Server: Apache-Coyote/1.1
    Transfer-Encoding: chunked
    
    {
        "jwt": "eyJraWQiOiI5NzYzMWI5YS0yZjM0LTRhYzQtOGMxYy1kN2U3MmZkYTExMGYiLCJhbGciOiJSUzI1NiJ9...",
        "status": "SUCCESS"
    }
    
`http localhost:8081/restricted Authorization:"Bearer eyJraWQiOiI5NzYzMWI5YS0yZjM0LTRhYzQtOGMxYy1kN2U3MmZkYTExMGYiLCJhbGciOiJSUzI1NiJ9..."`

    HTTP/1.1 200 OK
    Content-Type: application/json;charset=UTF-8
    Date: Mon, 18 Jul 2016 05:16:26 GMT
    Server: Apache-Coyote/1.1
    Transfer-Encoding: chunked
    
    {
        "account": {
            "firstName": "Anna",
            "lastName": "Apple",
            "userName": "anna"
        },
        "message": "Found Account",
        "status": "SUCCESS"
    }
    
The above request uses the standard `Authorization` header as part of the request to the second microservice using the JWT from the first microservice.

## Microservice Communication with messages

While the HTTP examples above are simple, HTTP just isn't a good protocol for microservice communication.

It's a synchronous protocol that is easily overwhelmed (Think [DDOS](https://en.wikipedia.org/wiki/Denial-of-service_attack)).

[Apache Kafka](http://kafka.apache.org/) is a popular, highly scalable pub/sub messaging platform with robust libraries in Java.

Follow these steps to use the messaging mode of the sample app:

1. Setup Kafka

    An exhaustive discussion of Kafka is outside the scope of this tutorial.
    However, if you follow the first two steps of the [quickstart](http://kafka.apache.org/documentation.html#quickstart), you'll have a local environment that's ready for this tutorial to work with.
    
2. Configure the tutorial

    This is the `application.properties` file of this tutorial:
    
    ```
    kafka.enabled=false
    kafka.broker.address=localhost:9092
    zookeeper.address=localhost:2181
    topic=micro-services
    ```
    
    Simply change the first line to: `kafka.enabled=true`
    
    **Note**: You will get lots of error output if Kafka is enabled in the tutorial application, but it is not running on your machine.

3. Build the tutorial

    Just like before:
    
    `mvn clean install`

4. Run the tutorial app

    Open up two terminal windows. In one, run:
    
    `target/*.jar --server.port=8080`
    
    You'll notice some new output from Kafka. This microservice will be producing messages.
    
    In the second terminal window, run:
    
    `target/*.jar --server.port=8081 --kafka.consumer.enabled=true`
    
    This microservice will be consuming messages.
    
    
5. Exercise the application

    1. `http localhost:8080/msg-account-request userName=anna`
    
        In the `8080` terminal window, you will see a response like this:
        
        ```
        HTTP/1.1 200
        Content-Type: application/json;charset=UTF-8
        Date: Tue, 23 Aug 2016 16:59:30 GMT
        Transfer-Encoding: chunked
        
        {
            "jwt": "eyJraWQiOiI2YjllZTE5YS1mMTc0LTRjNzctYWE5Ni05MjJhYmE4YTc4NzkiLCJhbGciOiJSUzI1NiJ9.eyJ1c2VyTmFtZSI6ImFubmEiLCJpYXQiOjE0NzE5NzE1NjksIm5iZiI6MTQ3MTk3MTU2OSwiZXhwIjoxNDcxOTcxNjI5fQ.Tmf934D_H_Kuz5NxqYBbZfkR0PhYBB0pNdSx8cycP712xdtXz0vUqEJHNN-RQeN1Gwu6CiKc4FUEQIRap0AhfIbFNfs5bjdJODRKGasPGFhT2hbTU8zpF43Z3DujX4mXrS4eEUVpdTMWxc2ISvR_UvfwvwwcVO-pgTqjz8WCdqk",
            "status": "SUCCESS"
        }
        ```
        
        That's the JWT request that was created. The JWT is sent as a message which is picked up by the `8081` consumer.
        
        In the `8081` terminal window, you will see a response like this:
        
        ```
        INFO  record offset: 12, record value: eyJraWQiOiI2YjllZTE5YS1mMTc0LTRjNzctYWE5Ni05MjJhYmE4YTc4NzkiLCJhbGciOiJSUzI1NiJ9.eyJ1c2VyTmFtZSI6ImFubmEiLCJpYXQiOjE0NzE5NzE1NjksIm5iZiI6MTQ3MTk3MTU2OSwiZXhwIjoxNDcxOTcxNjI5fQ.Tmf934D_H_Kuz5NxqYBbZfkR0PhYBB0pNdSx8cycP712xdtXz0vUqEJHNN-RQeN1Gwu6CiKc4FUEQIRap0AhfIbFNfs5bjdJODRKGasPGFhT2hbTU8zpF43Z3DujX4mXrS4eEUVpdTMWxc2ISvR_UvfwvwwcVO-pgTqjz8WCdqk
        ERROR Unable to get account: No public key registered for kid: 6b9ee19a-f174-4c77-aa96-922aba8a7879. JWT claims: {userName=anna, iat=1471971569, nbf=1471971569, exp=1471971629}
        ```
        
        The good news is that the consumer got the message. The bad news is that the `8081` microservice doesn't trust the `8080` microservice. That is, the `8080` microservice has not registered its public key with the `8081` microservice, so there's no way for it to verify the signature of the JWT that was created with teh `8080` microservice's private key.
        
    2. Establish trust
    
        Just like before, do:
       
        `http localhost:8080/get-my-public-creds`
       
        Take the data from that response and add the public key to the other microservice:
       
        ```
        http POST localhost:8081/add-public-creds \
          b64UrlPublicKey=<b64UrlPublicKey from previous request> \
          kid=<kid from previous request>
        ```

    3. Again: `http localhost:8080/msg-account-request userName=anna`
    
        This time, you will see a log messages like this on the `8081` microservice:
        
        ```
        INFO record offset: 13, record value: eyJraWQiOiI2YjllZTE5YS1mMTc0LTRjNzctYWE5Ni05MjJhYmE4YTc4NzkiLCJhbGciOiJSUzI1NiJ9.eyJ1c2VyTmFtZSI6ImFubmEiLCJpYXQiOjE0NzE5NzIyMDIsIm5iZiI6MTQ3MTk3MjIwMiwiZXhwIjoxNDcxOTcyMjYyfQ.3C2tz_PgIzkMXZMoDTLyPgxfZUQbsK6crnwc1Fu3-5btJKDV4nnq6S07wFwGNhksD365jOAF7NSHSWo8PNfHR9XPQQXhKVmkdnTCr9XO1cZTHdsUo2yH3TWvLxT2i7a4QxTvGHFcxsookX5cOUCGaT4gq5PeeN-1TRE22Xd2Di8
        INFO Account name extracted from JWT: Anna Apple
        ```
        
        Now, the consumer is able to verify the signature on the incoming JWT and it does an account lookup based on the `userName` claim