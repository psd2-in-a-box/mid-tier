package dk.sample.rest.bank.location.exposure.rs.model;

import dk.sample.rest.bank.location.exposure.rs.LocationServiceExposure;
import dk.sample.rest.bank.location.model.Location;
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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a set of locations from the REST service exposure in this default projection.
 */
@Resource
@ApiModel(value = "Locations",
        description = "a list of locations in default projection")
public class LocationsRepresentation {

    @Link
    private HALLink self;

    @EmbeddedResource("locations")
    private Collection<LocationRepresentation> locations;

    public LocationsRepresentation(List<Location> locations, UriInfo uriInfo) {
        this.locations = new ArrayList<>();
        this.locations.addAll(locations.stream()
                .map(location -> new LocationRepresentation(location, uriInfo))
                .collect(Collectors.toList()));
        this.self = new HALLink.Builder(uriInfo.getBaseUriBuilder()
            .path(LocationServiceExposure.class)
            .build())
            .build();
    }

    @ApiModelProperty(
            access = "public",
            name = "self",
            notes = "link to the locations list itself.")
    public HALLink getSelf() {
        return self;
    }

    @ApiModelProperty(
            access = "public",
            name = "locations",
            value = "locations list.")
    public Collection<LocationRepresentation> getLocations() {
        return Collections.unmodifiableCollection(locations);
    }
}
