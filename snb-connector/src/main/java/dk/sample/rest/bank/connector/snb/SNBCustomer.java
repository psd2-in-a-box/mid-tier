package dk.sample.rest.bank.connector.snb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.nykredit.jackson.dataformat.hal.annotation.Resource;

/**
 * Represents customer from API.
 */
@Resource
@JsonIgnoreProperties(ignoreUnknown = true)
public class SNBCustomer {

    private String customerNumber;

    public String getCustomerNumber() {
        return customerNumber;
    }

}
