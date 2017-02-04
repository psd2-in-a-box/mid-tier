package dk.sample.rest.bank.connector.snb;

/**
 * Representation of a SNB Account
 * 
 * @author borup
 */
public class SNBAccount {
	String accountNumber; 
  	double balance;
  	//coowners
  	String accountStatus;
  	double creditMax;

	public String toString() {
		return "Number: " + accountNumber + " balance: " + balance + " status: " + accountStatus + " credit: " + creditMax;
	}
}
