
package org.picfight.exchbot.lambda.backend;

public class PFCAddress {
	public String AddressString;
	public String Type;

	public String AddressString () {
		return this.AddressString;
	}

	@Override
	public String toString () {
		return this.AddressString();
	}

}
