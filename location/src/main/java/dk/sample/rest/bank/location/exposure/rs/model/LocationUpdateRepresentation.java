package dk.sample.rest.bank.location.exposure.rs.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The necessary input for creation of an Location and used for updating an Location.
 */
@ApiModel(value = "LocationUpdate",
        description = "the inout necessary for creating an Location")

public class LocationUpdateRepresentation {

    @NotNull
    //@Pattern(regexp = "^[0-9]+.[0-9]+,[0-9]*")
    private String longitude;

    @NotNull
    //@Pattern(regexp = "^[0-9]+.[0-9]+,[0-9]*")
    private String latitude;

    @NotNull
    @Pattern(regexp = "^[0-9]{10}")
    private String amplitude;

    @NotNull
    @Pattern(regexp = "^[0_9]{10}")
    private String number;


    @ApiModelProperty(
            access = "public",
            name = "latitude",
            required = true,
            example = "57.0214422",
            value = "the latitude for a position")
    public String getLatitude() {
        return latitude;
    }

    @ApiModelProperty(
            access = "public",
            name = "longitude",
            required = true,
            example = "9.8906541,16",
            value = "the longitude name of af position")
    public String getLongitude() {
        return longitude;
    }

    @ApiModelProperty(
            access = "public",
            name = "amplitude",
            required = true,
            example = "23",
            value = "the amplitude of the position.",
            notes = " the weigth for observations for a position")
    public String getAmplitude() {
        return amplitude;
    }

    @ApiModelProperty(
            access = "public",
            name = "number",
            required = true,
            example = "12",
            value = "the number of observations.",
            notes = " the observed events for a given position")
    public String getNumber() {
        return number;
    }


}
