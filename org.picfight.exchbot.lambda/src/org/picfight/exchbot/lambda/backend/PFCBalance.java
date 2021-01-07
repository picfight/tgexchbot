
package org.picfight.exchbot.lambda.backend;

public class PFCBalance {

	@Override
	public String toString () {
		return "PFCBalance [amount=" + this.AmountPFC + ", address=" + this.PFCAddress + "]";
	}

	public AmountPFC AmountPFC;
	public PFCAddress PFCAddress;
	public String ResolvedAccountName;

}
