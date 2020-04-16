
package org.picfight.exchbot.lambda.backend;

public class AvailableFunds {

	public Double CirculatingSupplyCoins;
	public Double AvailablePFC;
	public Double BTCperPFC;

	public Double getCirculatingSupply () {
		return this.CirculatingSupplyCoins;
	}

	public Double BTCperPFC () {
		return this.BTCperPFC;
	}

	public Double availablePFC () {
		return this.AvailablePFC;
	}

}
