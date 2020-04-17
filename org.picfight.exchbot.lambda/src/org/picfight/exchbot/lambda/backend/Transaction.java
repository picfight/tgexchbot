
package org.picfight.exchbot.lambda.backend;

public class Transaction {

	public static final String BUY = "BUY";
	public static final String SELL = "SELL";

	public Long chatID;
	public Long timestamp;

	public String type;
	// BUY
	public PFCAddress clientPFCWallet;
	public BTCAddress exchangeBTCWallet;
	public Double pfcAmount;

	// SELL
	public PFCAddress exchangePFCWallet;
	public BTCAddress clientBTCWallet;
	public Double btcAmount;

	//
	public String userName;
	public String firstName;
	public String lastName;

}
