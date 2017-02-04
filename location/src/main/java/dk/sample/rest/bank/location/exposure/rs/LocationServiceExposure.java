package dk.sample.rest.bank.location.exposure.rs;

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
import javax.validation.Valid;
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

import dk.sample.rest.bank.location.exposure.rs.model.LocationRepresentation;
import dk.sample.rest.bank.location.exposure.rs.model.LocationUpdateRepresentation;
import dk.sample.rest.bank.location.exposure.rs.model.LocationsRepresentation;
import dk.sample.rest.bank.location.model.Location;
import dk.sample.rest.bank.location.persistence.LocationArchivist;

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
 * Exposing location as REST service
 */
@Stateless
@Path("/locations")
@PermitAll
@DeclareRoles("advisor")
@Api(value = "/locations", tags = {"locations"})
public class LocationServiceExposure {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationServiceExposure.class);

    private final Map<String, LocationsProducerMethod> locationsProducers = new HashMap<>();
    private final Map<String, LocationProducerMethod> locationProducers = new HashMap<>();

    @EJB
    private LocationArchivist archivist;


    public LocationServiceExposure() {
        locationsProducers.put("application/hal+json", this::listServiceGeneration1Version1);
        locationsProducers.put("application/hal+json;concept=locations;v=1", this::listServiceGeneration1Version1);

        locationProducers.put("application/hal+json", this::getServiceGeneration1Version2);
        locationProducers.put("application/hal+json;concept=location;v=1", this::getServiceGeneration1Version1);
        locationProducers.put("application/hal+json;concept=location;v=2", this::getServiceGeneration1Version2);
    }

    @GET
    @Produces({"application/hal+json", "application/hal+json;concept=location;v=1"})
    @ApiOperation(value = "lists locations", response = LocationsRepresentation.class,
            authorizations = {
                    @Authorization(value = "oauth2", scopes = {}),
                    @Authorization(value = "oauth2-cc", scopes = {}),
                    @Authorization(value = "oauth2-ac", scopes = {}),
                    @Authorization(value = "oauth2-rop", scopes = {}),
                    @Authorization(value = "Bearer")
            },
            extensions = {@Extension(name = "roles", properties = {
                    @ExtensionProperty(name = "advisor", value = "advisors are allowed getting every location"),
                    @ExtensionProperty(name = "customer", value = "customer only allowed getting own locations")}
            )},
            produces = "application/hal+json, application/hal+json;concept=locations;v=1",
            notes = "List all locations in a default projection, which is Location version 1" +
                    "Supported projections and versions are: " +
                    "Locations in version 1 " +
                    "The Accept header for the default version is application/hal+json;concept=location;v=1.0.0.... " +
                    "The format for the default version is {....}", nickname = "listLocations")
    @ApiResponses(value = {
            @ApiResponse(code = 415, message = "Content type not supported.")
        })
    public Response list(@Context UriInfo uriInfo, @Context Request request, @HeaderParam("Accept") String accept) {
        return locationsProducers.getOrDefault(accept, this::handleUnsupportedContentType).getResponse(uriInfo, request);
    }

    @GET
    @Path("{latitude}-{longitude}")
    @Produces({"application/hal+json", "application/hal+json;concept=location;v=1", "application/hal+json;concept=location;v=2"})
    @ApiOperation(value = "gets the information from a single position", response = LocationRepresentation.class,
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
            produces = "application/hal+json, application/hal+json;concept=location;v=1, application/hal+json;concept=location;v=2",
            notes = "obtain a single customer back in a default projection, which is Location version 2" +
                    " Supported projections and versions are:" +
                    " Location in version1 and Location in version 2" +
                    " The format of the default version is .... - The Accept Header is not marked as required in the " +
                    "swagger - but it is needed - we are working on a solution to that", nickname = "getLocation")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "location not found.")
            })
    public Response get(@Context UriInfo uriInfo, @Context Request request,
                        @PathParam("latitude") @Pattern(regexp = "^[0-9]+.[0-9]+,[0-9]*$") String latitude,
                        @PathParam("longitude") @Pattern(regexp = "^[0-9]+.[0-9]+,[0-9]*$") String longitude,
                        @HeaderParam("Accept") String accept) {
        LOGGER.info("Default version of location collected");
        return locationProducers.getOrDefault(accept, this::handleUnsupportedContentType)
                .getResponse(uriInfo, request, latitude, longitude);
    }

    @PUT
    @RolesAllowed("system")
    @Path("{latitude}-{longitude}")
    @Produces({"application/hal+json"})
    @Consumes("application/json")
    @LogDuration(limit = 50)
    @ApiOperation(value = "Create new or update existing location", response = LocationRepresentation.class,
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
            notes = "PUT is used to create a new location from scratch and may be used to alter the values attached to the location",
            consumes = "application/json",
            produces = "application/hal+json, application/hal+json;concept=location;v=1, application/hal+json;concept=location;v=2",
            nickname = "updateLocation")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Could not update or create the location", response = ErrorRepresentation.class),
            @ApiResponse(code = 415, message = "The content-Type was not supported"),
            @ApiResponse(code = 201, message = "New Location Created", response = LocationRepresentation.class,
                    responseHeaders = {
                            @ResponseHeader(name = "Location", description = "a link to the created resource"),
                            @ResponseHeader(name = "Content-Type", description = "a link to the created resource"),
                            @ResponseHeader(name = "X-Log-Token", description = "an ide for reference purposes in logs etc")
                    })
            })
    public Response createOrUpdate(@Context UriInfo uriInfo, @Context Request request,
                                   @PathParam("latitude") String latitude,
                                   @PathParam("longitude") String longitude,
                                   @ApiParam(value = "position") LocationUpdateRepresentation position) {
        if (!latitude.equals(position.getLatitude()) || (!longitude.equals(position.getLongitude()))) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        Optional<Location> location = archivist.findPosition(latitude, longitude);
        Location loc;
        if (location.isPresent()) {
            loc = location.get();
            loc.setAmplitude(position.getAmplitude());
        } else {
            loc = new Location(position.getLatitude(), position.getLongitude(), position.getAmplitude());
        }
        archivist.save(loc);

        CacheControl cc = new CacheControl();
        int maxAge = 30;
        cc.setMaxAge(maxAge);

        return Response.created(URI.create(uriInfo.getPath()))
                .entity(new LocationRepresentation(loc, uriInfo))
                .cacheControl(cc).expires(Date.from(CurrentTime.now().plusSeconds(maxAge)))
                .status(201)
                .type("application/hal+json;concept=location;v=2")
                .build();
    }

    Response listServiceGeneration1Version1(UriInfo uriInfo, Request request) {
        List<Location> locations = archivist.listLocations();
        return new EntityResponseBuilder<>(locations, list -> new LocationsRepresentation(list, uriInfo))
                .name("locations")
                .version("1")
                .maxAge(10)
                .build(request);
    }

    @LogDuration(limit = 50)
    Response getServiceGeneration1Version1(UriInfo uriInfo, Request request, String latitude, String longitude) {
        Location location = archivist.getLocation(latitude, longitude);
        LOGGER.info("Usage - application/hal+json;concept=location;v=1");
        return new EntityResponseBuilder<>(location, cust -> new LocationRepresentation(cust, uriInfo))
                .name("location")
                .version("1")
                .maxAge(120)
                .build(request);
    }

    @LogDuration(limit = 50)
    Response getServiceGeneration1Version2(UriInfo uriInfo, Request request, String latitude, String longitude) {
        Location location = archivist.getLocation(latitude, longitude);
        LOGGER.info("Usage - application/hal+json;concept=location;v=2 - location = " + location);
        return new EntityResponseBuilder<>(location, loc -> new LocationRepresentation(loc, uriInfo))
                .name("location")
                .version("2")
                .maxAge(60)
                .build(request);
    }

    interface LocationsProducerMethod {
        Response getResponse(UriInfo uriInfo, Request request);
    }

    interface LocationProducerMethod {
        Response getResponse(UriInfo uriInfo, Request request, String latitude, String longitude);
    }

    Response handleUnsupportedContentType(UriInfo uriInfo, Request request, String... parms) {
        return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
    }

}
