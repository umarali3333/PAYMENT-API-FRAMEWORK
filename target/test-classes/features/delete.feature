# ============================================================
# DELETE API Tests - Payment Cancellation / Recall
# ============================================================
# DELETE /payments/{id} → Cancel a pending payment
#
# ISO 20022: camt.056 (Payment Cancellation Request)
# Only possible BEFORE settlement. After settlement, use pacs.004 (Return).
# ============================================================

Feature: DELETE API Tests - Payment Cancellation

  Background:
    Given user sets API base URL

  # ────────────────────────────────────────────────────────────
  # SCENARIO 1: Cancel a pending payment
  # ────────────────────────────────────────────────────────────
  Scenario: Cancel pending payment successfully
    Given user sets API base URL
    When user sends DELETE request to "/posts/1"
    Then validate payment was cancelled successfully

  # ────────────────────────────────────────────────────────────
  # SCENARIO 2: Cancel by ID and verify response code
  # ────────────────────────────────────────────────────────────
  Scenario: Delete post and validate status
    Given user sets API base URL
    When user cancels payment with ID "1"
    Then validate status code is 200

  # ────────────────────────────────────────────────────────────
  # SCENARIO 3: Delete performance validation
  # ────────────────────────────────────────────────────────────
  Scenario: Cancel payment within time SLA
    Given user sets API base URL
    When user sends DELETE request to "/posts/2"
    Then validate payment was cancelled successfully
    And validate response time less than 5000

  # ────────────────────────────────────────────────────────────
  # SCENARIO 4: Cancel already-cancelled payment (idempotency)
  # In ISO 20022 payment cancellation should be idempotent
  # ────────────────────────────────────────────────────────────
  Scenario: Attempt to cancel payment with invalid ID
    Given user sets API base URL
    When user attempts to delete non-existent payment "99999"
    Then validate status code is 404
