
package org.picfight.exchbot.lambda.backend;

public class DCRBalance {

	@Override
	public String toString () {
		return "DCRBalance [amount=" + this.AmountDCR + ", address=" + this.DCRAddress + "]";
	}

	public AmountDCR AmountDCR;
	public DCRAddress DCRAddress;

}
