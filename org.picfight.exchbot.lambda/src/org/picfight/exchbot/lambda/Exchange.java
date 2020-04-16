
package org.picfight.exchbot.lambda;

import org.picfight.exchbot.lambda.backend.AvailableFunds;

import com.jfixby.scarabei.api.names.Names;
import com.jfixby.scarabei.api.sys.settings.SystemSettings;

public class Exchange {
	static public double buyPriceBTC (final AvailableFunds rate) {
		final double exchange_rate = exchangeRateBTC();
		final double margin = Double.parseDouble(SystemSettings.getRequiredStringParameter(Names.newID("EXCHANGE_MARGIN")));

		return exchange_rate * (1.0 + margin / 2.0);
	}

	static public double sellPriceBTC (final AvailableFunds rate) {
		final double exchange_rate = exchangeRateBTC();
		final double margin = Double.parseDouble(SystemSettings.getRequiredStringParameter(Names.newID("EXCHANGE_MARGIN")));
		return exchange_rate * (1.0 - margin / 2.0);
	}

	public static double exchangeRateBTC () {
		final double exchange_rate = Double.parseDouble(SystemSettings.getRequiredStringParameter(Names.newID("EXCHANGE_RATE")));
		return exchange_rate;
	}
}
