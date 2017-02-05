package dk.sample.rest.bank.virtualaccount.exposure.rs.model;

import dk.sample.rest.bank.account.exposure.rs.AccountServiceExposure;
import javax.ws.rs.core.UriInfo;

import dk.nykredit.jackson.dataformat.hal.HALLink;
import dk.nykredit.jackson.dataformat.hal.annotation.Link;

import dk.sample.rest.bank.virtualaccount.exposure.rs.MicroPlanServiceExposure;
import dk.sample.rest.bank.virtualaccount.exposure.rs.VirtualAccountServiceExposure;
import dk.sample.rest.bank.virtualaccount.model.MicroPlan;

import io.swagger.annotations.ApiModelProperty;

/**
 * simple representation for micro plan, a microplan is representing a plan for how the configuration for the
 * the accumulation of the fonds towards the virtual account
 */
public class MicroPlanRepresentation {

    private String name;
    private String description;

    @Link
    private HALLink self;

    @Link
    private HALLink virtual;

    @Link
    private HALLink primaryAccount;

    @Link
    private HALLink secondaryAccount;

    @Link
    private HALLink tertiaryAccount;


    public MicroPlanRepresentation(MicroPlan plan, UriInfo uriInfo) {
        this.name = plan.getName();
        this.description = plan.getDescription();
        this.self = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                .path(MicroPlanServiceExposure.class)
                .path(MicroPlanServiceExposure.class, "get")
                .build(name))
                .build();
        this.virtual = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                .path(VirtualAccountServiceExposure.class)
                .path(VirtualAccountServiceExposure.class, "get")
                .build(plan.getVirtualAccount()))
                .build();
        this.primaryAccount = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                .path(AccountServiceExposure.class)
                .path(AccountServiceExposure.class, "get")
                .build(plan.getPrimaryAccount().substring(0, 4), plan.getPrimaryAccount().substring(5)))
                .build();
        if (!"".equals(plan.getSecondaryAccount())) {
            this.secondaryAccount = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                    .path(AccountServiceExposure.class)
                    .path(AccountServiceExposure.class, "get")
                    .build(plan.getSecondaryAccount().substring(0, 4), plan.getSecondaryAccount().substring(5)))
                    .build();
        }
        if (!"".equals(plan.getTertiaryAccount())) {
            this.tertiaryAccount = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                    .path(AccountServiceExposure.class)
                    .path(AccountServiceExposure.class, "get")
                    .build(plan.getTertiaryAccount().substring(0, 4), plan.getTertiaryAccount().substring(5)))
                    .build();
        }
    }

    @ApiModelProperty(
            access = "public",
            name = "name",
            example = "standard 1-up easy saver",
            value = "the easy-save scheme name.")
    public String getName() {
        return name;
    }

    @ApiModelProperty(
            access = "public",
            name = "description",
            example = "standard 1-up easy saver description",
            value = "the easy-save scheme description")
    public String getDescription() {
        return description;
    }

    @ApiModelProperty(
            access = "public",
            name = "self",
            notes = "link to the plan itself.")
    public HALLink getSelf() {
        return self;
    }

    @ApiModelProperty(
            access = "public",
            name = "virtual",
            notes = "link to the virtual account itself.")
    public HALLink getVirtual() {
        return virtual;
    }

    @ApiModelProperty(
            access = "public",
            name = "primaryaccount",
            notes = "link to the primary account.")
    public HALLink getPrimaryAccount() {
        return primaryAccount;
    }

    @ApiModelProperty(
            access = "public",
            name = "secondaryaccount",
            notes = "link to the secondary account.")
    public HALLink getSecondaryAccount() {
        return secondaryAccount;
    }

    @ApiModelProperty(
            access = "public",
            name = "tertiaryAccount",
            notes = "link to the tertiary account.")
    public HALLink getTertiaryAccount() {
        return tertiaryAccount;
    }
}
