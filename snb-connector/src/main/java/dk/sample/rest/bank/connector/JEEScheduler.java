package dk.sample.rest.bank.connector;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler of jobs
 * 
 * @author borup
 */
@Singleton
@Startup
public class JEEScheduler {
	private static final Logger LOG = LoggerFactory.getLogger(JEEScheduler.class);

	@Inject
	private TimerService timerService;
	
	@PostConstruct
	public void doStartup() {
		Timer timer = timerService.createSingleActionTimer(1000 * 10, new TimerConfig());
		LOG.debug("Scheduled timer for " + timer.getNextTimeout());
	}
	
	@Timeout
	public void doTimeout(Timer timer) {
		LOG.debug("Timeout - " + timer);
		// Schedule account-sourcer
	}
	
}
