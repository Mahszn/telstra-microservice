package au.com.telstra.simcardactivator;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class ActivationController {
    private final RestTemplate restTemplate;
    private static final String ACTUATOR_ENDPOINT = "http://localhost:8444/actuate";

    public ActivationController() { this.restTemplate = new RestTemplate(); }

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
}
