# Telstra SIM Card Activation Microservice

This microservice handles SIM card activation requests and stores activation records in a database. 
It integrates with an external Actuator service to perform the actual activation. 
You can then query the database to confirm whether a given SIM card was successfully activated.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Building and Running](#building-and-running)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)

## Prerequisites

- Java 17 or later
- Maven
- Spring Boot
- H2 Database

## Project Structure

The project is structured as follows:

```
telstra-microservice/
├── .idea/
├── .mvn/
├── data/
│   └── h2_db.mv.db
├── services/
│   └── SimCardActuator.jar
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── au.com.telstra.simcardactivator/
│   │   │       ├── records/
│   │   │       │   ├── ActivationRecord.java
│   │   │       │   ├── ActivationRequest.java
│   │   │       │   ├── ActuatorRequest.java
│   │   │       │   ├── ActuatorResponse.java
│   │   │       │   ├── SimQueryResponse.java
│   │   │       ├── ActivationController.java
│   │   │       ├── ActivationRecordRepository.java
│   │   │       ├── SimCardActivator.java
│   ├── test/
│   │   ├── java/
│   │   │   └── stepDefinitions/
│   │   │       └── SimCardActivatorStepDefinitions.java
│   │   ├── resources/
│   │       └── features/
│   │           └── sim_card_activator.feature
├── target/
├── .gitignore
├── application.properties
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## Building and Running

1. Start the Actuator service <br>
    In your terminal, navigate to the `services` directory and run the following command:

    ```bash
    java -jar SimCardActuator.jar
    ```
2. Start the microservice <br>
    In the root folder of the project, run the following command:<br>

    ```bash
    mvn spring-boot:run
    ```
   

## API Endpoints

1. POST /activateSIM <br>
    - Description: Activates a SIM card by sending an activation request to the Actuator service.
    - JSON Request Body Example:<br>
        ```json

        {
            "iccid": "1234567890123456789",
            "customerEmail": "bankai@example.com"
        }
      ```
    - Request Example:<br>
        ```bash
        curl -X POST -H "Content-Type: application/json" -d '{"iccid": "1234567890123456789", "customerEmail": "bankai@example.com"}' http://localhost:8080/activateSIM
        ```

2. GET /querySIM?simCardId={id} <br>
    - Description: Queries the database for an activation record by its ID.
    - Request Example:<br>
        ```bash
        curl -X GET http://localhost:8080/querySIM?simCardId=1
        ```
    - Response Example:<br>
        ```json
        {
            "iccid": "1234567890123456789",
            "customerEmail": "bankai@example.com",
            "activated": true
        }
      ```
      

## Testing

The project includes Cucumber BDD (Behavior Driven Development) for testing.

- A successful activation using ICCID `1255789453849037777` and email `"success@example.com"` is tested in the feature file.
- A failed activation using ICCID `8944500102198304826` and email `"fail@example.com"` is tested in the feature file.

**Steps**:

1. Ensure the Actuator service([SimCardActuator.jar](services/SimCardActuator.jar)) is running on port `8444`.
2. Ensure the microservice is running on port `8080`.
3. From the root project, run the following command:<br>

    ```bash
    mvn test
    ```
4. Cucumber will run the scenarios defined in `src/test/resources/features/sim_card_activator.feature` and verify the results.

**Expected**

- The first scenario (ICCID `1255789453849037777`) should succeed (`active = true`).
- The second scenario (ICCID `8944500102198304826`) should fail (`active = false`).
- Each scenario inserts a record into the database (ID `3` for the first scenario, `2` for the second, assuming no prior data).