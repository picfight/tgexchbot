
package org.picfight.exchbot.lambda.backend;

public class AmountBTC {

	public double value;

	public AmountBTC (final double btcAmount) {
		this.value = btcAmount;
	}

	public AmountBTC () {
	}

	@Override
	public String toString () {
		return this.value + "";
	}

}
