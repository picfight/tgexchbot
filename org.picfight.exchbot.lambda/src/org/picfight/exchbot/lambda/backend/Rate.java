
package org.picfight.exchbot.lambda.backend;

public class Rate {

	private CirculatingSupply circulatingSupply;
	private PFCtoBTC pfctobtc;
	private AvailablePFC availablePFC;

	public CirculatingSupply getCirculatingSupply () {
		return this.circulatingSupply;
	}

	public PFCtoBTC PFCtoBTC () {
		return this.pfctobtc;
	}

	public AvailablePFC availablePFC () {
		return this.availablePFC;
	}

}
