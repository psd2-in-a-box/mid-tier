package dk.sample.rest.bank.connector;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import dk.nykredit.jackson.dataformat.hal.HALMapper;
import dk.sample.rest.bank.account.model.Account;
import dk.sample.rest.bank.account.persistence.AccountArchivist;
import dk.sample.rest.bank.connector.snb.SNBAccount;
import dk.sample.rest.bank.connector.snb.SNBAccounts;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientRequestContext;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ws.rs.core.MediaType;

/**
 * Scheduler of jobs.
 */
@Singleton
@Startup
public class SNBScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(SNBScheduler.class);
    private static final String USERNAME = "user099";
    private static final String PASSWORD = "TSuNHAWuHYwH";

    @EJB
    private AccountArchivist archivist;

    @Schedule(hour = "*", minute = "*/2", second = "0")
    public void doTimeout() {
        LOG.info("Running account poll");
        createRequest();
        LOG.info("...finished run");
    }

    public void createRequest() {
        try {
            Client client = ClientBuilder.newBuilder()
                    .register(new JacksonJsonProvider(new HALMapper()))
                    .register(new Authenticator(USERNAME, PASSWORD))
                    .build();
            readAccounts(client, URI.create("http://api.futurefinance.io/api/accounts"));
        } catch (Exception e) {
            LOG.error("Exception synchronizing SparNord accounts", e);
        }
    }

    private void readAccounts(Client client, URI uri) {
        Response response = client
                .target(uri)
                .request(MediaType.APPLICATION_JSON)
                .get();
        SNBAccounts accounts = response.readEntity(SNBAccounts.class);
        accounts.getAccounts().forEach(this::handleAccount);
        if (accounts.getNext() != null) {
            readAccounts(client, URI.create(accounts.getNext().getHref()));
        }
    }

    private void handleAccount(SNBAccount account) {
        String reg = account.getAccountNumber().substring(0, 4);
        String konto = account.getAccountNumber().substring(4);
        Optional<Account> exists = archivist.findAccount(reg, konto);
        Account domainAccount = exists.orElse(new Account(reg, konto, String.format("%s-%s", reg, konto),
                account.getOwner().getCustomerNumber()));
        domainAccount.setBalance(account.getBalance());
        archivist.save(domainAccount);
    }

    /**
     * Filter to add authorization header to requests.
     */
    class Authenticator implements ClientRequestFilter {

        private final String credentials;

        public Authenticator(String user, String password) {
            credentials = Base64.getEncoder().encodeToString(String.format("%s:%s", user, password).getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.getHeaders().putSingle("Authorization", String.format("Basic %s", credentials));
        }

    }
}