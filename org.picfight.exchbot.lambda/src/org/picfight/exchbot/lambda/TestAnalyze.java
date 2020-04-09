
package org.picfight.exchbot.lambda;

import java.io.IOException;

import com.jfixby.scarabei.red.desktop.ScarabeiDesktop;

public class TestAnalyze {

	public static void main (final String[] args) throws IOException {
		ScarabeiDesktop.deploy();

		final TgBotMessageHandler h = new TgBotMessageHandler();

		final HandleArgs a = new HandleArgs();
		a.update = new TelegramUpdate();
		a.update.message.chatID = -1L;
// a.command = "/analyze";

		a.inputRaw = "JsFeTx96cSC2MtyBbDV1JV1m6mXwS8h8zhC";
		a.command = a.inputRaw;

		h.handle(a);

	}

}
