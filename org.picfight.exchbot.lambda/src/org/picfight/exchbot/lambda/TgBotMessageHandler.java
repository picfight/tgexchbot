
package org.picfight.exchbot.lambda;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

import org.picfight.exchbot.lambda.backend.AvailableFunds;
import org.picfight.exchbot.lambda.backend.BTCAddress;
import org.picfight.exchbot.lambda.backend.BTCBalance;
import org.picfight.exchbot.lambda.backend.Operation;
import org.picfight.exchbot.lambda.backend.PFCAddress;
import org.picfight.exchbot.lambda.backend.PFCBalance;
import org.picfight.exchbot.lambda.backend.StringAnalysis;
import org.picfight.exchbot.lambda.backend.TransactionBackEnd;
import org.picfight.exchbot.lambda.backend.TransactionBackEndArgs;
import org.picfight.exchbot.lambda.backend.UserSettings;
import org.picfight.exchbot.lambda.backend.WalletBackEnd;
import org.picfight.exchbot.lambda.backend.WalletBackEndArgs;
import org.telegram.telegrambots.meta.bots.AbsSender;

import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.json.Json;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.names.Names;
import com.jfixby.scarabei.api.sys.settings.SystemSettings;

public class TgBotMessageHandler implements Handler {
	public static final String WALLET_CHECK = "/walletcheck";
	public final WalletBackEnd walletBackEnd;
	public final TransactionBackEnd transactionsBackEnd;
	{
		final WalletBackEndArgs walletArgs = new WalletBackEndArgs();
		walletArgs.access_key = SystemSettings.getRequiredStringParameter(Names.newID("ACCESS_KEY"));
		walletArgs.host = SystemSettings.getRequiredStringParameter(Names.newID("BACKEND_HOST"));// "https://exchange.picfight.org";
		walletArgs.port = Integer.parseInt(SystemSettings.getRequiredStringParameter(Names.newID("BACKEND_PORT")));// "https://exchange.picfight.org";
		this.walletBackEnd = new WalletBackEnd(walletArgs);

		final TransactionBackEndArgs transactionArgs = new TransactionBackEndArgs();
		this.transactionsBackEnd = new TransactionBackEnd(transactionArgs);
	}

	@Override
	public boolean handle (final HandleArgs args) throws IOException {

		final boolean flag = Boolean.parseBoolean(SystemSettings.getRequiredStringParameter(Names.newID("disable_bot")));
		final Long chatid = args.update.message.chatID;

		if (flag) {
			Handlers.respond(args.bot, chatid, "Бот отключен.", false);
			return true;
		}

		final String userID = this.userID(chatid);

		final UserSettings settings = this.transactionsBackEnd.getUserSettings(userID, args.filesystem);

// if (args.command.equalsIgnoreCase(OPERATIONS.SET_CHINESE)) {
// settings.setLanguage(UserSettingsLanguage.CH);
// }
		if (args.command.equalsIgnoreCase(OPERATIONS.SET_RUSSIAN) || true) {
			settings.setLanguage(UserSettingsLanguage.RU);
		}
// if (args.command.equalsIgnoreCase(OPERATIONS.SET_ENGLISH)) {
// settings.setLanguage(UserSettingsLanguage.EN);
// }

		if (!settings.languageIsSet()) {
			this.respondSettings(args.bot, chatid);
			return true;
		}

		if (!settings.exchangeAddressIsSet()) {
			settings.setupExchangeAddress(this.walletBackEnd, userID);
// this.respondSettings(args.bot, chatid);
			return true;
		}

		if (false || //
			args.command.equalsIgnoreCase(OPERATIONS.MENU) || //
			args.command.equalsIgnoreCase(OPERATIONS.START) || //
			args.command.equalsIgnoreCase(OPERATIONS.HELP) || //
			false) {
			this.respondMenu(args.bot, settings, chatid);
// this.respondMenuCH(args.bot, settings, chatid);
			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.BUY_PFC)) {
			if (args.arguments.size() != 0) {
				final String text = args.arguments.getElementAt(0);
				final StringAnalysis anal = this.walletBackEnd.analyzeString(text);
				if (anal.PFCAddress != null) {
					this.processBuy(args, settings, anal.PFCAddress);
					return true;
				}
			}

			Handlers.respond(args.bot, chatid, "Buy-command usage:\n" + OPERATIONS.BUY_PFC + " pfc_wallet_address\n\n Example:\n"
				+ OPERATIONS.BUY_PFC + " Ja0bBc1d2e3f4g5h6j7k8l9m0", false);

			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.SELL_PFC)) {
			if (args.arguments.size() != 0) {
				final String text = args.arguments.getElementAt(0);
				final StringAnalysis anal = this.walletBackEnd.analyzeString(text);
				if (anal.BTCAddress != null) {
					this.processSell(args, settings, anal.BTCAddress);
					return true;
				}
			}

			Handlers.respond(args.bot, chatid, "Sell-command usage:\n" + OPERATIONS.SELL_PFC + " btc_wallet_address\n\n Example:\n"
				+ OPERATIONS.SELL_PFC + " 1a0bBc1d2e3f4g5h6j7k8l9m0", false);

			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.STATUS)) {
			if (args.arguments.size() != 0) {
				final String text = args.arguments.getElementAt(0);
				final StringAnalysis anal = this.walletBackEnd.analyzeString(text);
				if (anal.PFCAddress != null) {
					this.processStatus(args, anal.PFCAddress.AddressString);
					return true;
				}
				if (anal.BTCAddress != null) {
					this.processStatus(args, anal.BTCAddress.AddressString);
					return true;
				}
			}

			Handlers.respond(args.bot, chatid, "Order status command usage:\n" + OPERATIONS.STATUS + " wallet_address\n\n Example:\n"
				+ OPERATIONS.STATUS + " za0bBc1d2e3f4g5h6j7k8l9m0", false);

			return true;
		}

// if (args.command.equalsIgnoreCase(OPERATIONS.NEW_BTC_ADDRESS)) {
// final BTCAddress address = this.walletBackEnd.obtainNewBTCAddress();
// Handlers.respond(args.bot, chatid, "Send BTC here:", false);
// Handlers.respond(args.bot, chatid, address.AddressString(), false);
// return true;
// }
// if (args.command.equalsIgnoreCase(OPERATIONS.NEW_PFC_ADDRESS)) {
// final PFCAddress address = this.walletBackEnd.obtainNewPFCAddress();
// Handlers.respond(args.bot, chatid, "Send PFC here:", false);
// Handlers.respond(args.bot, chatid, address.AddressString(), false);
// return true;
// }
		L.e("Command not found", args.command);
		this.respondMenu(args.bot, settings, chatid);
// this.respondMenuCH(args.bot, chatid);
		return true;
	}

	private String userID (final Long chatid) {
		return "tg-" + chatid;
	}

	private void respondSettings (final AbsSender bot, final Long chatid) throws IOException {
		final String N = "\n";
		final StringBuilder b = new StringBuilder();
		b.append("Choose language:");
		b.append(N);
		b.append(OPERATIONS.SET_ENGLISH + " - English 🇬🇧");
		b.append(N);
		b.append(OPERATIONS.SET_RUSSIAN + " - Русский 🇷🇺");
		b.append(N);
		b.append(OPERATIONS.SET_CHINESE + " - 中文 🇨🇳");
		b.append(N);

		Handlers.respond(bot, chatid, b.toString(), false);
	}

	private void processStatus (final HandleArgs args, final String searchterm) throws IOException {
		final Transaction status = this.transactionsBackEnd.findTransaction(searchterm, args.filesystem);

		final Long chatid = args.update.message.chatID;
		if (status == null) {
			Handlers.respond(args.bot, chatid, "Order not found: " + searchterm, false);
			return;
		}

		final StringBuilder b = new StringBuilder();
		b.append("Order:");
		b.append("\n");

		b.append(searchterm);
		b.append("\n");

		b.append("Created: " + new Date(status.operation.timestamp));
		b.append("\n");

		b.append("\n");

		b.append("Status: ");
		final Status state = status.states.get(status.states.size() - 1);
		b.append(state.status);
		b.append("\n");

		if (state.error_message != null) {
			b.append(state.error_message);
			b.append("\n");
		}

// if (state.result != null) {
// b.append(state.result);
// b.append("\n");
// }

		b.append("Type: ");
		b.append(status.operation.type);
		b.append("\n");

		b.append("\n");

		if (status.operation.exchangeBTCWallet != null) {
			b.append("Exchange BTC wallet:");
			b.append("\n");
			b.append(status.operation.exchangeBTCWallet.AddressString);
			b.append("\n");
			b.append("\n");
		}

		if (status.operation.exchangePFCWallet != null) {
			b.append("Exchange PFC wallet:");
			b.append("\n");
			b.append(status.operation.exchangePFCWallet.AddressString);
			b.append("\n");
			b.append("\n");
		}

		if (status.operation.clientBTCWallet != null) {
			b.append("Client BTC wallet:");
			b.append("\n");
			b.append(status.operation.clientBTCWallet.AddressString);
			b.append("\n");
			b.append("\n");
		}

		if (status.operation.clientPFCWallet != null) {
			b.append("Client PFC wallet:");
			b.append("\n");
			b.append(status.operation.clientPFCWallet.AddressString);
			b.append("\n");
			b.append("\n");
		}

		Handlers.respond(args.bot, chatid, b.toString(), false);
		Handlers.respond(args.bot, chatid, Json.serializeToString(status).toString(), false);

	}

	private void processBuy (final HandleArgs args, final UserSettings settings, final PFCAddress pfcAddress) throws IOException {
		final BTCAddress exchangeAddress =
// this.walletBackEnd.obtainNewBTCAddress(settings);
			settings.getExchangeAddressBTC();
		final Long chatid = args.update.message.chatID;

		final BTCBalance balance = this.walletBackEnd.getBTCBallance(exchangeAddress, settings.getAccountName());
		if (balance.AmountBTC.Value > 0) {
			Handlers.respond(args.bot, chatid, "Invalid exchange address: " + exchangeAddress, false);
			return;
		}

		final Operation transact = new Operation();
		transact.chatID = chatid;
		transact.timestamp = System.currentTimeMillis();
		transact.userName = args.update.message.from.userName;
		transact.firstName = args.update.message.from.firstName;
		transact.lastName = args.update.message.from.lastName;

		transact.type = Operation.BUY;
		transact.clientPFCWallet = pfcAddress;
		transact.exchangeBTCWallet = exchangeAddress;

		final Transaction s = new Transaction();
		s.operation = transact;
		{
			final Status oper = new Status();
			s.states.add(oper);
			oper.status = StateStrings.ORDER_REGISTERED;
		}

		final File file = this.transactionsBackEnd.registerTransaction(s, args.filesystem);

		Handlers.respond(args.bot, chatid, "Send BTC to the following address:", false);
		Handlers.respond(args.bot, chatid, exchangeAddress.AddressString, false);
		Handlers.respond(args.bot, chatid, "PFC will be sent to the following address:", false);
		Handlers.respond(args.bot, chatid, "http://explorer.picfight.org/address/" + pfcAddress.AddressString, true);
		Handlers.respond(args.bot, chatid, "Check your PFC address beforehand.", false);
		Handlers.respond(args.bot, chatid, "Processing time can be up to 24H.", false);

		Handlers.respond(args.bot, chatid, "" + Json.serializeToString(transact), false);
		Handlers.respond(args.bot, chatid, "" + file.toString(), false);
	}

	private void processSell (final HandleArgs args, final UserSettings settings, final BTCAddress btcAddress) throws IOException {
		final PFCAddress pfcAddress =
			// this.walletBackEnd.obtainNewPFCAddress(settings);
			settings.getExchangeAddressPFC();
		final Long chatid = args.update.message.chatID;

		final PFCBalance balance = this.walletBackEnd.getPFCBallance(pfcAddress, settings.getAccountName());
		if (balance.AmountPFC.Value > 0) {
			Handlers.respond(args.bot, chatid, "Invalid exchange address: " + pfcAddress, false);
			return;
		}

		final Operation transact = new Operation();
		transact.chatID = chatid;
		transact.timestamp = System.currentTimeMillis();
		transact.userName = args.update.message.from.userName;
		transact.firstName = args.update.message.from.firstName;
		transact.lastName = args.update.message.from.lastName;

		transact.type = Operation.SELL;
		transact.exchangePFCWallet = pfcAddress;
		transact.clientBTCWallet = btcAddress;

		final Transaction s = new Transaction();
		s.operation = transact;
		{
			final Status oper = new Status();
			s.states.add(oper);
			oper.status = StateStrings.ORDER_REGISTERED;
		}

		final File file = this.transactionsBackEnd.registerTransaction(s, args.filesystem);

		Handlers.respond(args.bot, chatid, "Send PFC to the following address:", false);
		Handlers.respond(args.bot, chatid, pfcAddress.AddressString, false);
		Handlers.respond(args.bot, chatid, "BTC will be sent to the following address:", false);
		Handlers.respond(args.bot, chatid, "https://www.blockchain.com/btc/address/" + btcAddress.AddressString, true);
		Handlers.respond(args.bot, chatid, "Check your BTC address beforehand.", false);
		Handlers.respond(args.bot, chatid, "Processing time can be up to 24H.", false);

		Handlers.respond(args.bot, chatid, "" + Json.serializeToString(transact), false);
		Handlers.respond(args.bot, chatid, "" + file.toString(), false);
	}

	private void respondMenu (final AbsSender bot, final UserSettings settings, final Long chatid) throws IOException {
		try {
			final AvailableFunds rate = this.walletBackEnd.getFunds();

			final String N = "\n";
			final StringBuilder b = new StringBuilder();
			b.append("This bot sells and buys PicFight coins (PFC) for Bitcoins (BTC)");
			b.append(N);
			b.append("PFC available for sale: " + rate.AvailablePFC.Value + " PFC");
			b.append(N);
			b.append(N);
			b.append("Exchange rate:");
			b.append(N);
			b.append("Buy 100 PFC for " + this.formatFloat(Exchange.buyPriceBTC(rate) * 100, UP) + " BTC");
			b.append(N);
			b.append("Sell 100 PFC for " + this.formatFloat(Exchange.sellPriceBTC(rate) * 100, DOWN) + " BTC");
			b.append(N);
			b.append(N);
			b.append(OPERATIONS.BUY_PFC + " to buy PFC");
			b.append(N);
			b.append(OPERATIONS.SELL_PFC + " to sell PFC");
			b.append(N);
			b.append(OPERATIONS.STATUS + " to check your order status");
			b.append(N);
			b.append("Your exchange BTC address is: " + settings.getExchangeAddressBTC().toString());
			b.append(N);
			b.append("Your exchange PFC address is: " + settings.getExchangeAddressPFC().toString());
			b.append(N);
			b.append(OPERATIONS.STATUS + " to check your order status");
			b.append(N);
			b.append(N);
			b.append("You can download PFC wallet here: https://github.com/picfight/pfcredit");

			Handlers.respond(bot, chatid, b.toString(), false);

		} catch (final Throwable e) {
			final String N = "\n";
			final StringBuilder b = new StringBuilder();
			b.append("Backend is not responding: " + e.toString());
			b.append(N);

			Handlers.respond(bot, chatid, b.toString(), false);
		}

	}

	private String formatFloat (final double value, final boolean roundUP) {
		final DecimalFormat formatter = new DecimalFormat("#.######", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		if (roundUP == UP) {
			formatter.setRoundingMode(RoundingMode.UP);
		}
		if (roundUP == DOWN) {
			formatter.setRoundingMode(RoundingMode.DOWN);
		}
		final String s = formatter.format(value);
		return s;
	}

	static boolean UP = true;
	static boolean DOWN = !UP;

	private void respondMenuCH (final AbsSender bot, final Long chatid) throws IOException {
		final AvailableFunds rate = this.walletBackEnd.getFunds();

		final String N = "\n";
		final StringBuilder b = new StringBuilder();
		b.append("该机器人买卖比特币（BTC）的PicFight硬币（PFC）");
		b.append(N);
		b.append("PFC可供出售 " + rate.AvailablePFC.Value + "");
		b.append(N);
		b.append(N);
		b.append("汇率:");
		b.append(N);
		b.append("为BTC购买100个PFC " + this.formatFloat(Exchange.buyPriceBTC(rate) * 100, UP) + "");
		b.append(N);
		b.append("为BTC出售100个PFC " + this.formatFloat(Exchange.sellPriceBTC(rate) * 100, DOWN) + "");
		b.append(N);
		b.append(N);
		b.append(OPERATIONS.BUY_PFC + " 购买 PFC");
		b.append(N);
		b.append(OPERATIONS.SELL_PFC + " 出售 PFC");
		b.append(N);
		b.append(OPERATIONS.STATUS + " 查看订单状态");
		b.append(N);
		b.append(N);
		b.append("您可以在这里下载PFC钱包: https://github.com/picfight/pfcredit");

		Handlers.respond(bot, chatid, b.toString(), false);

	}

}
