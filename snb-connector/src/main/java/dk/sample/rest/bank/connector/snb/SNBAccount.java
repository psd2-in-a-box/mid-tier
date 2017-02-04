package dk.sample.rest.bank.connector.snb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.nykredit.jackson.dataformat.hal.annotation.EmbeddedResource;
import dk.nykredit.jackson.dataformat.hal.annotation.Resource;
import java.math.BigDecimal;

/**
 * Representation of a SNB Account
 */
@Resource
@JsonIgnoreProperties(ignoreUnknown = true)
public class SNBAccount {

    private String accountNumber;
    private BigDecimal balance;
    private Status accountStatus;
    private BigDecimal creditMax;
    
    @EmbeddedResource
    private SNBCustomer owner;

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Status getAccountStatus() {
        return accountStatus;
    }

    public BigDecimal getCreditMax() {
        return creditMax;
    }

    public SNBCustomer getOwner() {
        return owner;
    }

    public String toString() {
        return "Number: " + accountNumber + " balance: " + balance + " status: " + accountStatus + " credit: " + creditMax;
    }

    enum Status {
        ACTIVE;
    }
    
}
