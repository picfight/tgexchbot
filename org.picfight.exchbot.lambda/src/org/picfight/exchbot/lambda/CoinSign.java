
package org.picfight.exchbot.lambda;

import com.jfixby.scarabei.api.err.Err;

public enum CoinSign {
	BITCOIN("BTC"), DECRED("DCR"), TETHER("USDT"), DASH("DASH"), VERTCOIN("VTC"), ZCASH("ZEC"), MONA("MONA");

	public final String symbol;

	CoinSign (final String symbol) {
		this.symbol = symbol;
	}

	public static CoinSign resolve (final String s) {
		for (final CoinSign sign : CoinSign.values()) {
			if (s.toLowerCase().equals(sign.symbol.toLowerCase())) {
				return sign;
			}
		}
		Err.reportError("Unknown Symbol " + s);
		return null;
	}

}
