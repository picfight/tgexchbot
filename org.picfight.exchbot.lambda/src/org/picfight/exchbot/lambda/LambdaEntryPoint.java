
package org.picfight.exchbot.lambda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.sys.settings.ExecutionMode;
import com.jfixby.scarabei.api.sys.settings.SystemSettings;
import com.jfixby.scarabei.red.desktop.ScarabeiDesktop;

public class LambdaEntryPoint implements RequestStreamHandler {
	static {
		ScarabeiDesktop.deploy();
	}
	private static final ObjectMapper objectReader = new ObjectMapper();

	@Override
	public void handleRequest (final InputStream input, final OutputStream output, final Context context) throws IOException {
		Update update;
		update = objectReader.readValue(input, Update.class);
		L.d("Update @ '" + getFormattedTimestamp(update) + "' : " + update);
		L.d("Starting handling update " + update.getUpdateId());
		this.handleUpdate(update);
		L.d("Finished handling update " + update.getUpdateId());

	}

	private void handleUpdate (final Update update) {
		if (update.getMessage() == null) {
			return;
		}

		final ChatRequestResponse requesthandler = new ChatRequestResponse();
		requesthandler.input = this.updatetoTelegramUpdate(update);

		if (ChatBotActionHandler.handler == null) {
			ChatBotActionHandler.handler = new ChatBotActionHandler();
			SystemSettings.setExecutionMode(ExecutionMode.RELEASE_CANDIDATE);
		}

		ChatBotActionHandler.handler.handleRequest(requesthandler);

		L.d("requesthandler", requesthandler);

	}

	private TelegramUpdate updatetoTelegramUpdate (final Update update) {
		final TelegramUpdate u = new TelegramUpdate();
		if (update.hasMessage()) {
			final Message msg = update.getMessage();
			if (msg.hasText()) {
				final String text = msg.getText();
				u.message.text = text;
				u.message.hasText = true;
			}
			u.message.chatID = update.getMessage().getChatId();
		}

		return u;
	}

	private static String getFormattedTimestamp (final Update update) {
		if (update.getMessage() != null) {
			final Instant instant = Instant.ofEpochSecond(update.getMessage().getDate());
			return DateTimeFormatter.ISO_INSTANT.format(instant);
		}
		return "<unknown date>";
	}

}
