
package org.picfight.exchbot.lambda;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.picfight.exchbot.lambda.backend.AmountDCR;
import org.picfight.exchbot.lambda.backend.AmountPFC;
import org.picfight.exchbot.lambda.backend.BackendException;
import org.picfight.exchbot.lambda.backend.DCRAddress;
import org.picfight.exchbot.lambda.backend.DCRBalance;
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

		final String accountName = this.userID(chatid);

		args.accountName = accountName;

		final UserSettings settings = this.transactionsBackEnd.getUserSettings(accountName, args.filesystem);

		settings.setLanguage(UserSettingsLanguage.RU);
		args.settings = settings;
		L.d("L1");

		if (false || //
			args.command.equalsIgnoreCase(OPERATIONS.MENU) || //
			args.command.equalsIgnoreCase(OPERATIONS.START) || //
			args.command.equalsIgnoreCase(OPERATIONS.HELP) || //
			false) {
			this.respondMenu(args.bot, settings, chatid);
			return true;
		}
		L.d("L2");
		try {
			final boolean result = this.processWithBackend(args);
			if (result) {
				return result;
			}
		} catch (final BackendException e) {
			L.e(e);
			final StringBuilder b = new StringBuilder();
			b.append("Backend is not responding: " + e.toString());
			b.append(N);

			Handlers.respond(args.bot, chatid, b.toString(), false);
		}
// if (args.command.equalsIgnoreCase(OPERATIONS.BUY_PFC)) {
// if (args.arguments.size() != 0) {
// final String text = args.arguments.getElementAt(0);
// final StringAnalysis anal = this.walletBackEnd.analyzeString(text);
// if (anal.PFCAddress != null) {
// this.processBuy(args, settings, anal.PFCAddress);
// return true;
// }
// }
//
// Handlers.respond(args.bot, chatid, "Buy-command usage:\n" + OPERATIONS.BUY_PFC + " pfc_wallet_address\n\n Example:\n"
// + OPERATIONS.BUY_PFC + " Ja0bBc1d2e3f4g5h6j7k8l9m0", false);
//
// return true;
// }
//
// if (args.command.equalsIgnoreCase(OPERATIONS.SELL_PFC)) {
// if (args.arguments.size() != 0) {
// final String text = args.arguments.getElementAt(0);
// final StringAnalysis anal = this.walletBackEnd.analyzeString(text);
// if (anal.BTCAddress != null) {
// this.processSell(args, settings, anal.BTCAddress);
// return true;
// }
// }
//
// Handlers.respond(args.bot, chatid, "Sell-command usage:\n" + OPERATIONS.SELL_PFC + " btc_wallet_address\n\n Example:\n"
// + OPERATIONS.SELL_PFC + " 1a0bBc1d2e3f4g5h6j7k8l9m0", false);
//
// return true;
// }
//
// if (args.command.equalsIgnoreCase(OPERATIONS.STATUS)) {
// final List<Transaction> transactions = this.checkStatus(args, settings);
// if (transactions.size() == 0) {
// Handlers.respond(args.bot, chatid, "No processing orders.", false);
// } else {
// for (final Transaction t : transactions) {
// this.printTransaction(args, t);
// }
// }
// return true;
// }

		L.e("Command not found", args.command);
		this.respondMenu(args.bot, settings, chatid);
		return true;
	}

	private boolean processWithBackend (final HandleArgs args) throws BackendException, IOException {
		final UserSettings settings = args.settings;
		final String userID = args.accountName;
		final AbsSender bot = args.bot;
		final String accountName = args.accountName;

		if (!settings.exchangeAddressIsSet()) {
			settings.setupExchangeAddress(this.walletBackEnd, userID);
// return true;
		}
		final Long chatid = args.update.message.chatID;
		if (args.command.equalsIgnoreCase(OPERATIONS.BALANCE)) {
			L.d("L3");

			final DCRAddress dcr_address = settings.getExchangeAddressDCR();
			final DCRBalance dcr = this.walletBackEnd.getDCRBallance(dcr_address, accountName, 3);

			final PFCAddress pfc_address = settings.getExchangeAddressPFC();
			final PFCBalance pfc = this.walletBackEnd.getPFCBallance(pfc_address, accountName, 3);

			final StringBuilder b = new StringBuilder();
			b.append("Твои балансы:").append(N);
			b.append(N);
			b.append(dcr.AmountDCR.toString()).append(N);
			b.append(pfc.AmountPFC.toString()).append(N);
			b.append(N);
			b.append("Пополнить балансы - " + OPERATIONS.DEPOSIT).append(N);
			b.append("Вывести монеты с биржи - " + OPERATIONS.WITHDRAW).append(N);
			L.d("L4");
			Handlers.respond(bot, chatid, b.toString(), false);
			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.DEPOSIT)) {
			if (args.arguments.size() != 0) {
				final String text = args.arguments.getElementAt(0).toLowerCase();

				final DCRAddress dcr_address = settings.getExchangeAddressDCR();

				final PFCAddress pfc_address = settings.getExchangeAddressPFC();
				if (text.equals("dcr")) {
					Handlers.respond(bot, chatid, "Засылай DCR на следующий адрес:", false);
					Handlers.respond(bot, chatid, dcr_address.toString(), false);
					return true;
				}
				if (text.equals("pfc")) {
					Handlers.respond(bot, chatid, "Засылай PFC на следующий адрес:", false);
					Handlers.respond(bot, chatid, pfc_address.toString(), false);
					return true;
				}
			}
			{
				final StringBuilder b = new StringBuilder();
				b.append("Команды для зачисления средств на биржу:").append(N);
				b.append("пополнить DCR: " + OPERATIONS.DEPOSIT + " dcr").append(N);
				b.append("пополнить DCR: " + OPERATIONS.DEPOSIT + " dcr").append(N);
				Handlers.respond(bot, chatid, b.toString(), false);
				return true;
			}
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.WITHDRAW)) {
			if (args.arguments.size() != 0) {
				if (args.arguments.size() != 3) {
					this.withdrawHelp(bot, chatid);
					return true;
				}
				final String cointext = args.arguments.getElementAt(0).toLowerCase();
				String coin = null;
				if (cointext.equals("dcr")) {
					coin = "dcr";
				}
				if (cointext.equals("pfc")) {
					coin = "pfc";
				}
				if (coin == null) {
					Handlers.respond(bot, chatid, "Монета указана неправильно: " + cointext, false);
					this.withdrawHelp(bot, chatid);
					return true;
				}
				Double amount = null;
				final String amount_text = args.arguments.getElementAt(1).toLowerCase();
				if (amount_text.equals("all")) {
					amount = null;
				} else {
					try {
						amount = Double.parseDouble(amount_text);
					} catch (final Throwable e) {
						e.printStackTrace();

						Handlers.respond(bot, chatid, "Количество монет не распознано: " + amount_text, false);
						this.withdrawHelp(bot, chatid);
						return true;

					}
				}

				final String address_text = args.arguments.getElementAt(2);
				final StringAnalysis anal = this.walletBackEnd.analyzeString(address_text);
				if (anal.DCRAddress == null && anal.PFCAddress == null) {
					Handlers.respond(bot, chatid, "Не удалось распознать адрес для вывода: " + address_text, false);
					Handlers.respond(bot, chatid, anal.Error, false);
					this.withdrawHelp(bot, chatid);
					return true;
				}

				if (coin.equals("pfc")) {
					final Operation t = new Operation();
					if (amount == null) {
						t.allFunds = true;
					} else {
						t.pfcAmount = new AmountPFC(amount);
					}

					t.pfcAddress = anal.PFCAddress;
					if (t.pfcAddress == null) {
						Handlers.respond(bot, chatid, "Не удалось распознать адрес для вывода: " + address_text, false);
						Handlers.respond(bot, chatid, anal.Error, false);
						this.withdrawHelp(bot, chatid);
						return true;
					}

					final Result result = this.walletBackEnd.transferPFC(t);
					Handlers.respond(bot, chatid, result.toString(), false);
					return true;
				}

				if (coin.equals("dcr")) {
					final Operation t = new Operation();
					if (amount == null) {
						t.allFunds = true;
					} else {
						t.dcrAmount = new AmountDCR(amount);
					}

					t.dcrAddress = anal.DCRAddress;
					if (t.dcrAddress == null) {
						Handlers.respond(bot, chatid, "Не удалось распознать адрес для вывода: " + address_text, false);
						Handlers.respond(bot, chatid, anal.Error, false);
						this.withdrawHelp(bot, chatid);
						return true;
					}

					final Result result = this.walletBackEnd.transferDCR(t);
					Handlers.respond(bot, chatid, result.toString(), false);
					return true;
				}

			}
			{
				this.withdrawHelp(bot, chatid);
				return true;
			}
		}

		return false;
	}

	private void withdrawHelp (final AbsSender bot, final Long chatid) throws IOException {
		final StringBuilder b = new StringBuilder();
		b.append("Команды для вывода монет с биржи:").append(N);
		b.append("Вывести DCR: " + OPERATIONS.WITHDRAW + " dcr %количество% %адрес%").append(N);
		b.append("Вывести PFC: " + OPERATIONS.WITHDRAW + " pfc %количество% %адрес%").append(N);
		b.append(N);
		b.append("Примеры:").append(N);
		b.append(OPERATIONS.WITHDRAW + " dcr 0.02 D1aBcDeFg123456789H").append(N);
		b.append(OPERATIONS.WITHDRAW + " pfc 120 JabcgeFg123456789H").append(N);
		b.append(OPERATIONS.WITHDRAW + " dcr all DaBcDeFg123456789H").append(N);
		b.append(OPERATIONS.WITHDRAW + " pfc all J431aBcDeFg123456789H").append(N);
		b.append(N);
		Handlers.respond(bot, chatid, b.toString(), false);
	}

// private void printTransaction (final HandleArgs args, final Transaction status) throws IOException {
// final Long chatid = args.update.message.chatID;
//
// final StringBuilder b = new StringBuilder();
// b.append("Order:");
// b.append(N);
// b.append(N);
//
// if (status.states.size() > 0) {
// b.append("Status: ");
// final Status state = status.states.get(status.states.size() - 1);
// b.append(state.status);
// b.append(N);
//
// if (state.error_message != null) {
// b.append(state.error_message);
// b.append(N);
// }
// }
//
// b.append("Type: ");
// b.append(status.operation.type);
// b.append(N);
//
// b.append(N);
//
// if (status.operation.exchangeBTCWallet != null) {
// b.append("Exchange BTC wallet:");
// b.append(N);
// b.append(status.operation.exchangeBTCWallet.AddressString);
// b.append(N);
// b.append(N);
// }
//
// if (status.operation.exchangePFCWallet != null) {
// b.append("Exchange PFC wallet:");
// b.append(N);
// b.append(status.operation.exchangePFCWallet.AddressString);
// b.append(N);
// b.append(N);
// }
//
// if (status.operation.clientBTCWallet != null) {
// b.append("Client BTC wallet:");
// b.append(N);
// b.append(status.operation.clientBTCWallet.AddressString);
// b.append(N);
// b.append(N);
// }
//
// if (status.operation.clientPFCWallet != null) {
// b.append("Client PFC wallet:");
// b.append(N);
// b.append(status.operation.clientPFCWallet.AddressString);
// b.append(N);
// b.append(N);
// }
//
// Handlers.respond(args.bot, chatid, b.toString(), false);
// Handlers.respond(args.bot, chatid, Json.serializeToString(status).toString(), false);
//
// }

	private String userID (final Long chatid) {
		return "tg-" + chatid;
	}

// private void processBuy (final HandleArgs args, final UserSettings settings, final PFCAddress pfcAddress) throws IOException {
// final Long chatid = args.update.message.chatID;
//
// final BTCAddress exchangeAddress = settings.getExchangeAddressBTC();
//
//// settings.setupPrivateAddressPFC(pfcAddress);
//
// Handlers.respond(args.bot, chatid, "Send BTC to the following address:", false);
// Handlers.respond(args.bot, chatid, exchangeAddress.AddressString, false);
// Handlers.respond(args.bot, chatid, "Your PFC will be sent to the following address:", false);
// Handlers.respond(args.bot, chatid, "http://explorer.picfight.org/address/" + settings.getPrivateAddressPFC(), true);
// Handlers.respond(args.bot, chatid, "Check your PFC address beforehand.", false);
//// Handlers.respond(args.bot, chatid, "Processing time can be up to 24H.", false);
//
// }
//
// private void processSell (final HandleArgs args, final UserSettings settings, final BTCAddress btcAddress) throws IOException {
// final Long chatid = args.update.message.chatID;
//
// final PFCAddress exchangeAddress = settings.getExchangeAddressPFC();
//
//// settings.setupPrivateAddressBTC(btcAddress);
//
// Handlers.respond(args.bot, chatid, "Send PFC to the following address:", false);
// Handlers.respond(args.bot, chatid, exchangeAddress.AddressString, false);
// Handlers.respond(args.bot, chatid, "Your BTC will be sent to the following address:", false);
// Handlers.respond(args.bot, chatid, "https://www.blockchain.com/btc/address/" + settings.getPrivateAddressBTC(), true);
// Handlers.respond(args.bot, chatid, "Check your BTC address beforehand.", false);
//// Handlers.respond(args.bot, chatid, "Processing time can be up to 24H.", false);
//
// }

// private List<Transaction> checkStatus (final HandleArgs args, final UserSettings settings) throws IOException {
// final List<Transaction> transactions = Collections.newList();
// this.checkBuyStatus(transactions, args, settings);
// this.checkSellStatus(transactions, args, settings);
// return transactions;
// }

// private void checkSellStatus (final List<Transaction> transactions, final HandleArgs args, final UserSettings settings)
// throws IOException {
// final String accountName = args.userID;
// final Long chatid = args.update.message.chatID;
//
// final PFCAddress pfc_address = settings.getExchangeAddressPFC();
// final PFCBalance pfc = this.walletBackEnd.getPFCBallance(pfc_address, accountName);
// if (pfc.AmountPFC.Value > 0) {
// final Transaction t = new Transaction();
// t.operation = new Operation();
// {
// final Operation op = t.operation;
// op.chatID = chatid;
// op.type = Operation.SELL;
// op.firstName = args.update.message.from.firstName;
// op.lastName = args.update.message.from.lastName;
// op.clientBTCWallet = settings.getPrivateAddressBTC();
// op.exchangePFCWallet = pfc_address;
// op.timestamp = System.currentTimeMillis();
// op.userName = args.update.message.from.userName;
// op.pendingPFC = pfc.AmountPFC.Value;
// }
// transactions.add(t);
// }
// }

// private void checkBuyStatus (final List<Transaction> transactions, final HandleArgs args, final UserSettings settings)
// throws IOException {
// final String accountName = args.userID;
// final Long chatid = args.update.message.chatID;
//
// final BTCAddress btc_address = settings.getExchangeAddressBTC();
// final BTCBalance btc = this.walletBackEnd.getBTCBallance(btc_address, accountName);
// if (btc.AmountBTC.Value > 0) {
// final Transaction t = new Transaction();
// t.operation = new Operation();
// {
// final Operation op = t.operation;
// op.type = Operation.BUY;
// op.chatID = chatid;
// op.firstName = args.update.message.from.firstName;
// op.lastName = args.update.message.from.lastName;
// op.clientPFCWallet = settings.getPrivateAddressPFC();
// op.exchangeBTCWallet = btc_address;
// op.timestamp = System.currentTimeMillis();
// op.userName = args.update.message.from.userName;
// op.pendingBTC = btc.AmountBTC.Value;
// }
// transactions.add(t);
// }
// }

	public static final String N = "\n";

	private void respondMenu (final AbsSender bot, final UserSettings settings, final Long chatid) throws IOException {
		try {
// final AvailableFunds rate = this.walletBackEnd.getFunds();

// final MarketPair pair = MarketPair.newMarketPair(CoinSign.TETHER, CoinSign.BITCOIN);
// final Ticker ticker = GetTicker.get(pair);
// final double usd_per_btc = ticker.result.Last;
// final double btc_per_pfc = Exchange.sellPriceBTC(rate);
// final double usd_per_pfc = usd_per_btc * btc_per_pfc;

			final StringBuilder b = new StringBuilder();
			b.append("Этот бот-биржа продаёт и покупает пикфайт-коины (PFC) за декреды (DCR)").append(N);
			b.append(N);

			b.append("Команды для бота:").append(N);
			b.append(OPERATIONS.BALANCE + " - посмотреть свои текущие балансы на бирже").append(N);
			b.append(OPERATIONS.DEPOSIT + " - пополнить балансы").append(N);
			b.append(OPERATIONS.WITHDRAW + " - вывести монеты с биржи").append(N);
			b.append(N);
			b.append(OPERATIONS.MARKET + " - информация о торгах").append(N);
			b.append(OPERATIONS.BUY_PFC + " - купить").append(N);
			b.append(OPERATIONS.SELL_PFC + " - продать").append(N);
			b.append(N);
// b.append("Доступно к торговле: " + rate.AvailablePFC.Value + " PFC");
// b.append(N);
// b.append(N);
// b.append("Курс обмена:");
// b.append(N);
// b.append("1 PFC = $" + this.formatFloat(usd_per_pfc, UP) + "");
// b.append(N);
// b.append("100 PFC можно купить за " + this.formatFloat(Exchange.buyPriceBTC(rate) * 100, UP) + " BTC");
// b.append(N);
// b.append("и продать за " + this.formatFloat(Exchange.sellPriceBTC(rate) * 100, DOWN) + " BTC");
// b.append(N);
// b.append(N);
// b.append(OPERATIONS.BUY_PFC + " to buy PFC");
// b.append(N);
// b.append(OPERATIONS.SELL_PFC + " to sell PFC");
// b.append(N);
// b.append(OPERATIONS.STATUS + " to check your order status");
// b.append(N);
// b.append(N);
// b.append("Твой адрес для депозитов BTC:");
// b.append(N);
// b.append("" + settings.getExchangeAddressBTC().toString());
// b.append(N);
// b.append(N);
// b.append("для депозитов PFC:");
// b.append(N);
// b.append("" + settings.getExchangeAddressPFC().toString());
// b.append(N);
// b.append(N);
// b.append("Твой адрес для вывода BTC:");
// b.append(N);
// if (settings.privateBTCAddressIsSet()) {
// b.append("" + settings.getPrivateAddressBTC().toString());
// } else {
// b.append("-не установлен-");
// }
// b.append(N);
// b.append(N);
// b.append("для вывода PFC:");
// b.append(N);
// if (settings.privatePFCAddressIsSet()) {
// b.append("" + settings.getPrivateAddressPFC().toString());
// } else {
// b.append("-не установлен-");
// }
// b.append(N);
// b.append(N);
// b.append(OPERATIONS.STATUS + " to check your order status");
// b.append(N);
// b.append(N);
			b.append("PFC-кошелёк можно скачать тут: https://github.com/picfight/pfcredit");
			b.append("DCR можно купить и продать на Бинансе: https://www.binance.com/en/trade/DCR_BTC");

			Handlers.respond(bot, chatid, b.toString(), false);

		} catch (final Throwable e) {
			L.e(e);

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

}
