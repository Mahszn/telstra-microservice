package au.com.telstra.simcardactivator;

import au.com.telstra.simcardactivator.records.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;

@RestController
public class ActivationController {
    private static final Logger log = LoggerFactory.getLogger(ActivationController.class);
    private final RestTemplate restTemplate;
    private static final String ACTUATOR_ENDPOINT = "http://localhost:8444/actuate";
    private final ActivationRecordRepository activationRecordRepository;

    public ActivationController(ActivationRecordRepository activationRecordRepository) { this.restTemplate = new RestTemplate();
        this.activationRecordRepository = activationRecordRepository;
    }

    @PostMapping("/activateSIM")
    public ResponseEntity<String> activateSim(@Valid @RequestBody ActivationRequest request) {
        ActuatorRequest actuatorRequest = new ActuatorRequest(request.iccid());
        ResponseEntity<ActuatorResponse> response;

        try {
            response = restTemplate.postForEntity(
                    ACTUATOR_ENDPOINT,
                    actuatorRequest,
                    ActuatorResponse.class
            );
        } catch (Exception e) {
            System.err.println("Error calling actuator microservice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                    "Actuator service is unavailable. Please try again later."
            );
        }

        if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null){
            boolean activationSuccess = response.getBody().success();
            System.out.println("Activation Success for ICCID " + request.iccid() + ": " + activationSuccess);

            ActivationRecord record = new ActivationRecord(
                    request.iccid(),
                    request.customerEmail(),
                    activationSuccess
            );
            activationRecordRepository.save(record);

            if(activationSuccess) {
                return ResponseEntity.ok("SIM activation succeeded for ICCID: " + request.iccid());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        "SIM activation failed for ICCID: " + request.iccid()
                );
            }
        } else {
            System.err.println("Unexpected response from actuator: " + response.getStatusCode());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                    "Failed to retrieve valid response from the actuator service."
            );
        }
    }

    @GetMapping("/querySIM")
    public ResponseEntity<SimQueryResponse> getSimRecord(@RequestParam(value = "simCardId", required = true) Long simCardId) {
        try {
            return activationRecordRepository.findById(simCardId).map(record -> {
                SimQueryResponse response = new SimQueryResponse(
                        record.getIccid(),
                        record.getCustomerEmail(),
                        record.isActive()
                );
                return ResponseEntity.ok(response);
            }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new SimQueryResponse("n/a", "n/a", false)
            ));
        } catch (Exception e) {
            log.error("Error occurred while retrieving SIM record for ID: {}", simCardId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new SimQueryResponse("n/a", "n/a", false)
            );
        }
    }
}
