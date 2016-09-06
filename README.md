## Stormpath Java RoadStorm Tour Code

Check out the [Java RoadStorm Tour](http://roadstorm.afitnerd.com) landing page for tour dates and information.

The code included in this repo is the tutorial code used in talks on the Java RoadStorm Tour. There are two tutorials:

* [JJWT CSRF Tutorial](roadstorm-jwt-csrf-tutorial) - This shows how replacing the default CSRF handler for Spring Security with a custom handler that uses JWTs can enhance CSRF protection.
* [JJWT Microservices Tutorial](roadstorm-jwt-microservices-tutorial) - This is a demonstration of establishing trust between microservices using JWTs. It has both an HTTP mode and a messaging mode using Kafka.

## Resources

* [JJWT](https://github.com/jwtk/jjwt) - Java JWT library used in the tutorials
* [HTTPie](https://github.com/jkbrzt/httpie) - command line HTTP client (replaces curl)
* [Kafka](http://kafka.apache.org/) - Messaging system used in the microservices tutorial. Note: All you need to do is follow the [quickstart](http://kafka.apache.org/documentation.html#quickstart)

For more information, look at the README in each of the tutorials.