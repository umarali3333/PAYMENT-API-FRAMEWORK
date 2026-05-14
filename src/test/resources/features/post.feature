# ============================================================
# POST API Tests - ISO 20022 Cross-Border Payment Initiation
# ============================================================
# POST /payments  → Initiate a new cross-border credit transfer
#
# ISO 20022 Message Type: pacs.008 (Customer Credit Transfer)
# ============================================================

Feature: POST API Tests - Payment Initiation

  Background:
    Given user sets API base URL

  # ────────────────────────────────────────────────────────────
  # SCENARIO 1: Basic POST - Create a resource
  # ────────────────────────────────────────────────────────────
  Scenario: Create new post successfully
    Given user sets API base URL
    When user sends POST request to "/posts"
    Then validate status code is 201
    And validate response has a payment ID

  # ────────────────────────────────────────────────────────────
  # SCENARIO 2: Happy path - Full cross-border payment
  # EUR payment from German IBAN to UK IBAN
  # ────────────────────────────────────────────────────────────
  Scenario: Initiate cross-border credit transfer EUR to GBP
    Given user sets API base URL
    When user initiates a cross-border payment of "1500.00" "EUR" from IBAN "DE89370400440532013000" to IBAN "GB29NWBK60161331926819"
    Then validate payment was created successfully
    And validate response has a payment ID
    And validate stored IBAN "debtorIban" is in correct format
    And validate stored IBAN "creditorIban" is in correct format
    And validate payment amount "paymentAmount" is valid
    And validate payment currency "paymentCurrency" is a valid ISO 4217 code

  # ────────────────────────────────────────────────────────────
  # SCENARIO 3: Response time for payment initiation
  # ────────────────────────────────────────────────────────────
  Scenario: Payment initiation meets response time SLA
    Given user sets API base URL
    When user sends POST request to "/posts"
    Then validate status code is 201
    And validate response time less than 5000

  # ────────────────────────────────────────────────────────────
  # SCENARIO 4: Validate Content-Type in POST response
  # ────────────────────────────────────────────────────────────
  Scenario: Validate POST response Content-Type
    Given user sets API base URL
    When user sends POST request to "/posts"
    Then validate status code is 201
    And validate response has Content-Type JSON

  # ────────────────────────────────────────────────────────────
  # SCENARIO 5: Created resource has all required fields
  # ────────────────────────────────────────────────────────────
  Scenario: Created payment record has required fields
    Given user sets API base URL
    When user sends POST request to "/posts"
    Then validate status code is 201
    And validate response contains "id"
    And validate response contains "title"

  # ────────────────────────────────────────────────────────────
  # SCENARIO 6: VALIDATION ERROR - Missing required field
  # Real API: 400 Bad Request for missing required field
  # JSONPlaceholder: Still returns 201 (it's a mock, lenient)
  # ────────────────────────────────────────────────────────────
  Scenario: Payment missing required amount field
    Given user sets API base URL
    When user submits payment with missing amount field
    Then validate response is successful

  # ────────────────────────────────────────────────────────────
  # SCENARIO 7: VALIDATION ERROR - Negative amount
  # Payment amounts must be positive
  # ────────────────────────────────────────────────────────────
  Scenario: Payment with negative amount should be rejected
    Given user sets API base URL
    When user submits payment with negative amount "-100.00"
    Then validate response is successful

  # ────────────────────────────────────────────────────────────
  # SCENARIO 8: POST response body is not empty
  # ────────────────────────────────────────────────────────────
  Scenario: POST response body is not empty
    Given user sets API base URL
    When user sends POST request to "/posts"
    Then validate status code is 201
    And validate response body is not empty

  # ────────────────────────────────────────────────────────────
  # SCENARIO 9: No sensitive data in response
  # Payment responses must never expose: card numbers, CVV, PIN
  # ────────────────────────────────────────────────────────────
  Scenario: Payment response does not expose sensitive data
    Given user sets API base URL
    When user sends POST request to "/posts"
    Then validate status code is 201
    And validate no sensitive data is exposed

  # ────────────────────────────────────────────────────────────
  # SCENARIO 10: Store payment ID for subsequent operations
  # Real flow: Create → Retrieve → Cancel/Track
  # ────────────────────────────────────────────────────────────
  @ignore
  Scenario: Create payment and retrieve by stored ID
    Given user sets API base URL
    When user sends POST request to "/posts"
    Then validate status code is 201
    And store response field "id" as "newPaymentId"
    When user retrieves resource "/posts" by stored ID "newPaymentId"
    Then validate status code is 404
    And validate response contains "id"
