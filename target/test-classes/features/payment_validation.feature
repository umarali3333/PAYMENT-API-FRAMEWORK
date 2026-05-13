# ============================================================
# ISO 20022 Cross-Border Payment Validation Tests
# ============================================================
# These scenarios cover the business rules specific to
# cross-border payments, ISO 20022 compliance, and
# financial data validation.
# ============================================================

Feature: ISO 20022 Payment Field Validation Tests

  Background:
    Given user sets API base URL

  # ────────────────────────────────────────────────────────────
  # SCENARIO 1: Valid IBAN format - German account
  # ────────────────────────────────────────────────────────────
  Scenario: Validate German IBAN format in payment
    Given user sets API base URL
    When user initiates a cross-border payment of "500.00" "EUR" from IBAN "DE89370400440532013000" to IBAN "FR7614508059144900028066622"
    Then validate payment was created successfully
    And validate stored IBAN "debtorIban" is in correct format
    And validate stored IBAN "creditorIban" is in correct format

  # ────────────────────────────────────────────────────────────
  # SCENARIO 2: Valid amount - normal transaction
  # ────────────────────────────────────────────────────────────
  Scenario: Validate payment amount is correct format
    Given user sets API base URL
    When user initiates a cross-border payment of "250.50" "USD" from IBAN "US12345678901234567890" to IBAN "GB29NWBK60161331926819"
    Then validate payment was created successfully
    And validate payment amount "paymentAmount" is valid
    And validate payment currency "paymentCurrency" is a valid ISO 4217 code

  # ────────────────────────────────────────────────────────────
  # SCENARIO 3: EUR payment - most common in SEPA
  # ────────────────────────────────────────────────────────────
  Scenario: Validate EUR cross-border payment
    Given user sets API base URL
    When user initiates a cross-border payment of "1000.00" "EUR" from IBAN "DE89370400440532013000" to IBAN "NL91ABNA0417164300"
    Then validate payment was created successfully
    And validate payment currency "paymentCurrency" is a valid ISO 4217 code

  # ────────────────────────────────────────────────────────────
  # SCENARIO 4: GBP payment - UK domestic / SWIFT
  # ────────────────────────────────────────────────────────────
  Scenario: Validate GBP payment to UK account
    Given user sets API base URL
    When user initiates a cross-border payment of "750.00" "GBP" from IBAN "GB29NWBK60161331926819" to IBAN "GB82WEST12345698765432"
    Then validate payment was created successfully
    And validate payment currency "paymentCurrency" is a valid ISO 4217 code

  # ────────────────────────────────────────────────────────────
  # SCENARIO 5: Response must not expose sensitive data
  # PCI DSS requirement: mask card numbers, no CVV/PIN in response
  # ────────────────────────────────────────────────────────────
  Scenario: Payment response does not expose sensitive banking data
    Given user sets API base URL
    When user initiates a cross-border payment of "100.00" "EUR" from IBAN "DE89370400440532013000" to IBAN "GB29NWBK60161331926819"
    Then validate payment was created successfully
    And validate no sensitive data is exposed

  # ────────────────────────────────────────────────────────────
  # SCENARIO 6: High-value payment - special handling
  # Payments > EUR 1M may require additional compliance checks
  # ────────────────────────────────────────────────────────────
  Scenario: High value payment submission
    Given user sets API base URL
    When user initiates a cross-border payment of "50000.00" "EUR" from IBAN "DE89370400440532013000" to IBAN "GB29NWBK60161331926819"
    Then validate payment was created successfully
    And validate response time less than 5000

  # ────────────────────────────────────────────────────────────
  # SCENARIO 7: Multiple currencies - ensure ISO 4217 validation
  # ────────────────────────────────────────────────────────────
  Scenario Outline: Validate multiple currency codes
    Given user sets API base URL
    When user initiates a cross-border payment of "100.00" "<currency>" from IBAN "DE89370400440532013000" to IBAN "GB29NWBK60161331926819"
    Then validate payment was created successfully
    And validate payment currency "paymentCurrency" is a valid ISO 4217 code

    Examples:
      | currency |
      | EUR      |
      | USD      |
      | GBP      |
      | CHF      |
      | JPY      |
