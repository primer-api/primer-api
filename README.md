# tokeniser

## Primer code challenge

This project is a code challenge for a software engineer position at Primer.

### Summary

- Create an API using your language and framework of choice to tokenise card payment information (if you are unfamiliar with card tokenisation, just look it up :))
- Use this token to process a transaction against a Braintree sandbox account (feel free to use any other processor of your choice - Braintree is easy to get started with, as is Stripe)
- Share your work privately via Github (https://github.com/primer-api)

API endpoints:

- tokenise (card number, expiry date): returns a token
- sale (token, transaction amount): submits the card payment information to your processor sandbox and returns a basic abstracted response payload

### Solution

- This project is a Spring boot application in a microservice structure. It was generated using JHipster and needs the a registry for service discovery and authentication.
- There are 2 entities: CreditCard and Token.
- To tokenise I am using the SecureRandom java API that generate secure, unpredictable and evenly distributed the following pseudo random number generators (PRNGs)
- For the sale I am using Braintree in sandbox mode
- The main implementation logic it's at `com.primer.tokeniser.service.ApiService`

#### Technical decisions and backlog for improvements

- I've decided to generate a random token with numbers and keeping the same length to make easier to client integration
- The level of security depends on the PAN length because the token is truncate to keep the same length. For example, cards with 16 digits the token has the size of 10^16.
- To block requests after X failed attempts from the same IP with some validation on network level to prevent brute force
- Token must have a expiration time. This feature won't be included on the MVP but it should come on the next version
- I am using JHipster to make it easy and fast to generate a production ready application that is secure and reliable.

### How to run

> :warning: **DISCLAIMER** For credit card number and amount use the values for testing from the Braintree documentation https://developers.braintreepayments.com/reference/general/testing/java

##### 1. Set the configuration for Braintree API using environment variables

```shell script
export BRAINTREE_MERCHANT_ID=<your-merchant-id>
export BRAINTREE_PUBLIC_KEY=<your-sandbox-public-key>
export BRAINTREE_PRIVATE_KEY=<your-sandbox-private-key>
```

##### 2. Run `docker-compose -f src/main/docker/app.yml up`

##### 3. Generate a access token

```shell script
curl --location --request POST 'http://localhost:8761/api/authenticate' \
--header 'Content-Type: application/json' \
--data-raw '{
  "password": "admin",
  "rememberMe": true,
  "username": "admin"
}'
```

##### 4. Tokenize a credit card using the bearer token from the previous step. It will return the token in a string format.

```shell script
curl --location --request POST 'http://localhost:8081/api/tokenise' \
--header 'Authorization: Bearer <token-from-the-previous-step>' \
--header 'Content-Type: application/json' \
--data-raw '{
    "number": "4012000033330026",
    "expiration_date": "01/22"
}'
```

##### 5. Execute a sale using the token

```shell script
curl --location --request POST 'http://localhost:8081/api/sale' \
--header 'Authorization: Bearer <your-token>' \
--header 'Content-Type: application/json' \
--data-raw '{
    "token": "2679045129299883",
    "amount": "5002.00"
}'
```

## JHipster

This application was generated using JHipster 6.10.1, you can find documentation and help at [https://www.jhipster.tech/documentation-archive/v6.10.1](https://www.jhipster.tech/documentation-archive/v6.10.1).

This is a "microservice" application intended to be part of a microservice architecture, please refer to the [Doing microservices with JHipster][] page of the documentation for more information.

This application is configured for Service Discovery and Configuration with . On launch, it will refuse to start if it is not able to connect to .

## Development

To start your application in the dev profile, run:

```
./mvnw
```

For further instructions on how to develop with JHipster, have a look at [Using JHipster in development][].

## Building for production

### Packaging as jar

To build the final jar and optimize the tokeniser application for production, run:

```

./mvnw -Pprod clean verify


```

To ensure everything worked, run:

```

java -jar target/*.jar


```

Refer to [Using JHipster in production][] for more details.

### Packaging as war

To package your application as a war in order to deploy it to an application server, run:

```

./mvnw -Pprod,war clean verify


```

## Testing

To launch your application's tests, run:

```
./mvnw verify
```

For more information, refer to the [Running tests page][].

### Code quality

Sonar is used to analyse code quality. You can start a local Sonar server (accessible on http://localhost:9001) with:

```
docker-compose -f src/main/docker/sonar.yml up -d
```

You can run a Sonar analysis with using the [sonar-scanner](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner) or by using the maven plugin.

Then, run a Sonar analysis:

```
./mvnw -Pprod clean verify sonar:sonar
```

If you need to re-run the Sonar phase, please be sure to specify at least the `initialize` phase since Sonar properties are loaded from the sonar-project.properties file.

```
./mvnw initialize sonar:sonar
```

For more information, refer to the [Code quality page][].

## Using Docker to simplify development (optional)

You can use Docker to improve your JHipster development experience. A number of docker-compose configuration are available in the [src/main/docker](src/main/docker) folder to launch required third party services.

For example, to start a mysql database in a docker container, run:

```
docker-compose -f src/main/docker/mysql.yml up -d
```

To stop it and remove the container, run:

```
docker-compose -f src/main/docker/mysql.yml down
```

You can also fully dockerize your application and all the services that it depends on.
To achieve this, first build a docker image of your app by running:

```
./mvnw -Pprod verify jib:dockerBuild
```

Then run:

```
docker-compose -f src/main/docker/app.yml up -d
```

For more information refer to [Using Docker and Docker-Compose][], this page also contains information on the docker-compose sub-generator (`jhipster docker-compose`), which is able to generate docker configurations for one or several JHipster applications.

## Continuous Integration (optional)

To configure CI for your project, run the ci-cd sub-generator (`jhipster ci-cd`), this will let you generate configuration files for a number of Continuous Integration systems. Consult the [Setting up Continuous Integration][] page for more information.

[jhipster homepage and latest documentation]: https://www.jhipster.tech
[jhipster 6.10.1 archive]: https://www.jhipster.tech/documentation-archive/v6.10.1
[doing microservices with jhipster]: https://www.jhipster.tech/documentation-archive/v6.10.1/microservices-architecture/
[using jhipster in development]: https://www.jhipster.tech/documentation-archive/v6.10.1/development/
[using docker and docker-compose]: https://www.jhipster.tech/documentation-archive/v6.10.1/docker-compose
[using jhipster in production]: https://www.jhipster.tech/documentation-archive/v6.10.1/production/
[running tests page]: https://www.jhipster.tech/documentation-archive/v6.10.1/running-tests/
[code quality page]: https://www.jhipster.tech/documentation-archive/v6.10.1/code-quality/
[setting up continuous integration]: https://www.jhipster.tech/documentation-archive/v6.10.1/setting-up-ci/
