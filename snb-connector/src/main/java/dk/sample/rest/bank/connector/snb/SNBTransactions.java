package dk.sample.rest.bank.connector.snb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.nykredit.jackson.dataformat.hal.HALLink;
import dk.nykredit.jackson.dataformat.hal.annotation.EmbeddedResource;
import dk.nykredit.jackson.dataformat.hal.annotation.Link;
import dk.nykredit.jackson.dataformat.hal.annotation.Resource;
import java.util.List;

/**
 * Represents transactions in the SN API.
 */
@Resource
@JsonIgnoreProperties(ignoreUnknown = true)
public class SNBTransactions {

    @Link
    private HALLink next;

    @EmbeddedResource
    private List<SNBTransaction> transactions;

    public HALLink getNext() {
        return next;
    }

    public List<SNBTransaction> getTransactions() {
        return transactions;
    }

}
