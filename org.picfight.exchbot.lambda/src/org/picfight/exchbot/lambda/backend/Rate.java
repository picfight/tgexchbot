
package org.picfight.exchbot.lambda.backend;

public class Rate {

	private CirculatingSupply circulatingSupply;
	private BTCperPFC pfcperbtc;
	private AvailablePFC availablePFC;

	public CirculatingSupply getCirculatingSupply () {
		return this.circulatingSupply;
	}

	public BTCperPFC BTCperPFC () {
		return this.pfcperbtc;
	}

	public AvailablePFC availablePFC () {
		return this.availablePFC;
	}

}
