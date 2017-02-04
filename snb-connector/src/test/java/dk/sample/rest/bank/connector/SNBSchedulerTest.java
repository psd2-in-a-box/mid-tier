package dk.sample.rest.bank.connector;

import dk.sample.rest.bank.account.persistence.AccountArchivist;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SNBSchedulerTest {

    @Mock
    AccountArchivist archivist;

    @InjectMocks
    SNBScheduler scheduler;

    @Before
    public void setupSelf() throws Exception {
        Field self = SNBScheduler.class.getDeclaredField("self");
        self.setAccessible(true);
        self.set(scheduler, scheduler);
    }

    @Test
    public void testCreateRequest() {
        //scheduler.createRequest();
    }

}
