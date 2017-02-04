package dk.sample.rest.bank.connector.snb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import javax.ejb.Singleton;

/**
 * Feeds Account from SparNordBank API
 *  
 * @author borup
 */
@Singleton
public class SNBAccountFeeder {

	public Iterator<SNBAccount> getIterator() {
		// The returned iterator should wrap a function to iterate thru SNB getAccounts API
		return new ArrayList().iterator();
	}
	
}
