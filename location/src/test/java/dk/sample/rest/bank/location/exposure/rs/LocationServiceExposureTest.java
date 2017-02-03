package dk.sample.rest.bank.location.exposure.rs;

import dk.sample.rest.bank.location.exposure.rs.model.LocationUpdateRepresentation;
import dk.sample.rest.bank.location.exposure.rs.model.LocationsRepresentation;
import dk.sample.rest.bank.location.exposure.rs.model.LocationRepresentation;
import dk.sample.rest.bank.location.model.Location;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dk.sample.rest.bank.location.persistence.LocationArchivist;
import dk.sample.rest.common.test.rs.UriBuilderFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LocationServiceExposureTest {

    @Mock
    LocationArchivist archivist;

    @InjectMocks
    LocationServiceExposure service;

    @Test
    public void testList() {
        Request request = mock(Request.class);

        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));

        when(archivist.listLocations())
            .thenReturn(Arrays.asList(
                    new Location("57.0218822", "9.9006541,16", "4"),
                    new Location("57.0214422", "9.8906541,16", "2")));

        Response response = service.list(ui, request, "application/hal+json");
        LocationsRepresentation locations = (LocationsRepresentation) response.getEntity();

        assertEquals(2, locations.getLocations().size());
        assertEquals("http://mock/locations", locations.getSelf().getHref());

        response = service.list(ui, request, "application/hal+json;concept=non.existing;type");
        assertEquals(415,response.getStatus());

    }

    @Test
    public void testGet() {
        Request request = mock(Request.class);

        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));
        when(ui.getPath()).thenReturn("http://mock");

        when(archivist.getLocation("57.0214422", "9.8906541,16")).thenReturn(new Location("57.0214422", "9.8906541,16", "2"));

        LocationRepresentation location = (LocationRepresentation) service.get(ui, request,
                "57.0214422", "9.8906541,16",
                "application/hal+json"
        ).getEntity();

        assertEquals("57.0214422", location.getLatitude());
        assertEquals("9.8906541,16", location.getLongitude());
        assertEquals("2", location.getAmplitude());
        assertEquals("1", location.getNumber());
        assertEquals("http://mock/locations/" + location.getLatitude() + "-" + location.getLongitude(),
                location.getSelf().getHref());

        Response response = service.get(ui, request, "57.0214422", "9.8906541,16",
                "application/hal+json;concept=location;v=0");
        assertEquals(415,response.getStatus());

    }

    @Test
    public void testCreate() throws Exception {
        Request request = mock(Request.class);
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));
        when(ui.getPath()).thenReturn("http://mock");

        LocationUpdateRepresentation loc = mock(LocationUpdateRepresentation.class);
        when(loc.getLatitude()).thenReturn("57.0214422");
        when(loc.getLongitude()).thenReturn("9.8906541,16");
        when(loc.getAmplitude()).thenReturn("56");
        when(loc.getNumber()).thenReturn("12");

        when(archivist.findPosition("57.0214422", "9.8906541,16")).thenReturn(Optional.empty());

        LocationRepresentation resp = (LocationRepresentation) service.createOrUpdate(ui, request,
                "57.0214422", "9.8906541,16",
                loc).getEntity();

        assertEquals("57.0214422", resp.getLatitude());
        assertEquals("9.8906541,16", resp.getLongitude());
        assertEquals("56", resp.getAmplitude());
        assertEquals("1", resp.getNumber());
        assertEquals("http://mock/locations/" + resp.getLatitude() + "-" + resp.getLongitude(), resp.getSelf().getHref());
    }

    @Test
    public void testUpdate() throws Exception {
        Request request = mock(Request.class);
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));
        when(ui.getPath()).thenReturn("http://mock");

        Location location = new Location("57.0214422", "9.8906541,16", "2");

        LocationUpdateRepresentation locationUpdate = mock(LocationUpdateRepresentation.class);
        when(locationUpdate.getLatitude()).thenReturn("57.0214422");
        when(locationUpdate.getLongitude()).thenReturn("9.8906541,16");
        when(locationUpdate.getAmplitude()).thenReturn("2");
        when(locationUpdate.getNumber()).thenReturn("1");

        when(archivist.findPosition("57.0214422", "9.8906541,16")).thenReturn(Optional.of(location));

        LocationRepresentation resp = (LocationRepresentation) service.createOrUpdate(ui, request,
                "57.0214422", "9.8906541,16", locationUpdate).getEntity();

        assertEquals(location.getLatitude(), resp.getLatitude());
        assertEquals(location.getLongitude(), resp.getLongitude());
        assertEquals(location.getAmplitude(), resp.getAmplitude());
        assertEquals(location.getNumber(), resp.getNumber());

        assertEquals("http://mock/locations/" + resp.getLatitude() + "-" + resp.getLongitude(), resp.getSelf().getHref());
    }

    @Test(expected = WebApplicationException.class)
    public void testCreateInvalidRequest() throws Exception {
        Request request = mock(Request.class);
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));
        when(ui.getPath()).thenReturn("http://mock");

        LocationUpdateRepresentation locationUpdate = mock(LocationUpdateRepresentation.class);
        when(locationUpdate.getLatitude()).thenReturn("57.0214422");

        service.createOrUpdate(ui, request, "57.0214422", "9.8906541,16", locationUpdate);
        fail("Should have thrown exception before this step");
    }
}
