
package org.picfight.exchbot.lambda.backend;

public class AvailableFunds {

	public AmountPFC CirculatingSupplyCoins;
	public AmountPFC AvailablePFC;
	public AmountBTC AvailableBTC;
	public Double BTCperPFC;

	public AmountPFC availablePFC () {
		return this.AvailablePFC;
	}

}
