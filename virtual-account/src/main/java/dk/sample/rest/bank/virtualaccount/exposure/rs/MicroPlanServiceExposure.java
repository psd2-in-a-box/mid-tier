package dk.sample.rest.bank.virtualaccount.exposure.rs;

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

import dk.sample.rest.bank.virtualaccount.exposure.rs.model.MicroPlanRepresentation;
import dk.sample.rest.bank.virtualaccount.exposure.rs.model.MicroPlanUpdateRepresentation;
import dk.sample.rest.bank.virtualaccount.exposure.rs.model.MicroPlansRepresentation;
import dk.sample.rest.bank.virtualaccount.model.MicroPlan;
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
@Path("/microplans")
@PermitAll
@DeclareRoles("advisor")
@Api(value = "/microplans", tags = {"microplan"})
public class MicroPlanServiceExposure {
    private static final Logger LOGGER = LoggerFactory.getLogger(MicroPlanServiceExposure.class);
    private static final String ACCOUNT_ROOT = "/accounts/";
    private static final String VIRTUAL_ACCOUNT_ROOT = "/virtualaccounts/";

    private final Map<String, MicroPlansProducerMethod> plansProducer = new HashMap<>();
    private final Map<String, MicroPlanProducerMethod> planProducers = new HashMap<>();


    @EJB
    private VirtualAccountArchivist archivist;


    public MicroPlanServiceExposure() {
        plansProducer.put("application/hal+json", this::listServiceGeneration1Version1);
        plansProducer.put("application/hal+json;concept=microplans;v=1", this::listServiceGeneration1Version1);

        planProducers.put("application/hal+json", this::getServiceGeneration1Version1);
        planProducers.put("application/hal+json;concept=microplan;v=1", this::getServiceGeneration1Version1);
    }

    @GET
    @Produces({"application/hal+json", "application/hal+json;concept=microplans;v=1"})
    @ApiOperation(value = "lists accounts", response = MicroPlansRepresentation.class,
            authorizations = {
                    @Authorization(value = "oauth2", scopes = {}),
                    @Authorization(value = "oauth2-cc", scopes = {}),
                    @Authorization(value = "oauth2-ac", scopes = {}),
                    @Authorization(value = "oauth2-rop", scopes = {}),
                    @Authorization(value = "Bearer")
            },
            extensions = {@Extension(name = "roles", properties = {
                    @ExtensionProperty(name = "advisor", value = "advisors are allowed getting every microplan"),
                    @ExtensionProperty(name = "customer", value = "customer only allowed getting own plans")}
            )},
            produces = "application/hal+json, application/hal+json;concept=microplan;v=1",
            notes = "List all plans in a default projection, which is MicroPlan version 1" +
                    "Supported projections and versions are: " +
                    "MicroPlans in version 1 " +
                    "The Accept header for the default version is application/hal+json;concept=microplans;v=1.... " +
                    "The format for the default version is {....}", nickname = "listMicroPlans")
    @ApiResponses(value = {
            @ApiResponse(code = 415, message = "Content type not supported.")
        })
    public Response list(@Context UriInfo uriInfo, @Context Request request, @HeaderParam("Accept") String accept) {
        return plansProducer.getOrDefault(accept, this::handleUnsupportedContentType).getResponse(uriInfo, request);
    }

    @GET
    @Path("{microplan}")
    @Produces({"application/hal+json", "application/hal+json;concept=microplan;v=1"})
    @ApiOperation(value = "gets the information from a single position", response = MicroPlanRepresentation.class,
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
            produces = "application/hal+json, application/hal+json;concept=microplan;v=1",
            notes = "obtain a single plan " +
                    " Supported projections and versions are:" +
                    " MicroPlan in version1 " +
                    " The format of the default version is .... - The Accept Header is not marked as required in the " +
                    "swagger - but it is needed - we are working on a solution to that", nickname = "getMicroPlan")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "micro plan not found.")
            })
    public Response get(@Context UriInfo uriInfo, @Context Request request,
                        @PathParam("planname") @Pattern(regexp = "^[a_z]{50}$") String planname,
                        @HeaderParam("Accept") String accept) {
        LOGGER.info("Default version of microplan collected");
        return planProducers.getOrDefault(accept, this::handleUnsupportedContentType)
                .getResponse(uriInfo, request, planname);
    }

    @PUT
    @RolesAllowed("system")
    @Path("{microplan}")
    @Produces({"application/hal+json"})
    @Consumes("application/json")
    @LogDuration(limit = 50)
    @ApiOperation(value = "Create new or update existing MicroPlan", response = MicroPlanRepresentation.class,
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
            notes = "PUT is used to create a new microplan or used to alter the values attached to the micro plan account",
            consumes = "application/json",
            produces = "application/hal+json, application/hal+json;concept=microplan;v=1",
            nickname = "updateMicroPlan")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Could not update or create the plan", response = ErrorRepresentation.class),
            @ApiResponse(code = 415, message = "The content-Type was not supported"),
            @ApiResponse(code = 201, message = "New VirtualAccount Created", response = MicroPlanRepresentation.class,
                    responseHeaders = {
                            @ResponseHeader(name = "MicroPlan", description = "a link to the created resource"),
                            @ResponseHeader(name = "Content-Type", description = "a link to the created resource"),
                            @ResponseHeader(name = "X-Log-Token", description = "an id for reference purposes in logs etc")
                    })
            })
    public Response createOrUpdate(@Context UriInfo uriInfo, @Context Request request,
                                   @PathParam("name") @Pattern(regexp = "^[a_z]{50}$") String name,
                                   @ApiParam(value = "plan") MicroPlanUpdateRepresentation plan) {
        if (!name.equals(plan.getName())) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        Optional<MicroPlan> microPlan = archivist.findPlan(name);
        MicroPlan mp;
        if (microPlan.isPresent()) {
            mp = microPlan.get();
            mp.setDescription(plan.getDescription());
        } else {
            mp = new MicroPlan(plan.getName(), plan.getDescription(), plan.getVirtualAccount(),
                    plan.getPrimaryAccount(), plan.getSecondaryAccount(), plan.getTertiaryAccount());
        }
        archivist.save(mp);

        CacheControl cc = new CacheControl();
        int maxAge = 30;
        cc.setMaxAge(maxAge);

        return Response.created(URI.create(uriInfo.getPath()))
                .entity(new MicroPlanRepresentation(mp, uriInfo))
                .cacheControl(cc).expires(Date.from(CurrentTime.now().plusSeconds(maxAge)))
                .status(201)
                .type("application/hal+json;concept=MicroPlan;v=1")
                .build();
    }

    Response listServiceGeneration1Version1(UriInfo uriInfo, Request request) {
        List<MicroPlan> plans = archivist.listPlans();
        return new EntityResponseBuilder<>(plans, list -> new MicroPlansRepresentation(list, uriInfo))
                .name("microplan")
                .version("1")
                .maxAge(10)
                .build(request);
    }

    Response getServiceGeneration1Version1(UriInfo uriInfo, Request request, String planname) {
        Optional<MicroPlan> plan = archivist.findPlan(planname);
        if (plan.isPresent()) {
            MicroPlan p = plan.get();
            return new EntityResponseBuilder<>(p, pl -> new MicroPlanRepresentation(pl, uriInfo))
                    .name("microplan")
                    .version("1")
                    .maxAge(10)
                    .build(request);
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    interface MicroPlansProducerMethod {
        Response getResponse(UriInfo uriInfo, Request request);
    }

    interface MicroPlanProducerMethod {
        Response getResponse(UriInfo uriInfo, Request request, String name);
    }

    Response handleUnsupportedContentType(UriInfo uriInfo, Request request, String... parms) {
        return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
    }

}
