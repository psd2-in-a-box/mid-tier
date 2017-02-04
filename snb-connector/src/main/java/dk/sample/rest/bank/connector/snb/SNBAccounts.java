package dk.sample.rest.bank.connector.snb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.nykredit.jackson.dataformat.hal.HALLink;
import dk.nykredit.jackson.dataformat.hal.annotation.EmbeddedResource;
import dk.nykredit.jackson.dataformat.hal.annotation.Link;
import dk.nykredit.jackson.dataformat.hal.annotation.Resource;
import java.util.List;

/**
 * Accounts collection.
 */
@Resource
@JsonIgnoreProperties(ignoreUnknown = true)
public class SNBAccounts {

    @Link
    private HALLink next;

    @EmbeddedResource
    private List<SNBAccount> accounts;

    public HALLink getNext() {
        return next;
    }

    public List<SNBAccount> getAccounts() {
        return accounts;
    }

}
