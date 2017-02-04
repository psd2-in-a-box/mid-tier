package dk.sample.rest.bank.connector.snb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/**
 * Represents single transacation in the SN API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SNBTransaction {

    private String id;
    private BigDecimal amount;
    private String text;
    private long transactionDateTimestamp;

    public String getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getText() {
        return text;
    }

    public long getTransactionDateTimestamp() {
        return transactionDateTimestamp;
    }

}
