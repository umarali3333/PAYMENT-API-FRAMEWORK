# ============================================================
# GET API Tests - ISO 20022 Cross-Border Payment API
# ============================================================
# This feature covers all GET (read/inquiry) scenarios.
# Real API: Replace endpoints with your payment gateway URLs.
# Demo API: Using jsonplaceholder.typicode.com/posts
# ============================================================

Feature: GET API Tests

  Background:
    Given user sets API base URL

  # ────────────────────────────────────────────────────────────
  # SCENARIO 1: Get all payments (list endpoint)
  # ────────────────────────────────────────────────────────────
  Scenario: Get all posts
    Given user sets API base URL
    When user sends GET request to "/posts"
    Then validate status code is 400
    And validate response contains "title"

  # ────────────────────────────────────────────────────────────
  # SCENARIO 2: Get specific payment by ID
  # ────────────────────────────────────────────────────────────
  Scenario: Get post id 1
    Given user sets API base URL
    When user sends GET request to "/posts/1"
    Then validate status code is 200
    And validate response contains "userId"

  # ────────────────────────────────────────────────────────────
  # SCENARIO 3: Validate response headers
  # ────────────────────────────────────────────────────────────
  Scenario: Validate response header
    Given user sets API base URL
    When user sends GET request to "/posts"
    Then validate header "Content-Type"

  # ────────────────────────────────────────────────────────────
  # SCENARIO 4: Validate API response time SLA
  # Payment APIs have strict performance requirements:
  # SWIFT GPI < 30s, SEPA Instant < 10s, Internal APIs < 5s
  # ────────────────────────────────────────────────────────────
  Scenario: Validate response time
    Given user sets API base URL
    When user sends GET request to "/posts"
    Then validate response time less than 5000

  # ────────────────────────────────────────────────────────────
  # SCENARIO 5: Invalid post/payment ID returns 404
  # ────────────────────────────────────────────────────────────
  Scenario: Invalid post id
    Given user sets API base URL
    When user sends GET request to "/posts/0002"
    Then validate status code is 404

  # ────────────────────────────────────────────────────────────
  # SCENARIO 6: Content-Type validation (JSON format required)
  # ────────────────────────────────────────────────────────────
  Scenario: Validate Content-Type header is JSON
    Given user sets API base URL
    When user sends GET request to "/posts/1"
    Then validate status code is 200
    And validate response has Content-Type JSON

  # ────────────────────────────────────────────────────────────
  # SCENARIO 7: Get all payments and verify list is not empty
  # ────────────────────────────────────────────────────────────
  Scenario: Validate list response is not empty
    Given user sets API base URL
    When user sends GET request to "/posts"
    Then validate status code is 200
    And validate response is a list with items

  # ────────────────────────────────────────────────────────────
  # SCENARIO 8: Verify specific fields exist in payment record
  # In ISO 20022: id=endToEndId, userId=debtorReference, title=remittanceInfo
  # ────────────────────────────────────────────────────────────
  Scenario: Validate required fields in payment record
    Given user sets API base URL
    When user sends GET request to "/posts/1"
    Then validate status code is 200
    And validate response contains "id"
    And validate response contains "title"
    And validate response contains "body"
    And validate response contains "userId"

  # ────────────────────────────────────────────────────────────
  # SCENARIO 9: Filter payments by user (debtor in ISO 20022)
  # ────────────────────────────────────────────────────────────
  Scenario: Get payments filtered by user ID
    Given user sets API base URL
    When user sends GET request to "/posts" with query param "userId" = "1"
    Then validate status code is 200
    And validate response is a list with items

  # ────────────────────────────────────────────────────────────
  # SCENARIO 10: Get comments for a specific post (sub-resource)
  # In payments: GET /payments/{id}/statusUpdates
  # ────────────────────────────────────────────────────────────
  Scenario: Get sub-resource for payment (comments/status updates)
    Given user sets API base URL
    When user sends GET request to "/posts/1/comments"
    Then validate status code is 200
    And validate response is a list with items

  # ────────────────────────────────────────────────────────────
  # SCENARIO 11: Validate correct ID in response
  # ────────────────────────────────────────────────────────────
  Scenario: Validate payment ID matches requested ID
    Given user sets API base URL
    When user sends GET request to "/posts/5"
    Then validate status code is 200
    And validate "id" field value is 5

  # ────────────────────────────────────────────────────────────
  # SCENARIO 12: Response body is not empty
  # ────────────────────────────────────────────────────────────
  Scenario: Validate response body is not empty
    Given user sets API base URL
    When user sends GET request to "/posts/1"
    Then validate status code is 200
    And validate response body is not empty

  # ────────────────────────────────────────────────────────────
  # SCENARIO 13: Performance SLA - strict 3 second limit
  # ────────────────────────────────────────────────────────────
  Scenario: Validate response meets strict SLA
    Given user sets API base URL
    When user sends GET request to "/posts/1"
    Then validate status code is 200
    And validate response time is within SLA of 3000 milliseconds

  # ────────────────────────────────────────────────────────────
  # SCENARIO 14: Get multiple resource types in sequence
  # ────────────────────────────────────────────────────────────
  Scenario: Get posts and users resources
    Given user sets API base URL
    When user sends GET request to "/posts"
    Then validate status code is 200
    When user sends GET request to "/users"
    Then validate status code is 200

  # ────────────────────────────────────────────────────────────
  # SCENARIO 15: All items in list have required fields
  # In real payments: all records must have payment ID and status
  # ────────────────────────────────────────────────────────────
  Scenario: Validate each item in list has required payment fields
    Given user sets API base URL
    When user sends GET request to "/posts"
    Then validate status code is 200
    And validate each item has field "id"
    And validate each item has field "title"
    And validate each item has field "userId"
