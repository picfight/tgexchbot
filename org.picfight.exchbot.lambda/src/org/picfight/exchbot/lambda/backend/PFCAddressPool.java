
package org.picfight.exchbot.lambda.backend;

import java.io.IOException;
import java.util.ArrayList;

public class PFCAddressPool {

	public final ArrayList<PFCAddress> list = new ArrayList<>();

	public PFCAddress obtainNewAddress (final WalletBackEnd walletBackEnd, final String accountName) throws IOException {
		if (this.list.size() > 0) {
			final PFCAddress address = this.list.remove(0);
			return address;
		}
		final PFCAddress address = walletBackEnd.getNewPFCAddress(accountName);
		return address;
	}

}
