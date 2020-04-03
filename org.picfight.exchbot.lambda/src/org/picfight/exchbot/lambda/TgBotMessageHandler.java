
package org.picfight.exchbot.lambda;

import java.io.IOException;

import com.jfixby.scarabei.api.json.Json;
import com.jfixby.scarabei.api.json.JsonString;
import com.jfixby.scarabei.api.names.Names;
import com.jfixby.scarabei.api.net.http.Http;
import com.jfixby.scarabei.api.net.http.HttpCall;
import com.jfixby.scarabei.api.net.http.HttpCallExecutor;
import com.jfixby.scarabei.api.net.http.HttpCallParams;
import com.jfixby.scarabei.api.net.http.HttpCallProgress;
import com.jfixby.scarabei.api.net.http.HttpURL;
import com.jfixby.scarabei.api.net.http.METHOD;
import com.jfixby.scarabei.api.sys.settings.SystemSettings;

public class TgBotMessageHandler implements Handler {
	public static final String WALLET_CHECK = "/walletcheck";

	@Override
	public boolean handle (final HandleArgs args) throws IOException {

		final boolean flag = SystemSettings.getFlag(Names.newID("disable_bot"));
		if (flag) {
			final Long chatid = args.update.message.chatID;
			Handlers.respond(args.bot, chatid, "Бот отключен.", false);
			return true;
		}

		return true;
	}

	public static JsonString retrieve (final HttpURL url) throws IOException {
		final HttpCallParams params = Http.newCallParams();
		params.setURL(url);
		params.setMethod(METHOD.GET);
		final HttpCall call = Http.newCall(params);
		final HttpCallExecutor cx = Http.newCallExecutor();
		final HttpCallProgress progress = cx.execute(call);
		final String response = progress.readResultAsString();
		final JsonString json = Json.newJsonString(response);
		return json;
	}

}
