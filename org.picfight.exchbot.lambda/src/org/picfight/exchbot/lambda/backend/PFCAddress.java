
package org.picfight.exchbot.lambda.backend;

public class PFCAddress {
	public String address_string;
	public String type;

	public String AddressString () {
		return this.address_string;
	}

	@Override
	public String toString () {
		return this.AddressString();
	}

}
