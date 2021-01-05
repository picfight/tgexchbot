
package org.picfight.exchbot.lambda.backend;

public class DCRAddress {
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
