package dk.sample.rest.bank.virtualaccount.model;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import org.junit.Test;

public class VirtualAccountTest {

    @Test
    public void testNewVirtualAccount() {
        VirtualAccount va = new VirtualAccount(1L, new BigDecimal(10),new BigDecimal(20), new BigDecimal(30));
        assertEquals(1L, va.getVaNumber().longValue());
        assertEquals(new BigDecimal(10), va.getTotalBalance());
        assertEquals(new BigDecimal(20), va.getComittedBalance());
        assertEquals(new BigDecimal(30), va.getUnCommittedBalance());
    }
}
