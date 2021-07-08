package dk.sample.rest.bank.virtualaccount.model;

import java.math.BigDecimal;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MicroPlanTest {

    @Test
    public void testNewMimimalPlan() {
        MicroPlan mp = new MicroPlan("testPlan", "A sample savingplan", "11", "1234-1234567890");
        assertEquals("testPlan", mp.getName());
        assertEquals("A sample savingplan", mp.getDescription());
        assertEquals("11", mp.getVirtualAccount());
        assertEquals("1234-1234567890", mp.getPrimaryAccount());
        assertEquals("", mp.getSecondaryAccount());
        assertEquals("", mp.getTertiaryAccount());
    }

    @Test
    public void testNewDualAccountPlan() {
        MicroPlan mp = new MicroPlan("testPlan", "A sample savingplan", "11", "1234-1234567890","2345-0987654321");
        assertEquals("testPlan", mp.getName());
        assertEquals("A sample savingplan", mp.getDescription());
        assertEquals("11", mp.getVirtualAccount());
        assertEquals("1234-1234567890", mp.getPrimaryAccount());
        assertEquals("2345-0987654321", mp.getSecondaryAccount());
        assertEquals("", mp.getTertiaryAccount());
    }

    @Test
    public void testNewTrippleAccountPlan() {
        MicroPlan mp = new MicroPlan("testPlan", "A sample savingplan", "11", "1234-1234567890","2345-0987654321", "3456-123409856");
        assertEquals("testPlan", mp.getName());
        assertEquals("A sample savingplan", mp.getDescription());
        assertEquals("11", mp.getVirtualAccount());
        assertEquals("1234-1234567890", mp.getPrimaryAccount());
        assertEquals("2345-0987654321", mp.getSecondaryAccount());
        assertEquals("3456-123409856", mp.getTertiaryAccount());
    }
}
