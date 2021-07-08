package dk.sample.rest.bank.virtualaccount;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import dk.sample.rest.common.core.diagnostic.ContextInfo;
import dk.sample.rest.common.core.diagnostic.DiagnosticContext;
import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("unchecked")
public class VirtualAccountServiceExposureIT {

    private DiagnosticContext dCtx;

    @Before
    public void setupLogToken() {
        dCtx = new DiagnosticContext(new ContextInfo() {
            @Override
            public String getLogToken() {
                return "junit-" + System.currentTimeMillis();
            }

            @Override
            public void setLogToken(String s) {

            }
        });
        dCtx.start();
    }

    @After
    public void removeLogToken() {
        dCtx.stop();
    }

    @Test(expected = WebApplicationException.class)
    public void testListAccounts() {
        WebTarget target = ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/sample");
        Map<String, Object> response = target.path("virtualaccounts")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .get(Map.class);

        fail("Should not find anything");
    }

    @Test(expected = WebApplicationException.class)
    public void testListPlans() {
        WebTarget target = ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/sample");
        Map<String, Object> response = target.path("microplans")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .get(Map.class);

        fail("Should not find anything");
    }
}
