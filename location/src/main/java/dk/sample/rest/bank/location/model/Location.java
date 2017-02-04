package dk.sample.rest.bank.location.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import dk.sample.rest.common.persistence.jpa.AbstractAuditable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Very basic modelling of location concept to show the basic use of JPA for persistence handling.
 */
@Entity
@Table(name = "LOCATION", uniqueConstraints = @UniqueConstraint(columnNames = { "LONGITUDE", "LATITUDE" }))
public class Location extends AbstractAuditable {
    /**
     * TID - the technical unique identifier for instance, i.e., primary key. This should NEVER EVER be
     * exposed out side the service since it is a key very internal to this service.
     */
    @Id
    @Column(name = "TID", length = 36, nullable = false, columnDefinition = "CHAR(36)")
    private String tId;

    @Column(name = "LONGITUDE", length = 60, nullable = false)
    private String longitude;

    @Column(name = "LATITUDE", length = 60, nullable = false)
    private String latitude;

    @Column(name = "AMPLITUDE", length = 60, nullable = false)
    private String amplitude;

    @Column(name = "NUMBER", length = 10, nullable = false, columnDefinition = "CHAR(10)")
    private String number;

    protected Location() {
        // Required by JPA
    }

    public Location(String latitude, String longitude, String amplitude, String number) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.amplitude = amplitude;
        this.number = number;
        tId = UUID.randomUUID().toString();
    }

    public Location(String latitude, String longitude, String amplitude) {
        this(latitude, longitude, amplitude, "1");
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(String amplitude) {
        this.amplitude = amplitude;
    }

    public String getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("latitude", latitude)
            .append("longitude", longitude)
            .append("amplitude", amplitude)
            .append("number", number)
            .toString();
    }

}
