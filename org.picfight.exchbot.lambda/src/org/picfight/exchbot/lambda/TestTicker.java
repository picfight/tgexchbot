
package org.picfight.exchbot.lambda;

import java.io.IOException;

import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.red.desktop.ScarabeiDesktop;

public class TestTicker {

	public static void main (final String[] args) throws IOException {
		ScarabeiDesktop.deploy();

		final MarketPair pair = MarketPair.newMarketPair(CoinSign.TETHER, CoinSign.BITCOIN);
		final Ticker ticker = GetTicker.get(pair);

		L.d("ticker", ticker);
	}

}
