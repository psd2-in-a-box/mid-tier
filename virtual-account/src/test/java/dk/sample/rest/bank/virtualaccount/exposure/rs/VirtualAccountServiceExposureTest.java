package dk.sample.rest.bank.virtualaccount.exposure.rs;

import dk.sample.rest.bank.virtualaccount.exposure.rs.model.VirtualAccountRepresentation;
import dk.sample.rest.bank.virtualaccount.exposure.rs.model.VirtualAccountUpdateRepresentation;
import dk.sample.rest.bank.virtualaccount.exposure.rs.model.VirtualAccountsRepresentation;
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
public class VirtualAccountServiceExposureTest {

    @Mock
    VirtualAccountArchivist archivist;

    @InjectMocks
    VirtualAccountServiceExposure service;

    @Test
    public void testList() {
        Request request = mock(Request.class);

        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));

        when(archivist.listAccounts())
            .thenReturn(Arrays.asList(
                new VirtualAccount(1L, new BigDecimal(10), new BigDecimal(20), new BigDecimal(30)),
                new VirtualAccount(2L, new BigDecimal(11), new BigDecimal(21), new BigDecimal(31)),
                new VirtualAccount(3L, new BigDecimal(12), new BigDecimal(22), new BigDecimal(32))));

        Response response = service.list(ui, request, "application/hal+json");
        VirtualAccountsRepresentation accounts = (VirtualAccountsRepresentation) response.getEntity();

        assertEquals(3, accounts.getAccounts().size());
        assertEquals("http://mock/virtualaccounts", accounts.getSelf().getHref());

        response = service.list(ui, request, "application/hal+json;concept=non.existing;type");
        assertEquals(415,response.getStatus());

    }

    @Test
    public void testGet() {
        Request request = mock(Request.class);

        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));

        when(archivist.getAccount(1L)).thenReturn(
                new VirtualAccount(1L, new BigDecimal(10), new BigDecimal(20), new BigDecimal(30)));

        VirtualAccountRepresentation account = (VirtualAccountRepresentation) service.get(ui, request,
                "1","application/hal+json"
        ).getEntity();

        assertEquals("1", account.getVaNumber());
        assertEquals("10", account.getTotalBalance());
        assertEquals("30", account.getUncommittedBalance());
        assertEquals("http://mock/virtualaccounts/" + account.getVaNumber(),
                account.getSelf().getHref());

        Response response = service.get(ui, request, "1","application/hal+json;concept=location;v=0");
        assertEquals(415,response.getStatus());

    }

    @Test
    public void testCreate() throws Exception {
        Request request = mock(Request.class);
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));
        when(ui.getPath()).thenReturn("http://mock");

        VirtualAccountUpdateRepresentation account = mock(VirtualAccountUpdateRepresentation.class);
        when(account.getVaNumber()).thenReturn("34");
        when(account.getTotalBalance()).thenReturn("9890");
        when(account.getCommittedBalance()).thenReturn("5123");
        when(account.getUnCommittedBalance()).thenReturn("4767");

        when(archivist.findAccountByAccountNumber(34L)).thenReturn(Optional.empty());

        VirtualAccountRepresentation resp = (VirtualAccountRepresentation) service.createOrUpdate(ui, request,
                "34", account).getEntity();
        assertEquals("34", resp.getVaNumber());
        assertEquals("9890", account.getTotalBalance());
        assertEquals("5123", account.getCommittedBalance());
        assertEquals("4767", account.getUnCommittedBalance());
        assertEquals("http://mock/virtualaccounts/" + resp.getVaNumber(), resp.getSelf().getHref());
    }

    @Test
    public void testUpdate() throws Exception {
        Request request = mock(Request.class);
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));
        when(ui.getPath()).thenReturn("http://mock");

        VirtualAccount account = new VirtualAccount(1L, new BigDecimal(10), new BigDecimal(20), new BigDecimal(30));

        VirtualAccountUpdateRepresentation accountlocationUpdate = mock(VirtualAccountUpdateRepresentation.class);
        when(accountlocationUpdate.getVaNumber()).thenReturn("55");
        when(accountlocationUpdate.getUnCommittedBalance()).thenReturn("5555");

        when(archivist.findAccountByAccountNumber(55L)).thenReturn(Optional.of(account));

        VirtualAccountRepresentation resp = ( VirtualAccountRepresentation) service.createOrUpdate(ui, request,
                "55", accountlocationUpdate).getEntity();

        assertEquals(account.getVaNumber().toString(), resp.getVaNumber());
        assertEquals(account.getTotalBalance().toString(), resp.getTotalBalance());
        assertEquals(account.getComittedBalance().toString(), resp.getCommittedBalance());
        assertEquals(account.getUnCommittedBalance().toString(), resp.getUncommittedBalance());

        assertEquals("http://mock/virtualaccounts/" + resp.getVaNumber(), resp.getSelf().getHref());
    }

    @Test(expected = WebApplicationException.class)
    public void testCreateInvalidRequest() throws Exception {
        Request request = mock(Request.class);
        UriInfo ui = mock(UriInfo.class);

        VirtualAccountUpdateRepresentation account = mock(VirtualAccountUpdateRepresentation.class);
        when(account.getVaNumber()).thenReturn("1");

        service.createOrUpdate(ui, request, "2", account);
        fail("Should have thrown exception before this step");
    }
}
