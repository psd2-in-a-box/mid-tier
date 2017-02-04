package dk.sample.rest.bank.virtualaccount.exposure.rs.model;

import dk.sample.rest.bank.virtualaccount.exposure.rs.VirtualAccountServiceExposure;
import java.math.BigDecimal;
import javax.ws.rs.core.UriInfo;

import dk.nykredit.jackson.dataformat.hal.HALLink;
import dk.nykredit.jackson.dataformat.hal.annotation.Link;
import dk.nykredit.jackson.dataformat.hal.annotation.Resource;

import dk.sample.rest.bank.virtualaccount.model.VirtualAccount;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a single VirtualAccount as returned from REST service in the default projection.
 */
@Resource
@ApiModel(value = "VirtualAccount",
        description = "the virtual Account")
public class VirtualAccountRepresentation {
    private String vaNumber;
    private String totalBalance;
    private String committedBalance;
    private String uncommittedBalance;

    @Link
    private HALLink self;


    public VirtualAccountRepresentation(VirtualAccount account, UriInfo uriInfo) {
        this.vaNumber = account.getVaNumber().toString();
        this.totalBalance = account.getTotalBalance().toString();
        this.committedBalance = account.getComittedBalance().toString();
        this.uncommittedBalance = account.getUnCommittedBalance().toString();
        this.self = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                .path(VirtualAccountServiceExposure.class)
                .path(VirtualAccountServiceExposure.class, "get")
                .build(account.getVaNumber()).toString())
                .build();
    }

    @ApiModelProperty(
            access = "public",
            name = "vaNumber",
            example = "54791234567890",
            value = "the virtual account number.")
    public String getVaNumber() {
        return vaNumber;
    }

    @ApiModelProperty(
            access = "public",
            name = "totalBalance",
            example = "57.0214422",
            value = "the totalBalance of virtual account.")
    public String getTotalBalance() {
        return totalBalance;
    }

    @ApiModelProperty(
            access = "public",
            name = "committedBalance",
            example = "20002,12",
            value = "the committedBalance of a virtual account.")
    public String getCommittedBalance() {
        return committedBalance;
    }

    @ApiModelProperty(
            access = "public",
            name = "uncommittedBalance",
            example = "1023,45",
            value = "the uncommitted balance for a virtual account.")
    public String getUncommittedBalance() {
        return uncommittedBalance;
    }

    @ApiModelProperty(
            access = "public",
            name = "self",
            notes = "link to the account itself.")
    public HALLink getSelf() {
        return self;
    }
}
