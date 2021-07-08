package dk.sample.rest.bank.virtualaccount.exposure.rs.model;

import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiModelProperty;


/**
 * the plan that ties a number of accounts (currently 3) account(s)
 * together with a virtual savings account )
 */
public class MicroPlanUpdateRepresentation {

    @Pattern(regexp = "^[a_zA_Z]{50}$")
    private String name;

    @Pattern(regexp = "^[.]{100}$")
    private String description;

    @Pattern(regexp = "^[0-9]{10}$")
    private String virtualAccount;

    @Pattern(regexp = "^[0-9]{4}-[0-9]+$")
    private String primaryAccount;

    @Pattern(regexp = "^[0-9]{4}-[0-9]+$")
    private String secondaryAccount;

    @Pattern(regexp = "^[0-9]{4}-[0-9]+$")
    private String tertiaryAccount;

    @ApiModelProperty(
            access = "public",
            name = "name",
            required = true,
            example = "Childrens Microsavings Plan",
            value = "the name of the plan.")
    public String getName() {
        return name;
    }

    @ApiModelProperty(
            access = "public",
            name = "descrition",
            required = true,
            example = "A Childrens Microsavings Plan, allowing yu to support yours kid to get saving in a comfortable way",
            value = "the name of the plan.")
    public String getDescription() {
        return description;
    }

    public String getVirtualAccount() {
        return virtualAccount;
    }
    @ApiModelProperty(
            access = "public",
            name = "primaryaccountnumber",
            required = true,
            example = "1234-1234567890",
            value = "the primary account number.")
    public String getPrimaryAccount() {
        return primaryAccount;
    }
    @ApiModelProperty(
            access = "public",
            name = "secondaryaccountnumber",
            required = true,
            example = "1234-1234567890",
            value = "the secondary account number.")
    public String getSecondaryAccount() {
        return secondaryAccount;
    }

    @ApiModelProperty(
            access = "public",
            name = "tertiaryaccountnumber",
            required = true,
            example = "1234-1234567890",
            value = "the tertiary account number.")
    public String getTertiaryAccount() {
        return tertiaryAccount;
    }
}
