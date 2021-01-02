
package org.picfight.exchbot.lambda.backend;

public class AmountPFC {

	public double Value;

	public AmountPFC (final double pfcAmount) {
		this.Value = pfcAmount;
	}

	public AmountPFC () {
	}

	@Override
	public String toString () {
		return this.Value + " PFC";
	}

}
