package dk.sample.rest.bank.connector;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import dk.nykredit.jackson.dataformat.hal.HALLink;
import dk.nykredit.jackson.dataformat.hal.HALMapper;
import dk.sample.rest.bank.account.model.Account;
import dk.sample.rest.bank.account.persistence.AccountArchivist;
import dk.sample.rest.bank.connector.snb.SNBAccount;
import dk.sample.rest.bank.connector.snb.SNBAccounts;
import dk.sample.rest.bank.connector.snb.SNBTransaction;
import dk.sample.rest.bank.connector.snb.SNBTransactions;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.core.MediaType;

/**
 * Scheduler of jobs.
 */
@Singleton
@Startup
public class SNBScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(SNBScheduler.class);
    private static final Pattern PAGE = Pattern.compile(".*page=(\\d+).*");
    private static final String USERNAME = "user099";
    private static final String PASSWORD = "TSuNHAWuHYwH";

    @EJB
    private AccountArchivist archivist;

    @EJB
    private SNBScheduler self;

    @Schedule(hour = "*", minute = "*/3", second = "0")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
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
        accounts.getAccounts().forEach(account -> {
            try {
                self.handleAccount(client, account);
            } catch (RuntimeException e) {
                LOG.warn("Failed to handle transactions for {}", account.getAccountNumber());
            }
        });
        if (accounts.getNext() != null) {
            readAccounts(client, URI.create(accounts.getNext().getHref()));
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void handleAccount(Client client, SNBAccount account) {
        String reg = account.getAccountNumber().substring(0, 4);
        String konto = account.getAccountNumber().substring(4);
        Optional<Account> exists = archivist.findAccount(reg, konto);
        Account domainAccount = exists.orElse(new Account(reg, konto, String.format("%s-%s", reg, konto),
                account.getOwner().getCustomerNumber()));

        int txCount = domainAccount.getTransactions().size();
        handleTransactions(client, domainAccount, URI.create(account.getTransactions().getHref()));
        domainAccount.setBalance(account.getBalance());

        if (txCount != domainAccount.getTransactions().size()) {
            LOG.info("Added {} new transactions to {}", domainAccount.getTransactions().size() - txCount, domainAccount);
        }

        archivist.save(domainAccount);
    }

    private void handleTransactions(Client client, Account account, URI uri) {
        SNBTransactions transactions = client
                .target(uri)
                .request()
                .get(SNBTransactions.class);
        List<SNBTransaction> transactionList = new ArrayList<>(transactions.getTransactions());

        if (!transactionList.isEmpty()
                && transactionList.get(0).getTransactionDateTimestamp() > account.getLatest().toEpochMilli()) {
            next(transactions.getNext()).ifPresent(nextUri -> handleTransactions(client, account, nextUri));
        }

        Collections.reverse(transactionList);
        transactionList.forEach(transaction -> {
            if (transaction.getTransactionDateTimestamp() > account.getLatest().toEpochMilli()) {
                account.addTransaction(transaction.getId(), transaction.getText(), transaction.getAmount(),
                        Instant.ofEpochMilli(transaction.getTransactionDateTimestamp()));
            }
        });
    }

    private Optional<URI> next(HALLink next) {
        URI nextURI = null;
        if (next != null) {
            nextURI = URI.create(next.getHref());
            Matcher m = PAGE.matcher(nextURI.getQuery());
            if (!m.matches() || Integer.parseInt(m.group(1)) > 2) {
                nextURI = null;
            }
        }
        return Optional.ofNullable(nextURI);
    }

    /**
     * Filter to add authorization header to requests.
     */
    static class Authenticator implements ClientRequestFilter {

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
