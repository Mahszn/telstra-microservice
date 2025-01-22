Feature: SIM Card Activation

  # This feature tests activating a SIM card via the Telstra microservice and
  # verifying the outcome by querying the database.

  Scenario: Successfully activate SIM card
    Given I have a SIM card with ICCID "1255789453849037777" and email "success@example.com"
    When I send a POST request to /activateSIM
    Then the SIM card activation should be successful
    And a new record should be saved in the database with id 1

    # Querying by the expected auto-increment ID to confirm 'active' is true
    When I query for the record with id 1
    Then the activation status should be "true"


  Scenario: Failing to activate SIM card
    Given I have a SIM card with ICCID "8944500102198304826" and email "fail@example.com"
    When I send a POST request to /activateSIM
    Then the SIM card activation should fail
    And a new record should be saved in the database with id 2

    # Querying the second record created, expecting 'active' to be false
    When I query for the record with id 2
    Then the activation status should be "false"
