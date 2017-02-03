package dk.sample.rest.bank.location.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LocationTest {

    @Test
    public void testNewLocation() {
        Location location = new Location("57.0214422", "9.8906541,16", "34", "234");
        assertEquals("57.0214422", location.getLatitude());
        assertEquals("9.8906541,16", location.getLongitude());
        assertEquals("34", location.getAmplitude());
        assertEquals("234", location.getNumber());
    }
}
