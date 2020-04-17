
package org.picfight.exchbot.lambda.backend;

public class AmountPFC {

	public double value;

	public AmountPFC (final double pfcAmount) {
		this.value = pfcAmount;
	}

	public AmountPFC () {
	}

	@Override
	public String toString () {
		return this.value + "";
	}

}
