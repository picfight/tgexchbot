
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
import com.jfixby.scarabei.api.io.IO;
import com.jfixby.scarabei.api.json.Json;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.sys.settings.ExecutionMode;
import com.jfixby.scarabei.api.sys.settings.SystemSettings;
import com.jfixby.scarabei.red.desktop.ScarabeiDesktop;

public class LambdaEntryPoint implements RequestStreamHandler {
	static {
		ScarabeiDesktop.deploy();
	}

	@Override
	public void handleRequest (final InputStream input, final OutputStream output, final Context context) throws IOException {

		final com.jfixby.scarabei.api.io.InputStream is = IO.newInputStream( () -> input);
		is.open();
		final String data = is.readAllToString();
		is.close();
		L.d("DATA", data);

		final Data dt = Json.deserializeFromString(Data.class, data);
		final ObjectMapper objectReader = new ObjectMapper();
		final Update update = objectReader.readValue(dt.body, Update.class);
// update = objectReader.readValue(input, );
		L.d("Update @ '" + getFormattedTimestamp(update) + "' : " + update);
		L.d("Starting handling update " + update.getUpdateId());
		this.handleUpdate(update);
		L.d("Finished handling update " + update.getUpdateId());

		final String response = "{ \"statusCode\": 200, \"headers\": {\"Content-Type\": \"application/json\"}, \"body\": \"\" }";

		final com.jfixby.scarabei.api.io.OutputStream os = IO.newOutputStream( () -> output);
		os.open();
		os.write(response.getBytes());
		os.flush();
		os.close();

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
		L.d("debug", update);

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
