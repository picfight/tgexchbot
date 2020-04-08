
package org.picfight.exchbot.lambda;

import java.io.IOException;

import org.picfight.exchbot.lambda.backend.BTCAddress;
import org.picfight.exchbot.lambda.backend.BTCAddressArgs;
import org.picfight.exchbot.lambda.backend.ExchangeBackEnd;
import org.picfight.exchbot.lambda.backend.ExchangeBackEndArgs;
import org.picfight.exchbot.lambda.backend.PFCAddress;
import org.picfight.exchbot.lambda.backend.PFCAddressArgs;
import org.picfight.exchbot.lambda.backend.Rate;

import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.names.Names;
import com.jfixby.scarabei.api.sys.settings.SystemSettings;

public class TgBotMessageHandler implements Handler {
	public static final String WALLET_CHECK = "/walletcheck";
	public static final ExchangeBackEnd backEnd;
	static {
		final ExchangeBackEndArgs args = new ExchangeBackEndArgs();
		args.access_key = SystemSettings.getRequiredStringParameter(Names.newID("ACCESS_KEY"));
		args.host = SystemSettings.getRequiredStringParameter(Names.newID("BACKEND_HOST"));// "https://exchange.picfight.org";
		args.port = Integer.parseInt(SystemSettings.getRequiredStringParameter(Names.newID("BACKEND_PORT")));// "https://exchange.picfight.org";
		backEnd = new ExchangeBackEnd(args);
	}

	@Override
	public boolean handle (final HandleArgs args) throws IOException {

		final boolean flag = Boolean.parseBoolean(SystemSettings.getRequiredStringParameter(Names.newID("disable_bot")));
		final Long chatid = args.update.message.chatID;

		if (flag) {
			Handlers.respond(args.bot, chatid, "Бот отключен.", false);
			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.RATE)) {
			final Rate rate = backEnd.getRate();
			Handlers.respond(args.bot, chatid, "Circulating supply: " + rate.getCirculatingSupply(), false);
			Handlers.respond(args.bot, chatid, "PicfightCoin price in BTC: " + rate.BTCperPFC(), false);
			Handlers.respond(args.bot, chatid, "Available PFC coins: " + rate.availablePFC(), false);
			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.NEW_BTC_ADDRESS)) {
			final BTCAddress address = backEnd.obtainNewBTCAddress(null);
			Handlers.respond(args.bot, chatid, "New BTC Address: " + address, false);
			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.NEW_PFC_ADDRESS)) {
			final PFCAddress address = backEnd.obtainNewPFCAddress(null);
			Handlers.respond(args.bot, chatid, "New PFC Address: " + address, false);
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

		L.e("Command not found", args.command);

		return false;
	}

}
