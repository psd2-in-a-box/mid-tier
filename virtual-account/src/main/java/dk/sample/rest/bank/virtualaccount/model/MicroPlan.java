package dk.sample.rest.bank.virtualaccount.model;

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
 * Very basic modelling of microplans concept to show the basic use of JPA for persistence handling.
 */
@Entity
@Table(name = "MICROPLAN", uniqueConstraints = @UniqueConstraint(columnNames = { "TID" }))
public class MicroPlan extends AbstractAuditable {

    /**
     * TID - the technical unique identifier for instance, i.e., primary key. This should NEVER EVER be
     * exposed out side the service since it is a key very internal to this service.
     */
    @Id
    @Column(name = "TID", length = 36, nullable = false, columnDefinition = "CHAR(36)")
    private String tId;

    @Column(name = "NAME", length = 36, nullable = false, columnDefinition = "CHAR(50)")
    private String name;

    @Column(name = "DESCRIPTION", columnDefinition = "VARCHAR(100)")
    private String description;

    @Column(name = "VIRTUALACCOUNTNO", columnDefinition = "VARCHAR(10)")
    private String virtualAccount;

    @Column(name = "PRIMARYACCOUNTNO", columnDefinition = "VARCHAR(16)")
    private String primaryAccount;

    @Column(name = "SECNODARYACCOUNTNO", columnDefinition = "VARCHAR(16)")
    private String secondaryAccount;

    @Column(name = "TERTIARYACCOUNTNO", columnDefinition = "VARCHAR(16)")
    private String tertiaryAccount;

    protected MicroPlan() {
        // Required by JPA
    }

    public MicroPlan(String name, String description, String virtualAccount,
                     String primaryAccount, String secondaryAccount) {
        this(name, description, virtualAccount, primaryAccount, secondaryAccount, "");
    }

    public MicroPlan(String name, String description, String virtualAccount,
                     String primaryAccount) {
        this(name, description, virtualAccount, primaryAccount, "", "");
    }

    public MicroPlan(String name, String description, String virtualAccount,
                     String primaryAccount, String secondaryAccount, String tertiaryAccount) {
        tId = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.virtualAccount = virtualAccount;
        this.primaryAccount = primaryAccount;
        this.secondaryAccount = secondaryAccount;
        this.tertiaryAccount = tertiaryAccount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getVirtualAccount() {
        return virtualAccount;
    }

    public String getPrimaryAccount() {
        return primaryAccount;
    }

    public String getSecondaryAccount() {
        return secondaryAccount;
    }

    public String getTertiaryAccount() {
        return tertiaryAccount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", name)
                .append("description", description)
                .append("virtualAccount", virtualAccount)
                .append("primaryAccount", primaryAccount)
                .append("secondaryAccount", secondaryAccount)
                .append("tertiaryAccount", tertiaryAccount)
                .toString();
    }

}
