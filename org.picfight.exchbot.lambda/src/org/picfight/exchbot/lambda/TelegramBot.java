
package org.picfight.exchbot.lambda;

import java.util.function.Function;

import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.jfixby.scarabei.api.debug.Debug;

public class TelegramBot extends TelegramWebhookBot {
	String token;
	String username;
	String path;
	Function<Update, BotApiMethod> onUpdate = x -> null;

	public TelegramBot (final TelegramBotSpecs specs) {
		this.token = Debug.checkNull("TELEGRAM_BOT_TOKEN", specs.token);
		this.username = Debug.checkNull("TELEGRAM_BOT_USERNAME", specs.botusername);
		if (specs.onUpdate != null) {
			this.onUpdate = specs.onUpdate;
		}
	}

	@Override
	public String getBotToken () {
		return this.token;
	}

	@Override
	public BotApiMethod onWebhookUpdateReceived (final Update update) {
		return this.onUpdate.apply(update);
	}

	@Override
	public String getBotUsername () {
		return this.username;
	}

	@Override
	public String getBotPath () {
		return this.path;
	}
}
