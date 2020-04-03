
package org.picfight.exchbot.lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.telegram.telegrambots.meta.bots.AbsSender;

import com.jfixby.scarabei.api.debug.Debug;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.names.ID;
import com.jfixby.scarabei.api.names.Names;
import com.jfixby.scarabei.api.sys.settings.SystemSettings;

public class ChatBotActionHandler {

	private static final ID TELEGRAM_BOT_TOKEN = Names.newID("TELEGRAM_BOT_TOKEN");
	private static final ID TELEGRAM_BOT_USERNAME = Names.newID("TELEGRAM_BOT_USERNAME");

	private final AbsSender bot;

	public ChatBotActionHandler () {
		final String token = SystemSettings.getStringParameter(TELEGRAM_BOT_TOKEN, null);
		final String username = SystemSettings.getStringParameter(TELEGRAM_BOT_USERNAME, null);

		final TelegramBotSpecs specs = new TelegramBotSpecs();
		specs.token = token;
		specs.botusername = username;

		this.bot = TelegramFactory.newBot(specs);

		bind();

	}

	public static final void bind () {
		try {
			final String gateway = "https://17qhk58f54.execute-api.eu-central-1.amazonaws.com/default/exchangebot_picfight_org";
			final ID id = Names.newID("TELEGRAM_BOT_TOKEN");
			final String token = SystemSettings.getStringParameter(id, null);
			Debug.checkNull(id.toString(), token);
			final URL yahoo = new URL("https://api.telegram.org/bot" + token + "/setWebhook?url=" + gateway);
			final URLConnection yc = yahoo.openConnection();
			final BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				L.d("SET WEBHOOK", inputLine);
			}
			in.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		//
	}

	public static ChatBotActionHandler handler;

	public void handleRequest (final ChatRequestResponse requesthandler) {
		final TelegramUpdate update = requesthandler.input;
		final String text = update.message.text.toLowerCase();
		final Long chatid = update.message.chatID;
		final boolean success = Handlers.handle(this.bot, update, text);
		if (success) {
			return;
		}
		try {
			Handlers.respond(this.bot, chatid, "Echo: " + text);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
