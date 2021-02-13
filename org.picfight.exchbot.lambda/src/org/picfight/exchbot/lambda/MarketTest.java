
package org.picfight.exchbot.lambda;

import java.io.IOException;

import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.red.desktop.ScarabeiDesktop;

public class MarketTest {

	public static void main (final String[] args) throws IOException {
		ScarabeiDesktop.deploy();

		{
			final double usd_for_1_btc = TgBotMessageHandler.usd_for_btc(1);
			L.d("1 BTC стоит " + TgBotMessageHandler.round(usd_for_1_btc, 8) + "$");
		}
		{
			final double btc_for_1_dcr = TgBotMessageHandler.btc_for_dcr(1);
			final double usd_for_1_dcr = TgBotMessageHandler.usd_for_dcr(1);
			L.d("1 DCR стоит " + TgBotMessageHandler.round(btc_for_1_dcr, 8) + " BTC или $"
				+ TgBotMessageHandler.round(usd_for_1_dcr, 2) + "");
		}

		{
			final double dcr_for_1_pfc = 0.00313658;
			final double usd_for_1_pfc = TgBotMessageHandler.usd_for_1_pfc(dcr_for_1_pfc);
			L.d("1 PFC стоит " + TgBotMessageHandler.round(dcr_for_1_pfc, 8) + " DCR или $"
				+ TgBotMessageHandler.round(usd_for_1_pfc, 2) + "");
		}

	}

}
