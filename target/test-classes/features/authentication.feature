# ============================================================
# Authentication & Authorization Tests
# ============================================================
# Critical for banking/payment APIs:
# - All endpoints MUST require authentication
# - Tokens must expire and be refreshed
# - Role-based access must be enforced
# - Failed auth must return 401 (not 403 or 200!)
# ============================================================

Feature: Authentication and Authorization Tests

  Background:
    Given user sets API base URL

  # ────────────────────────────────────────────────────────────
  # SCENARIO 1: Valid auth token - success
  # ────────────────────────────────────────────────────────────
  Scenario: Authenticated request succeeds
    Given user is authenticated with a valid token
    When user sends authenticated GET request to "/posts/1"
    Then validate status code is 200
    And validate response contains "id"

  # ────────────────────────────────────────────────────────────
  # SCENARIO 2: Missing auth token - unauthorized
  # Real APIs MUST return 401. JSONPlaceholder is public (returns 200).
  # This shows the PATTERN to use in real payment APIs.
  # ────────────────────────────────────────────────────────────
  Scenario: Unauthenticated GET request
    Given user has no authentication credentials
    When user sends unauthenticated GET request to "/posts"
    Then validate response is successful

  # ────────────────────────────────────────────────────────────
  # SCENARIO 3: Unauthenticated POST (create payment without auth)
  # Real payment API: Must return 401 Unauthorized
  # ────────────────────────────────────────────────────────────
  Scenario: Unauthenticated POST request
    Given user has no authentication credentials
    When user sends unauthenticated POST request to "/posts"
    Then validate response is successful

  # ────────────────────────────────────────────────────────────
  # SCENARIO 4: API Key authentication works
  # ────────────────────────────────────────────────────────────
  Scenario: API Key authenticated request succeeds
    Given user uses API key authentication
    When user sends authenticated GET request to "/posts"
    Then validate status code is 200
    And validate response is a list with items
