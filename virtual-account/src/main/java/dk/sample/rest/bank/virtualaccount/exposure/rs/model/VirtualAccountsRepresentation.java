package dk.sample.rest.bank.virtualaccount.exposure.rs.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriInfo;

import dk.nykredit.jackson.dataformat.hal.HALLink;
import dk.nykredit.jackson.dataformat.hal.annotation.EmbeddedResource;
import dk.nykredit.jackson.dataformat.hal.annotation.Link;
import dk.nykredit.jackson.dataformat.hal.annotation.Resource;

import dk.sample.rest.bank.virtualaccount.exposure.rs.VirtualAccountServiceExposure;
import dk.sample.rest.bank.virtualaccount.model.VirtualAccount;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a set of accounts from the REST service exposure in this default projection.
 */
@Resource
@ApiModel(value = "VirtualAccounts",
        description = "a list of virtual accounts")
public class VirtualAccountsRepresentation {

    @Link
    private HALLink self;

    @EmbeddedResource("virtualAccounts")
    private Collection<VirtualAccountRepresentation> accounts;

    public VirtualAccountsRepresentation(List<VirtualAccount> accounts, UriInfo uriInfo) {
        this.accounts = new ArrayList<>();
        this.accounts.addAll(accounts.stream()
                .map(account -> new VirtualAccountRepresentation(account, uriInfo))
                .collect(Collectors.toList()));
        this.self = new HALLink.Builder(uriInfo.getBaseUriBuilder()
            .path(VirtualAccountServiceExposure.class)
            .build())
            .build();
    }

    @ApiModelProperty(
            access = "public",
            name = "self",
            notes = "link to the accounts list itself.")
    public HALLink getSelf() {
        return self;
    }

    @ApiModelProperty(
            access = "public",
            name = "accounts",
            value = "virtual accounts list.")
    public Collection<VirtualAccountRepresentation> getAccounts() {
        return Collections.unmodifiableCollection(accounts);
    }
}
