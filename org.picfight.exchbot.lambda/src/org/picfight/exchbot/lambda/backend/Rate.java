
package org.picfight.exchbot.lambda.backend;

public class Rate {

	private CirculatingSupply circulatingSupply;
	private org.picfight.exchbot.lambda.backend.PFCtoUSD pfctousd;
	private AvailablePFC availablePFC;

	public CirculatingSupply getCirculatingSupply () {
		return this.circulatingSupply;
	}

	public PFCtoUSD PFCtoUSD () {
		return this.pfctousd;
	}

	public AvailablePFC availablePFC () {
		return this.availablePFC;
	}

}
