
package org.picfight.exchbot.lambda;

import java.io.IOException;
import java.io.InputStream;

import org.picfight.exchbot.lambda.backend.UserSettings;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.jfixby.scarabei.api.collections.List;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.strings.Strings;
import com.jfixby.scarabei.api.sys.Sys;

public class Handlers {

	public static boolean handle (final AbsSender bot, final TelegramUpdate update, final String text,
		final FilesystemSetup filesystem) {
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

		final String command = lines.getElementAt(0).toLowerCase();
		lines.removeElementAt(0);

		final Handler h = new TgBotMessageHandler();

		final HandleArgs args = new HandleArgs();
		args.bot = bot;
		args.update = update;
		args.command = command;
		args.arguments = lines;
		args.inputRaw = text;
		args.filesystem = filesystem;

		try {
			L.d("debug args", args);
			return h.handle(args);
		} catch (final IOException e) {
			e.printStackTrace();
			return true;
		}
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
// L.d("Sending message[" + i + "]: " + sendMessage);

				if (bot != null) {
					bot.execute(sendMessage);
					L.d("Message sent: " + sendMessage);
				} else {
					L.d("Resulting message: " + sendMessage.getText());
				}
				return;
			} catch (final TelegramApiException e) {
				x = e;
				e.printStackTrace();
				Sys.sleep(1000);
			}
		}

		throw new IOException(x);
	}

	public static void respond (final AbsSender bot, final Long chatid, final InputStream inputStream, final boolean addlink)
		throws IOException {

		TelegramApiException x = null;
		for (int i = 0; i < 5; i++) {
			try {
				final SendPhoto sendMessage = new SendPhoto().setChatId(chatid).setPhoto("123", inputStream);

				if (bot != null) {
					bot.execute(sendMessage);
					L.d("Message sent: " + sendMessage);
				} else {
					L.d("Resulting message: " + sendMessage);
				}
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
	public FilesystemSetup filesystem;
	public String inputRaw;
	public AbsSender bot;
	public TelegramUpdate update;
	public String command;
	public List<String> arguments;
// public String accountName;
	public UserSettings settings;

	@Override
	public String toString () {
		return "HandleArgs [filesystem=" + this.filesystem + ", inputRaw=" + this.inputRaw + ", bot=" + this.bot + ", update="
			+ this.update + ", command=" + this.command + ", arguments=" + this.arguments + "]";
	}

}

interface Handler {
	public boolean handle (HandleArgs args) throws IOException;
}
