
package org.picfight.exchbot.lambda;

import java.io.IOException;

import org.picfight.exchbot.lambda.backend.BTCAddress;
import org.picfight.exchbot.lambda.backend.BTCAddressArgs;
import org.picfight.exchbot.lambda.backend.ExchangeBackEnd;
import org.picfight.exchbot.lambda.backend.PFCAddress;
import org.picfight.exchbot.lambda.backend.PFCAddressArgs;

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

	public static final ExchangeBackEnd backEnd = new ExchangeBackEnd();

	/**
	 *
	 */
	@Override
	public boolean handle (final HandleArgs args) throws IOException {

		final boolean flag = SystemSettings.getFlag(Names.newID("disable_bot"));
		final Long chatid = args.update.message.chatID;

		if (flag) {
			Handlers.respond(args.bot, chatid, "Бот отключен.", false);
			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.BUY_PFC)) {
			final BTCAddressArgs a = new BTCAddressArgs();
			final BTCAddress address = backEnd.obtainNewBTCAddress(a);
			Handlers.respond(args.bot, chatid, "Send BTC here: " + address.AddressString(), false);
			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.SELL_PFC)) {
			final PFCAddressArgs a = new PFCAddressArgs();
			final PFCAddress address = backEnd.obtainNewPFCAddress(a);
			Handlers.respond(args.bot, chatid, "Send PFC here: " + address.AddressString(), false);
			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.BUY_PFC_CH)) {
			final BTCAddressArgs a = new BTCAddressArgs();
			final BTCAddress address = backEnd.obtainNewBTCAddress(a);
			Handlers.respond(args.bot, chatid, "Send BTC here: " + address.AddressString(), false);
			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.SELL_PFC_CH)) {
			final PFCAddressArgs a = new PFCAddressArgs();
			final PFCAddress address = backEnd.obtainNewPFCAddress(a);
			Handlers.respond(args.bot, chatid, "Send PFC here: " + address.AddressString(), false);
			return true;
		}

		return false;
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
