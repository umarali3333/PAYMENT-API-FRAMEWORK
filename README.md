# ISO 20022 Cross-Border Payment API Test Framework

## What This Is
A professional REST Assured + BDD Cucumber test framework designed around **ISO 20022 cross-border payment** scenarios. Built for Java developers who want a real-world, interview-ready API testing project.

---

## Quick Start (Run in 3 Steps)

### Prerequisites
- Java 11+
- Maven 3.6+
- VS Code with Extension Pack for Java

### Step 1: Open in VS Code
```bash
# Unzip the project
# File → Open Folder → select payment-api-framework
```

### Step 2: Install dependencies
```bash
mvn clean install -DskipTests
```

### Step 3: Run tests
```bash
mvn test
```

### View Report
```
target/cucumber-reports/cucumber-report.html
```
Open in browser - it's a visual HTML report!

---

## Project Structure

```
payment-api-framework/
├── src/test/
│   ├── java/
│   │   ├── TestRunner.java                     ← Entry point
│   │   ├── config/
│   │   │   └── ConfigManager.java              ← Environment config
│   │   ├── utils/
│   │   │   ├── BaseAPI.java                    ← REST Assured setup
│   │   │   ├── AuthManager.java                ← All auth types
│   │   │   ├── TestContext.java                ← Shared state between steps
│   │   │   └── ValidationHelper.java           ← All assertion methods
│   │   ├── models/
│   │   │   └── PaymentRequestBuilder.java      ← ISO 20022 payloads
│   │   └── steps/
│   │       ├── Hooks.java                      ← Before/After lifecycle
│   │       ├── CommonStepDefinitions.java      ← Shared Given/Then steps
│   │       ├── GetPaymentStepDefinitions.java  ← GET steps
│   │       ├── PostPaymentStepDefinitions.java ← POST steps
│   │       └── PutDeleteStepDefinitions.java   ← PUT/PATCH/DELETE steps
│   └── resources/
│       ├── features/
│       │   ├── get.feature                     ← 15 GET scenarios
│       │   ├── post.feature                    ← 10 POST scenarios
│       │   ├── put.feature                     ← 5 PUT/PATCH scenarios
│       │   ├── delete.feature                  ← 4 DELETE scenarios
│       │   ├── authentication.feature          ← 4 auth scenarios
│       │   └── payment_validation.feature      ← 7 ISO 20022 scenarios
│       └── config/
│           ├── dev.properties
│           └── staging.properties
```

---

## Running Specific Tests

```bash
# Run only GET tests
mvn test -Dcucumber.filter.tags="@get"

# Run against staging
mvn test -Denv=staging

# Run single feature file
mvn test -Dcucumber.features="src/test/resources/features/get.feature"
```

---

## ISO 20022 Payment Message Types Covered

| Message | Description | Used For |
|---------|-------------|----------|
| pacs.008 | Customer Credit Transfer | Send money cross-border |
| pacs.004 | Payment Return | Refund / Recall |
| pacs.002 | Payment Status Report | Track payment |
| camt.056 | Cancellation Request | Cancel before settlement |

---

## Authentication Types Supported

| Type | When Used | Class |
|------|-----------|-------|
| Bearer Token (JWT) | Open Banking, PSD2 | `AuthManager.withBearerToken()` |
| API Key | Gateways (Stripe, Adyen) | `AuthManager.withApiKey()` |
| Basic Auth | Legacy SWIFT | `AuthManager.withBasicAuth()` |
| OAuth2 Client Credentials | Bank-to-bank | `AuthManager.fetchOAuth2Token()` |

---

## Interview Q&A Guide

### Q: What is REST Assured?
**A:** Java library for API testing. Similar to Postman but in code. You write test in Java, run with Maven, get reports.

### Q: What is BDD / Cucumber?
**A:** Behaviour Driven Development. Feature files written in plain English (Gherkin), understood by non-technical stakeholders (business). Step definitions are the actual Java code underneath.

### Q: How does authentication work in payment APIs?
**A:** Two main approaches:
1. **OAuth2** - Client gets access token (JWT), attaches as `Authorization: Bearer <token>`
2. **mTLS** - Mutual certificate auth, required by PSD2/Open Banking

### Q: What is idempotency in payments?
**A:** Same payment request submitted twice = only one payment processed. Use `X-Idempotency-Key` header. Critical to prevent duplicate charges!

### Q: What is ISO 20022?
**A:** International standard for financial messaging. Defines XML format for payment messages. Used by SWIFT GPI, SEPA, Fedwire, TARGET2.

### Q: What are pacs.008 / pacs.004?
**A:** 
- **pacs.008** = Customer Credit Transfer (I want to send EUR 1000 to someone)
- **pacs.004** = Payment Return (send the money back, wrong account)

### Q: How do you validate IBAN?
**A:** IBAN = Country Code (2 letters) + Check Digits (2 numbers) + Account Number (up to 30 chars). Validate with regex: `[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}`

### Q: What is response time validation?
**A:** APIs have SLA (Service Level Agreements). SWIFT GPI < 30s, SEPA Instant < 10s. We test with `response.getTime() < maxMs`.

### Q: Difference between 401 and 403?
**A:** 
- **401 Unauthorized** = No credentials provided or invalid token
- **403 Forbidden** = Valid credentials but insufficient permission (wrong role)

---

## Real Payment Gateway Integration

To connect to a real payment API, change `dev.properties`:
```properties
base.url=https://api.your-payment-gateway.com/v1
auth.token=Bearer your-actual-jwt-token
api.key=your-actual-api-key
```

Then update the endpoints in feature files:
```gherkin
# Instead of:
When user sends GET request to "/posts"

# Use your real endpoint:
When user sends GET request to "/payments"
When user sends GET request to "/payments/PAY-12345/status"
```
