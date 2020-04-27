
package org.picfight.exchbot.lambda;

import org.picfight.exchbot.lambda.backend.AvailableFunds;

public class Exchange {
	static public double buyPriceBTC (final AvailableFunds rate) {
		final double exchange_rate = rate.ExchangeRate;
		final double margin = rate.ExchangeMargin;
		return exchange_rate * (1.0 + margin / 2.0);
	}

	static public double sellPriceBTC (final AvailableFunds rate) {
		final double exchange_rate = rate.ExchangeRate;
		final double margin = rate.ExchangeMargin;
		return exchange_rate * (1.0 - margin / 2.0);
	}
}
