
package org.picfight.exchbot.lambda.backend;

public class AmountBTC {

	public double Value;

	public AmountBTC (final double btcAmount) {
		this.Value = btcAmount;
	}

	public AmountBTC () {
	}

	@Override
	public String toString () {
		return this.Value + " BTC";
	}

}
