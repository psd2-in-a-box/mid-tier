package dk.sample.rest.bank.connector;

import dk.sample.rest.bank.account.persistence.AccountArchivist;
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
    
    @Test
    public void testCreateRequest() {
        //scheduler.createRequest();
    }
    
}
