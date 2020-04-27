
package org.picfight.exchbot.lambda.backend;

public class Operation {

	public static final String BUY = "BUY";
	public static final String SELL = "SELL";

	public Long chatID;

	public Long timestamp;

	public String type;
	// BUY
	public PFCAddress clientPFCWallet;
	public BTCAddress exchangeBTCWallet;
	public AmountPFC pfcAmount;

	// SELL
	public PFCAddress exchangePFCWallet;
	public BTCAddress clientBTCWallet;
	public AmountBTC btcAmount;

	//
	public String userName;
	public String firstName;
	public String lastName;

}
