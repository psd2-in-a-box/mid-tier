package dk.sample.rest.bank.virtualaccount.exposure.rs;

import dk.sample.rest.bank.virtualaccount.exposure.rs.model.MicroPlanRepresentation;
import dk.sample.rest.bank.virtualaccount.exposure.rs.model.MicroPlanUpdateRepresentation;
import dk.sample.rest.bank.virtualaccount.exposure.rs.model.MicroPlansRepresentation;
import dk.sample.rest.bank.virtualaccount.exposure.rs.model.VirtualAccountRepresentation;
import dk.sample.rest.bank.virtualaccount.exposure.rs.model.VirtualAccountUpdateRepresentation;
import dk.sample.rest.bank.virtualaccount.exposure.rs.model.VirtualAccountsRepresentation;
import dk.sample.rest.bank.virtualaccount.model.MicroPlan;
import dk.sample.rest.bank.virtualaccount.model.VirtualAccount;
import dk.sample.rest.bank.virtualaccount.persistence.VirtualAccountArchivist;
import dk.sample.rest.common.test.rs.UriBuilderFactory;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MicroPlanServiceExposureTest {

    @Mock
    VirtualAccountArchivist archivist;

    @InjectMocks
    MicroPlanServiceExposure service;

    @Test
    public void testList() {
        Request request = mock(Request.class);

        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));

        when(archivist.listPlans())
            .thenReturn(Arrays.asList(
                    new MicroPlan("testPlan", "A sample savingplan", "11", "1234-1234567890"),
                    new MicroPlan("testPlan", "A sample savingplan", "11", "1234-1234567890"),
                    new MicroPlan("testPlan", "A sample savingplan", "11", "1234-1234567890")));

        Response response = service.list(ui, request, "application/hal+json");
        MicroPlansRepresentation plans = (MicroPlansRepresentation) response.getEntity();

        assertEquals(3, plans.getPlans().size());
        assertEquals("http://mock/microplans", plans.getSelf().getHref());

        response = service.list(ui, request, "application/hal+json;concept=non.existing;type");
        assertEquals(415,response.getStatus());

    }

    @Test
    public void testGet() {
        Request request = mock(Request.class);

        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));

        when(archivist.findPlan("testPlan")).thenReturn(
                Optional.of(new MicroPlan("testPlan", "A sample savingplan", "11", "1234-1234567890")));

        MicroPlanRepresentation plan = (MicroPlanRepresentation) service.get(ui, request,
                "testPlan","application/hal+json"
        ).getEntity();

        assertEquals("testPlan", plan.getName());
        assertEquals("A sample savingplan", plan.getDescription());
        assertEquals("http://mock/microplans/" + plan.getName(),
                plan.getSelf().getHref());
        assertEquals("http://mock/virtualaccounts/11", plan.getVirtual().getHref());
        assertEquals("http://mock/accounts/1234-1234567890", plan.getPrimaryAccount().getHref());

        Response response = service.get(ui, request, "1","application/hal+json;concept=location;v=0");
        assertEquals(415,response.getStatus());
    }


    @Test
    public void testCreate() throws Exception {
        Request request = mock(Request.class);
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));
        when(ui.getPath()).thenReturn("http://mock");

        MicroPlanUpdateRepresentation plan = mock(MicroPlanUpdateRepresentation.class);
        when(plan.getName()).thenReturn("newPlan");
        when(plan.getDescription()).thenReturn("describe it");
        when(plan.getVirtualAccount()).thenReturn("5123");
        when(plan.getPrimaryAccount()).thenReturn("4767-98989898989");
        when(plan.getSecondaryAccount()).thenReturn("4767-232323232323");
        when(plan.getTertiaryAccount()).thenReturn("4767-565656565665");

        when(archivist.findPlan("newPlan")).thenReturn(Optional.empty());

        MicroPlanRepresentation resp = (MicroPlanRepresentation) service.createOrUpdate(ui, request,
                "newPlan", plan).getEntity();
        assertEquals("newPlan", resp.getName());
        assertEquals("describe it", plan.getDescription());
        assertEquals("5123", plan.getVirtualAccount());
        assertEquals("4767-98989898989", plan.getPrimaryAccount());
        assertEquals("4767-232323232323", plan.getSecondaryAccount());
        assertEquals("4767-565656565665", plan.getTertiaryAccount());
        assertEquals("http://mock/microplans/" + plan.getName(), resp.getSelf().getHref());
        assertEquals("http://mock/virtualaccounts/" + plan.getVirtualAccount(), resp.getVirtual().getHref());
        assertEquals("http://mock/accounts/" + plan.getPrimaryAccount(), resp.getPrimaryAccount().getHref());
        assertEquals("http://mock/accounts/" + plan.getSecondaryAccount(), resp.getSecondaryAccount().getHref());
        assertEquals("http://mock/accounts/" + plan.getTertiaryAccount(), resp.getTertiaryAccount().getHref());
    }


    @Test
    public void testUpdate() throws Exception {
        Request request = mock(Request.class);
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));
        when(ui.getPath()).thenReturn("http://mock");

        MicroPlan plan = new MicroPlan("testPlan", "A sample savingplan", "11", "1234-1234567890",
                "1234-555566667777", "5555-4567890987");

        MicroPlanUpdateRepresentation planUpdate = mock(MicroPlanUpdateRepresentation.class);
        when(planUpdate.getName()).thenReturn("plantocreate");
        when(planUpdate.getDescription()).thenReturn(" The New Plan to be created");

        when(archivist.findPlan("plantocreate")).thenReturn(Optional.of(plan));

        MicroPlanRepresentation resp = ( MicroPlanRepresentation) service.createOrUpdate(ui, request,
                "plantocreate", planUpdate).getEntity();

        assertEquals(plan.getName(), resp.getName());
        assertEquals(plan.getDescription(), resp.getDescription());
        assertEquals("http://mock/microplans/" + plan.getName(), resp.getSelf().getHref());
        assertEquals("http://mock/virtualaccounts/" + plan.getVirtualAccount(), resp.getVirtual().getHref());
        assertEquals("http://mock/accounts/" + plan.getPrimaryAccount(), resp.getPrimaryAccount().getHref());
        assertEquals("http://mock/accounts/" + plan.getSecondaryAccount(), resp.getSecondaryAccount().getHref());
        assertEquals("http://mock/accounts/" + plan.getTertiaryAccount(), resp.getTertiaryAccount().getHref());
    }


    @Test(expected = WebApplicationException.class)
    public void testCreateInvalidRequest() throws Exception {
        Request request = mock(Request.class);
        UriInfo ui = mock(UriInfo.class);

        MicroPlanUpdateRepresentation account = mock(MicroPlanUpdateRepresentation.class);
        when(account.getName()).thenReturn("noplan");

        service.createOrUpdate(ui, request, "otherplan", account);
        fail("Should have thrown exception before this step");
    }
}
