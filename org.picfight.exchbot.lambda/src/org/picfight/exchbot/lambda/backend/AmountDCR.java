
package org.picfight.exchbot.lambda.backend;

public class AmountDCR {

	public double Value;

	public AmountDCR (final double pfcAmount) {
		this.Value = pfcAmount;
	}

	public AmountDCR () {
	}

	@Override
	public String toString () {
		return this.Value + " DCR";
	}

}
