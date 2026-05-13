# ============================================================
# PUT / PATCH API Tests - Payment Update / Amendment
# ============================================================
# PUT /payments/{id}   → Full payment update (before settlement)
# PATCH /payments/{id} → Partial amendment (reference, name fix)
#
# ISO 20022: camt.056 (Payment Cancellation) / Pain.001 amend
# ============================================================

Feature: PUT and PATCH API Tests - Payment Amendments

  Background:
    Given user sets API base URL

  # ────────────────────────────────────────────────────────────
  # SCENARIO 1: Full update of a payment record
  # ────────────────────────────────────────────────────────────
  Scenario: Update entire payment record
    Given user sets API base URL
    When user sends PUT request to "/posts/1"
    Then validate status code is 200
    And validate payment was updated successfully

  # ────────────────────────────────────────────────────────────
  # SCENARIO 2: PUT response has Content-Type JSON
  # ────────────────────────────────────────────────────────────
  Scenario: Validate PUT response headers
    Given user sets API base URL
    When user sends PUT request to "/posts/1"
    Then validate status code is 200
    And validate response has Content-Type JSON

  # ────────────────────────────────────────────────────────────
  # SCENARIO 3: PATCH - partial update of reference field
  # Use case: Correct the beneficiary name or payment reference
  # ────────────────────────────────────────────────────────────
  Scenario: Partial update of payment reference
    Given user sets API base URL
    When user sends PATCH request to "/posts/1" with field "title" = "Corrected Payment Reference"
    Then validate status code is 200
    And validate updated field "title" contains "Corrected"

  # ────────────────────────────────────────────────────────────
  # SCENARIO 4: Update payment amount and currency
  # ────────────────────────────────────────────────────────────
  Scenario: Update payment amount
    Given user sets API base URL
    When user updates payment "1" with amount "2500.00" "USD"
    Then validate payment was updated successfully
    And validate response time less than 5000

  # ────────────────────────────────────────────────────────────
  # SCENARIO 5: PUT response body not empty
  # ────────────────────────────────────────────────────────────
  Scenario: PUT response body is not empty
    Given user sets API base URL
    When user sends PUT request to "/posts/1"
    Then validate status code is 200
    And validate response body is not empty
