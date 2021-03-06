package dk.sample.rest.bank.account.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import dk.sample.rest.common.persistence.jpa.AbstractAuditable;
import java.time.Instant;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Very basic modelling of account concept to show the basic use of JPA for persistence handling. Mirrored account.
 */
@Entity
@Table(name = "BANK_ACCOUNT", uniqueConstraints = @UniqueConstraint(columnNames = {"REG_NO", "ACCOUNT_NO"}))
public class Account extends AbstractAuditable {

    private static final String[] EXCLUDED_FIELDS = new String[]{"tId", "lastModifiedBy", "lastModifiedTime", "transactions",
        "reconciledTransactions"};

    /**
     * TID - the technical unique identifier for instance, i.e., primary key. This should NEVER EVER be exposed out side the service since it is
     * a key very internal to this service.
     */
    @Id
    @Column(name = "TID", length = 36, nullable = false, columnDefinition = "CHAR(36)")
    private String tId;

    @Column(name = "REG_NO", length = 4, nullable = false)
    private String regNo;

    @Column(name = "ACCOUNT_NO", length = 12, nullable = false)
    private String accountNo;

    @Column(name = "NAME", length = 40, nullable = false)
    private String name;

    @Column(name = "BALANCE", nullable = false)
    private BigDecimal balance;

    @Column(name = "CUSTOMER", nullable = false)
    private String customer;

    @Column(name = "LATEST")
    private Instant latest;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Transaction> transactions;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ReconciledTransaction> reconciledTransactions;

    protected Account() {
        // Required by JPA
    }

    public Account(String regNo, String accountNo, String name, String customer) {
        this.regNo = regNo;
        this.accountNo = accountNo;
        this.name = name;
        this.customer = customer;
        this.balance = new BigDecimal(0);
        latest = Instant.ofEpochMilli(0);
        transactions = new HashSet<>();
        reconciledTransactions = new HashSet<>();
        tId = UUID.randomUUID().toString();
    }

    public String getRegNo() {
        return regNo;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public String getName() {
        return name;
    }

    public String getCustomer() {
        return customer;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Set<Transaction> getTransactions() {
        return Collections.unmodifiableSet(transactions);
    }

    public Set<ReconciledTransaction> getReconciledTransactions() {
        return Collections.unmodifiableSet(reconciledTransactions);
    }

    public boolean addTransaction(String id, String description, BigDecimal amount, Instant timestamp) {
        return addTransaction(new Transaction(this, id, amount, description, timestamp));
    }

    public void addTransaction(String description, BigDecimal amount) {
        addTransaction(new Transaction(this, amount, description));
    }

    private boolean addTransaction(Transaction transaction) {
        if (!transactions.contains(transaction)) {
            transactions.add(transaction);
            balance = balance.add(transaction.getAmount());
            if (latest.isBefore(transaction.getTimestamp())) {
                latest = transaction.getTimestamp();
            }
            return true;
        }
        return false;
    }

    public void addReconciledTransaction(Transaction transaction, Boolean reconciled, String note) {
        reconciledTransactions.add(new ReconciledTransaction(reconciled, note, transaction));
    }

    public Instant getLatest() {
        return latest;
    }

    @Override
    protected String[] excludedFields() {
        return EXCLUDED_FIELDS;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("regNo", regNo)
                .append("accountNo", accountNo)
                .append("name", name)
                .toString();
    }

}
