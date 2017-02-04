package dk.sample.rest.bank.virtualaccount.model;

import java.math.BigDecimal;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import dk.sample.rest.common.persistence.jpa.AbstractAuditable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Very basic modelling of virtual account concept to show the basic use of JPA for persistence handling.
 */
@Entity
@Table(name = "VIRTUALACCOUNT", uniqueConstraints = @UniqueConstraint(columnNames = { "VANUMBER" }))
public class VirtualAccount extends AbstractAuditable {
    /**
     * TID - the technical unique identifier for instance, i.e., primary key. This should NEVER EVER be
     * exposed out side the service since it is a key very internal to this service.
     */
    @Id
    @Column(name = "TID", length = 36, nullable = false, columnDefinition = "CHAR(36)")
    private String tId;

    @Column(name = "VANUMBER", columnDefinition = "LONG")
    private Long vaNumber;

    @Column(name = "TOTAL", columnDefinition = "DECIMAL")
    private BigDecimal totalBalance;

    @Column(name = "COMMITTED", columnDefinition = "DECIMAL")
    private BigDecimal comittedBalance;

    @Column(name = "NUMBER", columnDefinition = "DECIMAL")
    private BigDecimal unCommittedBalance;

    protected VirtualAccount() {
        // Required by JPA
    }

    public VirtualAccount(Long vaNumber, BigDecimal totalBalance, BigDecimal comittedBalance, BigDecimal unCommittedBalance) {
        this.vaNumber = vaNumber;
        this.totalBalance = totalBalance;
        this.comittedBalance = comittedBalance;
        this.unCommittedBalance = unCommittedBalance;
        tId = UUID.randomUUID().toString();
    }

    public VirtualAccount(Long vaNumber, BigDecimal totalBalance, BigDecimal comittedBalance) {
        this(vaNumber, totalBalance, comittedBalance, new BigDecimal(0));
    }

    public Long getVaNumber() {
        return vaNumber;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public BigDecimal getComittedBalance() {
        return comittedBalance;
    }

    public void addUnCommitted(BigDecimal amount) {
        this.unCommittedBalance = this.unCommittedBalance.add(amount);
    }

    public BigDecimal getUnCommittedBalance() {
        return unCommittedBalance;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("vaNumber", vaNumber)
                .append("totalBalance", totalBalance)
                .append("comittedBalance", comittedBalance)
                .append("unCommittedBalance", unCommittedBalance)
                .toString();
    }

}
