
package org.picfight.exchbot.lambda;

import java.io.IOException;

import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.red.desktop.ScarabeiDesktop;

public class MarketTest {

	public static void main (final String[] args) throws IOException {
		ScarabeiDesktop.deploy();

		final double dcr_for_1_pfc = 0.5 / 1500;
		final double usd_for_1_pfc = TgBotMessageHandler.usd_for_1_pfc(dcr_for_1_pfc);
		L.d("1 PFC стоит " + TgBotMessageHandler.round(dcr_for_1_pfc) + " DCR или $" + TgBotMessageHandler.round(usd_for_1_pfc)
			+ "");

	}

}
