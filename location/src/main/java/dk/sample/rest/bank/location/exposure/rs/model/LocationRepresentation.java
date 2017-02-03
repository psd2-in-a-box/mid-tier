package dk.sample.rest.bank.location.exposure.rs.model;

import dk.sample.rest.bank.location.exposure.rs.LocationServiceExposure;
import javax.ws.rs.core.UriInfo;

import dk.nykredit.jackson.dataformat.hal.HALLink;
import dk.nykredit.jackson.dataformat.hal.annotation.Link;
import dk.nykredit.jackson.dataformat.hal.annotation.Resource;

import dk.sample.rest.bank.location.model.Location;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a single Location as returned from REST service in the default projection.
 */
@Resource
@ApiModel(value = "Location",
        description = "the location")
public class LocationRepresentation {
    private String longitude;
    private String latitude;
    private String amplitude;
    private String number;

    @Link
    private HALLink self;

    @Link
    private HALLink origin;

    public LocationRepresentation(Location location, UriInfo uriInfo) {
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
        this.amplitude = location.getAmplitude();
        this.number = location.getNumber();
        this.self = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                .path(LocationServiceExposure.class)
                .path(LocationServiceExposure.class, "get")
                .build(location.getLatitude(), location.getLongitude()))
                .build();
    }

    @ApiModelProperty(
            access = "public",
            name = "longitude",
            example = "9.8906541,16",
            value = "the longitude of a GPS coordinate.")
    public String getLongitude() {
        return longitude;
    }

    @ApiModelProperty(
            access = "public",
            name = "latitude",
            example = "57.0214422",
            value = "the latitude of a GPS coordinate.")
    public String getLatitude() {
        return latitude;
    }

    @ApiModelProperty(
            access = "public",
            name = "amplitude",
            example = "201",
            value = "the amplitude.")
    public String getAmplitude() {
        return amplitude;
    }

    @ApiModelProperty(
            access = "public",
            name = "number",
            example = "34",
            value = "the number of observations on coordinate.")
    public String getNumber() {
        return number;
    }

    @ApiModelProperty(
            access = "public",
            name = "self",
            notes = "link to the coordinate itself.")
    public HALLink getSelf() {
        return self;
    }
}
