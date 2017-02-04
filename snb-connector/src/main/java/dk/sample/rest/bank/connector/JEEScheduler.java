package dk.sample.rest.bank.connector;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.annotation.Resource;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientRequestContext;
import java.io.IOException;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;

/**
 * Scheduler of jobs
 * 
 * @author borup
 */
@Singleton
@Startup
public class JEEScheduler {
	private static final Logger LOG = LoggerFactory.getLogger(JEEScheduler.class);

	@Resource
	private TimerService timerService;
	
	@PostConstruct
	public void doStartup() {
		Timer timer = timerService.createSingleActionTimer(1000 * 10, new TimerConfig());
		LOG.error("Scheduled timer for " + timer.getNextTimeout());
	}
	
	@Timeout
	public void doTimeout(Timer timer) {
		LOG.error("Timeout - " + timer);
		createRequest();
	}
	
	public void createRequest() {
	    String username = "user099";
    	String password = "TSuNHAWuHYwH";

		try {
    	Client client = ClientBuilder.newClient();
	    Response response = client.register(new Authenticator(username, password))
			.target("http://api.futurefinance.io/api/accounts/4574000000")
			.request()
			.get();
    	LOG.error("RESPONSE - " + response);
		} catch (Exception e) {
			LOG.error("EXCEPTION: " + e);
		}
	}

	class Authenticator implements ClientRequestFilter {
	    private final String user;
    	private final String password;

    	public Authenticator(String user, String password) {
        	this.user = user;
        	this.password = password;
    	}

    	public void filter(ClientRequestContext requestContext) throws IOException {
        	MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        	final String basicAuthentication = getBasicAuthentication();
        	headers.add("Authorization", basicAuthentication);
	    }

    	private String getBasicAuthentication() {
        	String token = this.user + ":" + this.password;
        	try {
            	return "BASIC " + DatatypeConverter.printBase64Binary(token.getBytes("UTF-8"));
        	} catch (UnsupportedEncodingException ex) {
            	throw new IllegalStateException("Cannot encode with UTF-8", ex);
        	}
    	}
	}
}
