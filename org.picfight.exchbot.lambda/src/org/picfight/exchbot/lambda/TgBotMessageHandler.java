
package org.picfight.exchbot.lambda;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.picfight.exchbot.lambda.backend.BTCAddress;
import org.picfight.exchbot.lambda.backend.PFCAddress;
import org.picfight.exchbot.lambda.backend.Rate;
import org.picfight.exchbot.lambda.backend.StringAnalysis;
import org.picfight.exchbot.lambda.backend.Transaction;
import org.picfight.exchbot.lambda.backend.TransactionBackEnd;
import org.picfight.exchbot.lambda.backend.TransactionBackEndArgs;
import org.picfight.exchbot.lambda.backend.WalletBackEnd;
import org.picfight.exchbot.lambda.backend.WalletBackEndArgs;
import org.telegram.telegrambots.meta.bots.AbsSender;

import com.jfixby.scarabei.api.collections.List;
import com.jfixby.scarabei.api.json.Json;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.names.Names;
import com.jfixby.scarabei.api.strings.Strings;
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

		if (false || //
			args.command.equalsIgnoreCase(OPERATIONS.MENU) || //
			args.command.equalsIgnoreCase(OPERATIONS.START) || //
			args.command.equalsIgnoreCase(OPERATIONS.HELP) || //
			false) {
			this.respondMenu(args.bot, chatid);
			this.respondMenuCH(args.bot, chatid);
			return true;
		}

		if (args.inputRaw != null) {
			final List<String> list = Strings.split(args.inputRaw, " ");
			if (list.size() > 0) {
				final String text = list.getElementAt(0);
				final StringAnalysis anal = this.walletBackEnd.analyzeString(text);
				if (anal.BTCAddress != null) {
					this.processSell(args, anal.BTCAddress);
					return true;
				}
				if (anal.PFCAddress != null) {
					this.processBuy(args, anal.PFCAddress);
					return true;
				}

			}
		}

// if (args.command.equalsIgnoreCase(OPERATIONS.RATE)) {
// final Rate rate = backEnd.getRate();
// Handlers.respond(args.bot, chatid, "Circulating supply: " + (rate.getCirculatingSupply()).longValue() + " PFC", false);
// Handlers.respond(args.bot, chatid, "PicfightCoin price: " + rate.BTCperPFC() + " BTC per coin", false);
// Handlers.respond(args.bot, chatid, "Available coins: " + rate.availablePFC() + " PFC", false);
// return true;
// }
//
// if (args.command.equalsIgnoreCase(OPERATIONS.BUY_PFC)) {
// if (args.arguments.size() == 0) {
// Handlers.respond(args.bot, chatid,
// "To buy PicFight coins, send the following command: " + OPERATIONS.BUY_PFC + " " + address.AddressString(), false);
// }
//
// final BTCAddressArgs a = new BTCAddressArgs();
// final BTCAddress address = backEnd.obtainNewBTCAddress(a);
// Handlers.respond(args.bot, chatid, "Send BTC here: " + address.AddressString(), false);
// return true;
// }
//
// if (args.command.equalsIgnoreCase(OPERATIONS.SELL_PFC)) {
// final PFCAddressArgs a = new PFCAddressArgs();
// final PFCAddress address = backEnd.obtainNewPFCAddress(a);
// Handlers.respond(args.bot, chatid, "Send PFC here: " + address.AddressString(), false);
// return true;
// }
//
// if (args.command.equalsIgnoreCase(OPERATIONS.BUY_PFC_CH)) {
// final BTCAddressArgs a = new BTCAddressArgs();
// final BTCAddress address = backEnd.obtainNewBTCAddress(a);
// Handlers.respond(args.bot, chatid, "Send BTC here: " + address.AddressString(), false);
// return true;
// }
//
// if (args.command.equalsIgnoreCase(OPERATIONS.SELL_PFC_CH)) {
// final PFCAddressArgs a = new PFCAddressArgs();
// final PFCAddress address = backEnd.obtainNewPFCAddress(a);
// Handlers.respond(args.bot, chatid, "Send PFC here: " + address.AddressString(), false);
// return true;
// }

		L.e("Command not found", args.command);
		this.respondMenu(args.bot, chatid);
		this.respondMenuCH(args.bot, chatid);
		return true;
	}

	private void processBuy (final HandleArgs args, final PFCAddress pfcAddress) throws IOException {
		final BTCAddress btcAddress = this.walletBackEnd.obtainNewBTCAddress();
		final Long chatid = args.update.message.chatID;

		final Transaction transact = new Transaction();
		transact.chatID = chatid;
		transact.timestamp = System.currentTimeMillis();
		transact.userName = args.update.message.from.userName;
		transact.firstName = args.update.message.from.firstName;
		transact.lastName = args.update.message.from.lastName;

		transact.type = Transaction.BUY;
		transact.clientPFCWallet = pfcAddress;
		transact.exchangeBTCWallet = btcAddress;
		this.transactionsBackEnd.registerTransaction(transact);

		Handlers.respond(args.bot, chatid, "Send BTC to the following address:", false);
		Handlers.respond(args.bot, chatid, btcAddress.AddressString, false);
		Handlers.respond(args.bot, chatid, "PFC will be sent to the following address:", false);
		Handlers.respond(args.bot, chatid, "http://explorer.picfight.org/address/" + pfcAddress.AddressString, true);
		Handlers.respond(args.bot, chatid, "Check your PFC address beforehand.", false);
		Handlers.respond(args.bot, chatid, "Processing time can be up to 24H.", false);

		Handlers.respond(args.bot, chatid, "" + Json.serializeToString(transact), false);
	}

	private void processSell (final HandleArgs args, final BTCAddress btcAddress) throws IOException {
		final PFCAddress pfcAddress = this.walletBackEnd.obtainNewPFCAddress();
		final Long chatid = args.update.message.chatID;

		final Transaction transact = new Transaction();
		transact.chatID = chatid;
		transact.timestamp = System.currentTimeMillis();
		transact.userName = args.update.message.from.userName;
		transact.firstName = args.update.message.from.firstName;
		transact.lastName = args.update.message.from.lastName;

		transact.type = Transaction.SELL;
		transact.exchangePFCWallet = pfcAddress;
		transact.clientBTCWallet = btcAddress;

		this.transactionsBackEnd.registerTransaction(transact);

		Handlers.respond(args.bot, chatid, "Send PFC to the following address:", false);
		Handlers.respond(args.bot, chatid, pfcAddress.AddressString, false);
		Handlers.respond(args.bot, chatid, "BTC will be sent to the following address:", false);
		Handlers.respond(args.bot, chatid, "https://www.blockchain.com/btc/address/" + btcAddress.AddressString, true);
		Handlers.respond(args.bot, chatid, "Check your BTC address beforehand.", false);
		Handlers.respond(args.bot, chatid, "Processing time can be up to 24H.", false);

		Handlers.respond(args.bot, chatid, "" + Json.serializeToString(transact), false);
	}

	private void respondMenu (final AbsSender bot, final Long chatid) throws IOException {
		final Rate rate = this.walletBackEnd.getRate();

		final String N = "\n";
		final StringBuilder b = new StringBuilder();
		b.append("This bot sells and buys PicFight coins (PFC) for Bitcoins (BTC)");
		b.append(N);
		b.append("PFC available for sale: " + rate.availablePFC() + " PFC");
		b.append(N);
		b.append(N);
		b.append("Exchange rate:");
		b.append(N);
		b.append("Buy 100 PFC for " + this.formatFloat(this.buyPrice(rate) * 100, UP) + " BTC");
		b.append(N);
		b.append("Sell 100 PFC for " + this.formatFloat(this.sellPrice(rate) * 100, DOWN) + " BTC");
		b.append(N);
		b.append(N);
		b.append("To  buy PFC submit your PFC-wallet address");
		b.append(N);
		b.append("To sell PFC submit your BTC-wallet address");
		b.append(N);
		b.append(N);
		b.append("You can download PFC wallet here: https://github.com/picfight/pfcredit");

		Handlers.respond(bot, chatid, b.toString(), false);

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
		final Rate rate = this.walletBackEnd.getRate();

		final String N = "\n";
		final StringBuilder b = new StringBuilder();
		b.append("该机器人买卖比特币（BTC）的PicFight硬币（PFC）");
		b.append(N);
		b.append("PFC可供出售 " + rate.availablePFC() + "");
		b.append(N);
		b.append(N);
		b.append("汇率:");
		b.append(N);
		b.append("为BTC购买100个PFC " + this.formatFloat(this.buyPrice(rate) * 100, UP) + "");
		b.append(N);
		b.append("为BTC出售100个PFC " + this.formatFloat(this.sellPrice(rate) * 100, DOWN) + "");
		b.append(N);
		b.append(N);
		b.append("要购买PFC，请提交您的PFC钱包地址");
		b.append(N);
		b.append("要出售PFC，请提交您的BTC钱包地址");
		b.append(N);
		b.append(N);
		b.append("您可以在这里下载PFC钱包: https://github.com/picfight/pfcredit");

		Handlers.respond(bot, chatid, b.toString(), false);

	}

	private double buyPrice (final Rate rate) {
		final double exchange_rate = Double.parseDouble(SystemSettings.getRequiredStringParameter(Names.newID("EXCHANGE_RATE")));
		final double margin = Double.parseDouble(SystemSettings.getRequiredStringParameter(Names.newID("EXCHANGE_MARGIN")));

		return exchange_rate * (1.0 + margin / 2.0);
	}

	private double sellPrice (final Rate rate) {
		final double exchange_rate = Double.parseDouble(SystemSettings.getRequiredStringParameter(Names.newID("EXCHANGE_RATE")));
		final double margin = Double.parseDouble(SystemSettings.getRequiredStringParameter(Names.newID("EXCHANGE_MARGIN")));
		return exchange_rate * (1.0 - margin / 2.0);
	}

}
