package models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.UUID;

/**
 * PaymentRequestBuilder - Creates ISO 20022 inspired payment request payloads.
 *
 * ISO 20022 Message Types used in cross-border payments:
 * ─────────────────────────────────────────────────────
 * pacs.008 → Customer Credit Transfer (most common - send money)
 * pacs.004 → Payment Return (refund/recall)
 * pacs.002 → Payment Status Report (check if payment succeeded)
 * camt.056 → Payment Cancellation Request
 * camt.029 → Cancellation Status (approved/rejected)
 * pain.001 → Credit Transfer Initiation (from corporate to bank)
 * pain.002 → Customer Payment Status Report
 *
 * NOTE: We use JSONPlaceholder's /posts endpoint to simulate API calls
 * because it's a free, public API. In real projects, replace the endpoint
 * with your bank's actual payment API URL.
 *
 * The payload structure mirrors ISO 20022 naming conventions.
 */
public class PaymentRequestBuilder {

    private static final Logger log = LogManager.getLogger(PaymentRequestBuilder.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    // ─────────────────────────────────────────────────────────────────────────
    // STANDARD CREDIT TRANSFER (pacs.008) - "Send Money"
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds a standard cross-border credit transfer payload.
     *
     * In real banking:
     * - Debtor  = person/company SENDING the money
     * - Creditor = person/company RECEIVING the money
     * - BIC = Bank Identifier Code (e.g., DEUTDEDB for Deutsche Bank)
     * - IBAN = International Bank Account Number
     */
    public static String buildCreditTransfer(
            String amount,
            String currency,
            String debtorIban,
            String debtorBic,
            String creditorIban,
            String creditorBic) {

        ObjectNode root = mapper.createObjectNode();

        // Message identification
        root.put("messageId", "MSG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        root.put("creationDateTime", Instant.now().toString());
        root.put("messageType", "pacs.008.001.08");

        // Transaction details
        ObjectNode txInfo = root.putObject("creditTransferTransactionInfo");
        txInfo.put("endToEndId", "E2E-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        txInfo.put("uetr", UUID.randomUUID().toString()); // Unique End-to-end Transaction Reference (SWIFT GPI)

        // Amount
        ObjectNode amt = txInfo.putObject("instructedAmount");
        amt.put("currency", currency);
        amt.put("amount", amount);

        // Charge bearer (who pays the fees)
        // SHA = shared (common), OUR = sender pays all, BEN = beneficiary pays all
        txInfo.put("chargeBearer", "SHAR");

        // Debtor (Sender)
        ObjectNode debtor = txInfo.putObject("debtor");
        debtor.put("name", "ACME Corporation");
        ObjectNode debtorAccount = txInfo.putObject("debtorAccount");
        debtorAccount.put("iban", debtorIban);
        ObjectNode debtorAgent = txInfo.putObject("debtorAgent");
        debtorAgent.put("bic", debtorBic);

        // Creditor (Receiver)
        ObjectNode creditor = txInfo.putObject("creditor");
        creditor.put("name", "XYZ Supplier Ltd");
        ObjectNode creditorAccount = txInfo.putObject("creditorAccount");
        creditorAccount.put("iban", creditorIban);
        ObjectNode creditorAgent = txInfo.putObject("creditorAgent");
        creditorAgent.put("bic", creditorBic);

        // Purpose / Reference
        txInfo.put("remittanceInformation", "Invoice INV-2024-001 Payment");
        txInfo.put("purpose", "SUPP"); // Supplier payment

        log.debug("Built pacs.008 Credit Transfer payload: amount={} {}", amount, currency);

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            log.error("Failed to serialize payment request: {}", e.getMessage());
            return "{}";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PAYMENT RETURN (pacs.004) - "Refund / Recall"
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds a payment return/refund request.
     * Used when: wrong account, duplicate payment, customer request.
     */
    public static String buildPaymentReturn(String originalMessageId, String returnReason) {
        ObjectNode root = mapper.createObjectNode();

        root.put("messageId", "RET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        root.put("creationDateTime", Instant.now().toString());
        root.put("messageType", "pacs.004.001.09");

        ObjectNode txReturn = root.putObject("transactionReturn");
        txReturn.put("originalEndToEndId", originalMessageId);
        txReturn.put("returnReasonCode", returnReason); // AC04=Closed account, AM09=Wrong amount

        log.debug("Built pacs.004 Payment Return payload. Reason: {}", returnReason);

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            return "{}";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PAYMENT STATUS INQUIRY (pacs.002) - "Where's my payment?"
    // ─────────────────────────────────────────────────────────────────────────

    public static String buildStatusInquiry(String endToEndId) {
        ObjectNode root = mapper.createObjectNode();
        root.put("messageType", "pacs.002.001.10");
        root.put("originalEndToEndId", endToEndId);
        root.put("inquiryDateTime", Instant.now().toString());

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            return "{}";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INVALID PAYLOADS (for negative testing)
    // ─────────────────────────────────────────────────────────────────────────

    /** Missing required 'amount' field */
    public static String buildMissingAmountPayload() {
        return """
                {
                  "messageType": "pacs.008.001.08",
                  "debtorAccount": { "iban": "DE89370400440532013000" },
                  "creditorAccount": { "iban": "GB29NWBK60161331926819" }
                }
                """;
    }

    /** Negative amount - should be rejected */
    public static String buildNegativeAmountPayload() {
        return """
                {
                  "messageType": "pacs.008.001.08",
                  "instructedAmount": { "currency": "EUR", "amount": "-100.00" },
                  "debtorAccount": { "iban": "DE89370400440532013000" },
                  "creditorAccount": { "iban": "GB29NWBK60161331926819" }
                }
                """;
    }

    /** Invalid IBAN format */
    public static String buildInvalidIbanPayload() {
        return """
                {
                  "messageType": "pacs.008.001.08",
                  "instructedAmount": { "currency": "EUR", "amount": "500.00" },
                  "debtorAccount": { "iban": "INVALID-IBAN-123" },
                  "creditorAccount": { "iban": "ALSO-INVALID" }
                }
                """;
    }

    /** Exceeds daily limit */
    public static String buildExceedsLimitPayload() {
        return """
                {
                  "messageType": "pacs.008.001.08",
                  "instructedAmount": { "currency": "EUR", "amount": "9999999.99" },
                  "debtorAccount": { "iban": "DE89370400440532013000" },
                  "creditorAccount": { "iban": "GB29NWBK60161331926819" }
                }
                """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SIMPLIFIED PAYLOAD (for JSONPlaceholder demo)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * JSONPlaceholder /posts accepts: title, body, userId
     * We map payment fields to these for live demo purposes.
     */
    public static String buildDemoPaymentPost(String amount, String currency) {
        ObjectNode root = mapper.createObjectNode();
        root.put("title", "Payment " + currency + " " + amount);
        root.put("body", "Cross-border payment via pacs.008 for amount " + amount + " " + currency);
        root.put("userId", 1);
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            return "{}";
        }
    }
}
