
package org.picfight.exchbot.lambda;

import java.io.IOException;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.jfixby.scarabei.api.collections.List;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.strings.Strings;
import com.jfixby.scarabei.api.sys.Sys;

public class Handlers {

	public static boolean handle (final AbsSender bot, final TelegramUpdate update, final String text) {
		if (text == null) {
			return false;
		}
		if ("".equals(text)) {
			return false;
		}

		final List<String> lines = Strings.split(text, " ");

		if (lines.size() == 0) {
			return false;
		}

		final String command = lines.getElementAt(0);
		lines.removeElementAt(0);

		final Handler h = loadHandler();
		if (h == null) {
			return false;
		}

		final HandleArgs args = new HandleArgs();
		args.bot = bot;
		args.update = update;
		args.command = command;
		args.arguments = lines;
		args.inputRaw = text;
// final Runnable r = new Runnable() {
// @Override
// public void run () {
// h.handle(args);
// }
// };
// final Thread t = new Thread(r);
// t.start();
// Sys.sleep(20000);
// return true;

		try {
			L.d("debug args", args);
			return h.handle(args);
		} catch (final IOException e) {
			e.printStackTrace();
			return true;
		}
	}

	private static Handler loadHandler () {
		return new TgBotMessageHandler();
	}

	public static final void respond (final AbsSender bot, final Long chatID, final String responseText) throws IOException {
		respond(bot, chatID, responseText, false);
	}

	public static void respond (final AbsSender bot, final Long chatid, final String responseText, final boolean addlink)
		throws IOException {

		TelegramApiException x = null;
		for (int i = 0; i < 5; i++) {
			try {
				final SendMessage sendMessage = new SendMessage().setChatId(chatid).setText(responseText);
				sendMessage.setParseMode(ParseMode.HTML);
				if (!addlink) {
					sendMessage.disableWebPagePreview();
				}
				// sendMessage.enableMarkdown(true);
				L.d("Sending message[" + i + "]: " + sendMessage);

				bot.execute(sendMessage);
				L.d("Message sent: " + sendMessage);
				return;
			} catch (final TelegramApiException e) {
				x = e;
				e.printStackTrace();
				Sys.sleep(1000);
			}
		}

		throw new IOException(x);
	}

}

class HandleArgs {
	public String inputRaw;
	public AbsSender bot;
	public TelegramUpdate update;
	public String command;
	public List<String> arguments;

	@Override
	public String toString () {
		return "HandleArgs [inputRaw=" + this.inputRaw + ", bot=" + this.bot + ", update=" + this.update + ", command="
			+ this.command + ", arguments=" + this.arguments + "]";
	}

}

interface Handler {
	public boolean handle (HandleArgs args) throws IOException;
}