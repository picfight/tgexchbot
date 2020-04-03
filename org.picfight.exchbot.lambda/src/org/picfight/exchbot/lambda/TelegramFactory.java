
package org.picfight.exchbot.lambda;

import org.telegram.telegrambots.meta.bots.AbsSender;

public class TelegramFactory {

	public static AbsSender newBot (final TelegramBotSpecs specs) {
		return new TelegramBot(specs);
	}
}
