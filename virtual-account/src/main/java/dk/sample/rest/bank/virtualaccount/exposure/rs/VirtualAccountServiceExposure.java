package dk.sample.rest.bank.virtualaccount.exposure.rs;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dk.nykredit.time.CurrentTime;

import dk.sample.rest.bank.virtualaccount.exposure.rs.model.VirtualAccountRepresentation;
import dk.sample.rest.bank.virtualaccount.exposure.rs.model.VirtualAccountUpdateRepresentation;
import dk.sample.rest.bank.virtualaccount.exposure.rs.model.VirtualAccountsRepresentation;
import dk.sample.rest.bank.virtualaccount.model.VirtualAccount;
import dk.sample.rest.bank.virtualaccount.persistence.VirtualAccountArchivist;

import dk.sample.rest.common.core.logging.LogDuration;
import dk.sample.rest.common.rs.EntityResponseBuilder;
import dk.sample.rest.common.rs.error.ErrorRepresentation;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.ResponseHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exposing Virtul Accounts as REST service
 */
@Stateless
@Path("/virtualaccounts")
@PermitAll
@DeclareRoles("advisor")
@Api(value = "/virtualaccounts", tags = {"virtualAccount"})
public class VirtualAccountServiceExposure {
    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualAccountServiceExposure.class);

    private final Map<String, VirtualAccountsProducerMethod> accountsProducer = new HashMap<>();
    private final Map<String, VirtualAccountProducerMethod> accountProducers = new HashMap<>();

    @EJB
    private VirtualAccountArchivist archivist;


    public VirtualAccountServiceExposure() {
        accountsProducer.put("application/hal+json", this::listServiceGeneration1Version1);
        accountsProducer.put("application/hal+json;concept=locations;v=1", this::listServiceGeneration1Version1);

        accountProducers.put("application/hal+json", this::getServiceGeneration1Version2);
        accountProducers.put("application/hal+json;concept=virtualaccount;v=1", this::getServiceGeneration1Version1);
        accountProducers.put("application/hal+json;concept=virtualaccount;v=2", this::getServiceGeneration1Version2);
    }

    @GET
    @Produces({"application/hal+json", "application/hal+json;concept=virtualaccount;v=1"})
    @ApiOperation(value = "lists accounts", response = VirtualAccountsRepresentation.class,
            authorizations = {
                    @Authorization(value = "oauth2", scopes = {}),
                    @Authorization(value = "oauth2-cc", scopes = {}),
                    @Authorization(value = "oauth2-ac", scopes = {}),
                    @Authorization(value = "oauth2-rop", scopes = {}),
                    @Authorization(value = "Bearer")
            },
            extensions = {@Extension(name = "roles", properties = {
                    @ExtensionProperty(name = "advisor", value = "advisors are allowed getting every virtualaccount"),
                    @ExtensionProperty(name = "customer", value = "customer only allowed getting own locations")}
            )},
            produces = "application/hal+json, application/hal+json;concept=locations;v=1",
            notes = "List all locations in a default projection, which is VirtualAccount version 1" +
                    "Supported projections and versions are: " +
                    "VirtualAccounts in version 1 " +
                    "The Accept header for the default version is application/hal+json;concept=virtualaccount;v=1.0.0.... " +
                    "The format for the default version is {....}", nickname = "listVirtualAccounts")
    @ApiResponses(value = {
            @ApiResponse(code = 415, message = "Content type not supported.")
        })
    public Response list(@Context UriInfo uriInfo, @Context Request request, @HeaderParam("Accept") String accept) {
        return accountsProducer.getOrDefault(accept, this::handleUnsupportedContentType).getResponse(uriInfo, request);
    }

    @GET
    @Path("{virtualAccountNumber}")
    @Produces({"application/hal+json", "application/hal+json;concept=virtualaccount;v=1", "application/hal+json;concept=virtualaccount;v=2"})
    @ApiOperation(value = "gets the information from a single position", response = VirtualAccountRepresentation.class,
            authorizations = {
                    @Authorization(value = "oauth2", scopes = {}),
                    @Authorization(value = "oauth2-cc", scopes = {}),
                    @Authorization(value = "oauth2-ac", scopes = {}),
                    @Authorization(value = "oauth2-rop", scopes = {}),
                    @Authorization(value = "Bearer")
            },
            extensions = {@Extension(name = "roles", properties = {
                    @ExtensionProperty(name = "customer", value = "customer allows getting own information"),
                    @ExtensionProperty(name = "advisor", value = "advisor allows getting all information")}
            )},
            produces = "application/hal+json, application/hal+json;concept=virtualaccount;v=1, application/hal+json;concept=virtualaccount;v=2",
            notes = "obtain a single customer back in a default projection, which is VirtualAccount version 2" +
                    " Supported projections and versions are:" +
                    " VirtualAccount in version1 and VirtualAccount in version 2" +
                    " The format of the default version is .... - The Accept Header is not marked as required in the " +
                    "swagger - but it is needed - we are working on a solution to that", nickname = "getVirtualAccount")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "virtualaccount not found.")
            })
    public Response get(@Context UriInfo uriInfo, @Context Request request,
                        @PathParam("virtualAccountNumber") @Pattern(regexp = "^[0-9]*$") String virtualAccountNumber,
                        @HeaderParam("Accept") String accept) {
        LOGGER.info("Default version of virtualaccount collected");
        return accountProducers.getOrDefault(accept, this::handleUnsupportedContentType)
                .getResponse(uriInfo, request, virtualAccountNumber);
    }

    @PUT
    @RolesAllowed("system")
    @Path("{virtualAccountNumber}")
    @Produces({"application/hal+json"})
    @Consumes("application/json")
    @LogDuration(limit = 50)
    @ApiOperation(value = "Create new or update existing virtualaccount", response = VirtualAccountRepresentation.class,
            authorizations = {
                    @Authorization(value = "oauth2", scopes = {}),
                    @Authorization(value = "oauth2-cc", scopes = {}),
                    @Authorization(value = "oauth2-ac", scopes = {}),
                    @Authorization(value = "oauth2-rop", scopes = {}),
                    @Authorization(value = "Bearer")
            },
            extensions = {@Extension(name = "roles", properties = {
                    @ExtensionProperty(name = "customer", value = "customer allows getting own customer"),
                    @ExtensionProperty(name = "system", value = "system allows getting every customer")
            })},
            notes = "PUT is used to create a new virtualaccount or used to alter the values attached to the virtual account",
            consumes = "application/json",
            produces = "application/hal+json, application/hal+json;concept=virtualaccount;v=1, application/hal+json;concept=virtualaccount;v=2",
            nickname = "updateVirtualAccount")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Could not update or create the virtualaccount", response = ErrorRepresentation.class),
            @ApiResponse(code = 415, message = "The content-Type was not supported"),
            @ApiResponse(code = 201, message = "New VirtualAccount Created", response = VirtualAccountRepresentation.class,
                    responseHeaders = {
                            @ResponseHeader(name = "VirtualAccount", description = "a link to the created resource"),
                            @ResponseHeader(name = "Content-Type", description = "a link to the created resource"),
                            @ResponseHeader(name = "X-Log-Token", description = "an ide for reference purposes in logs etc")
                    })
            })
    public Response createOrUpdate(@Context UriInfo uriInfo, @Context Request request,
                                   @PathParam("virtualAccountNumber") String virtualAccountNumber,
                                   @ApiParam(value = "account") VirtualAccountUpdateRepresentation account) {
        if (!virtualAccountNumber.equals(account.getVaNumber())) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        Long no;
        try {
            no = Long.parseLong(virtualAccountNumber);
        } catch (NumberFormatException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }


        Optional<VirtualAccount> virtualaccount = archivist.findAccountByAccountNumber(no);
        VirtualAccount ac;
        if (virtualaccount.isPresent()) {
            ac = virtualaccount.get();
            ac.addUnCommitted(new BigDecimal(account.getUnCommittedBalance()));
        } else {
            ac = new VirtualAccount(no, new BigDecimal(account.getTotalBalance()),
                    new BigDecimal(account.getCommittedBalance()), new BigDecimal(account.getUnCommittedBalance()));
        }
        archivist.save(ac);

        CacheControl cc = new CacheControl();
        int maxAge = 30;
        cc.setMaxAge(maxAge);

        return Response.created(URI.create(uriInfo.getPath()))
                .entity(new VirtualAccountRepresentation(ac, uriInfo))
                .cacheControl(cc).expires(Date.from(CurrentTime.now().plusSeconds(maxAge)))
                .status(201)
                .type("application/hal+json;concept=virtualaccount;v=2")
                .build();
    }

    Response listServiceGeneration1Version1(UriInfo uriInfo, Request request) {
        List<VirtualAccount> locations = archivist.listAccounts();
        return new EntityResponseBuilder<>(locations, list -> new VirtualAccountsRepresentation(list, uriInfo))
                .name("virtualaccount")
                .version("1")
                .maxAge(10)
                .build(request);
    }

    @LogDuration(limit = 50)
    Response getServiceGeneration1Version1(UriInfo uriInfo, Request request, String accountNo) {
        Long no;
        try {
            no = Long.parseLong(accountNo);
        } catch (NumberFormatException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        VirtualAccount virtualaccount = archivist.getAccount(no);
        LOGGER.info("Usage - application/hal+json;concept=virtualaccount;v=1");
        return new EntityResponseBuilder<>(virtualaccount, ac -> new VirtualAccountRepresentation(ac, uriInfo))
                .name("virtualaccount")
                .version("1")
                .maxAge(120)
                .build(request);
    }

    @LogDuration(limit = 50)
    Response getServiceGeneration1Version2(UriInfo uriInfo, Request request, String accountNo) {
        Long no;
        try {
          no = Long.parseLong(accountNo);
        } catch (NumberFormatException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        VirtualAccount virtualaccount = archivist.getAccount(no);
        LOGGER.info("Usage - application/hal+json;concept=virtualaccount;v=2 - virtualaccount = " + virtualaccount);
        return new EntityResponseBuilder<>(virtualaccount, ac -> new VirtualAccountRepresentation(ac, uriInfo))
                .name("virtualaccount")
                .version("2")
                .maxAge(60)
                .build(request);
    }

    interface VirtualAccountsProducerMethod {
        Response getResponse(UriInfo uriInfo, Request request);
    }

    interface VirtualAccountProducerMethod {
        Response getResponse(UriInfo uriInfo, Request request, String accountNo);
    }

    Response handleUnsupportedContentType(UriInfo uriInfo, Request request, String... parms) {
        return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
    }

}
