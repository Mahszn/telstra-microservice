package stepDefinitions;

import au.com.telstra.simcardactivator.SimCardActivator;
import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = SimCardActivator.class, loader = SpringBootContextLoader.class)
public class SimCardActivatorStepDefinitions {
    private final RestTemplate restTemplate = new RestTemplate();

    private String iccid;
    private String customerEmail;
    private ResponseEntity<String> postResponse;
    private ResponseEntity<String> getResponse;

    @Given("I have a SIM card with ICCID {string} and email {string}")
    public void i_have_a_sim_card_with_iccid_and_email(String iccid, String email) {
        this.iccid = iccid;
        this.customerEmail = email;
    }

    @When("I send a POST request to /activateSIM")
    public void i_send_a_post_request_to_activate_sim() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("iccid", iccid);
        requestBody.put("customerEmail", customerEmail);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        postResponse = restTemplate.postForEntity(
                "http://localhost:8080/activateSIM",
                requestEntity,
                String.class
        );
    }

    @Then("the SIM card activation should be successful")
    public void the_sim_card_activation_should_be_successful() {
        Assertions.assertEquals(
                HttpStatus.OK,
                postResponse.getStatusCode(),
                "SIM card activation failed with status code: " + postResponse.getStatusCode()
        );

        String body = postResponse.getBody();
        Assertions.assertNotNull(body, "Response body should not be null.");
        Assertions.assertTrue(
                body.contains("succeeded"),
                "Response body should indicate success. Actual: " + body
        );
    }

    @Then("the SIM card activation should fail")
    public void the_sim_card_activation_should_fail() {
        HttpStatus status = (HttpStatus) postResponse.getStatusCode();
        Assertions.assertTrue(
                status.is4xxClientError() || status.is5xxServerError(),
                "Expected 4xx/5xx for failed activation, got: " + status
        );

        String body = postResponse.getBody();
        Assertions.assertNotNull(body, "Response body should not be null.");
        Assertions.assertTrue(
                body.contains("failed") || body.contains("fail"),
                "Response body should indicate failure. Actual: " + body
        );
    }

    @Then("a new record should be saved in the database with id {long}")
    public void a_new_record_should_be_saved_in_the_database_with_id(Long expectedId) {
        Assertions.assertNotNull(expectedId, "Expected ID should not be null");
    }

    @When("I query for the record with id {long}")
    public void i_query_for_the_record_with_id(Long recordId) {
        // GET request to /querySIM?simCardId={recordId}
        String url = "http://localhost:8080/querySIM?simCardId=" + recordId;
        getResponse = restTemplate.getForEntity(url, String.class);
    }

    @Then("the activation status should be {string}")
    public void the_activation_status_should_be(String expectedStatus) throws Exception {
        Assertions.assertEquals(
                HttpStatus.OK, getResponse.getStatusCode(),
                "Expected 200 OK when querying an existing record. Actual: " + getResponse.getStatusCode()
        );

        String responseBody = getResponse.getBody();
        Assertions.assertNotNull(responseBody, "Query response body should not be null.");

        JsonNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(responseBody);
        // The JSON structure should be {"iccid":"...","customerEmail":"...","active":true/false}
        JsonNode activeNode = jsonNode.get("active");
        Assertions.assertNotNull(activeNode, "Response JSON should contain an 'active' field.");

        // Compare "true" or "false" against the node's text value
        String actualStatus = activeNode.asText(); // "true" / "false"
        Assertions.assertEquals(
                expectedStatus,
                actualStatus,
                "Activation status mismatch. Expected: " + expectedStatus + ", Actual: " + actualStatus
        );
    }
}