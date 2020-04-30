
package org.picfight.exchbot.lambda.backend;

public class BTCBalance {

	public AmountBTC AmountBTC;
	public BTCAddress BTCAddress;

	@Override
	public String toString () {
		return "BTCBalance [AmountBTC=" + this.AmountBTC + ", BTCAddress=" + this.BTCAddress + "]";
	}

}
