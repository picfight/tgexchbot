
package org.picfight.exchbot.lambda.backend;

import java.util.ArrayList;

public class BTCAddressPool {

	public final ArrayList<BTCAddress> list = new ArrayList<>();

	public BTCAddress obtainNewAddress (final WalletBackEnd walletBackEnd, final String accountName) throws BackendException {
		if (this.list.size() > 0) {
			final BTCAddress address = this.list.remove(0);
			return address;
		}
		final BTCAddress address = walletBackEnd.getNewBTCAddress(accountName);
		return address;
	}

}
