package dk.sample.rest.bank.virtualaccount.exposure.rs.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The necessary input for creation and update of an virtual account.
 */
@ApiModel(value = "VirtualAccountUpdate",
        description = "the inout necessary for creating an virtual account")

public class VirtualAccountUpdateRepresentation {

    @NotNull
    @Pattern(regexp = "^[0-9]*")
    private String vaNumber;

    @NotNull
    @Pattern(regexp = "^[0-9]*.[0-9]{2}")
    private String totalBalance;

    @NotNull
    @Pattern(regexp = "^[0-9]*.[0-9]{2}")
    private String committedBalance;

    @NotNull
    @Pattern(regexp = "^[0-9]*.[0-9]{2}")
    private String unCommittedBalance;


    @ApiModelProperty(
            access = "public",
            name = "totalBalance",
            required = true,
            example = "246,47",
            value = "the totalBalance for a virtual account")
    public String getTotalBalance() {
        return totalBalance;
    }

    @ApiModelProperty(
            access = "public",
            name = "vaNumber",
            required = true,
            example = "54791234567890",
            value = "the vaNumber name of a virtual account")
    public String getVaNumber() {
        return vaNumber;
    }

    @ApiModelProperty(
            access = "public",
            name = "committedBalance",
            required = true,
            example = "234,35",
            value = "the committedBalance of the virtual account.",
            notes = " the weigth for observations for a virtual account")
    public String getCommittedBalance() {
        return committedBalance;
    }

    @ApiModelProperty(
            access = "public",
            name = "unCommittedBalance",
            required = true,
            example = "12,12",
            value = "the unCommittedBalance of the account.",
            notes = " the observed events for a given virtual account")
    public String getUnCommittedBalance() {
        return unCommittedBalance;
    }


}
