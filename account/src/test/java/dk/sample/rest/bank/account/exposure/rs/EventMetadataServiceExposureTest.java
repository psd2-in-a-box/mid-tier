package dk.sample.rest.bank.account.exposure.rs;

import java.net.URI;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dk.sample.rest.bank.account.exposure.rs.model.EventsMetadataRepresentation;
import dk.sample.rest.bank.account.persistence.AccountArchivist;
import dk.sample.rest.common.test.rs.UriBuilderFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventMetadataServiceExposureTest {

    @Mock
    AccountArchivist archivist;

    @InjectMocks
    AccountEventFeedMetadataServiceExposure service;

    @Test
    public void testMetadata(){
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));
        Request request = mock(Request.class);
        Response response = service.getMetadata(ui, request, "application/hal+json");
        EventsMetadataRepresentation info = (EventsMetadataRepresentation) response.getEntity();
        assertNotNull(info);
        assertTrue(info.getMetadata().contains("purpose"));
        assertEquals("http://mock/account-events-metadata", info.getSelf().getHref());

        response = service.getMetadata( ui, request, "application/hal+json;concept=metadata");
        assertEquals(415,response.getStatus());
    }

    @Test
    public void testVersionedMetadata(){
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));
        Request request = mock(Request.class);
        Response response = service.getMetadata(ui, request,"application/hal+json;concept=metadata;v=1");
        EventsMetadataRepresentation info = (EventsMetadataRepresentation) response.getEntity();
        assertNotNull(info);
        assertTrue(info.getMetadata().contains("purpose"));
        assertEquals("http://mock/account-events-metadata", info.getSelf().getHref());
    }

}
