
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
import org.picfight.exchbot.lambda.backend.PFCAddress;
import org.picfight.exchbot.lambda.backend.PFCBalance;
import org.picfight.exchbot.lambda.backend.StringAnalysis;
import org.picfight.exchbot.lambda.backend.TradeResult;
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

		L.e("Command not found", args.command);
		this.respondMenu(args.bot, settings, chatid);
		return true;
	}

	private boolean processWithBackend (final HandleArgs args) throws BackendException, IOException {
		final UserSettings settings = args.settings;
		final AbsSender bot = args.bot;
		final Long chatid = args.update.message.chatID;

		if (!settings.exchangeAddressIsSet()) {
			settings.setupExchangeAddress(this.walletBackEnd);
// return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.BALANCE)) {
			this.showBalances(args);

			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.MARKET)) {
			this.showMarketState(args);

			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.DEPOSIT)) {
			final StringBuilder b = new StringBuilder();
			b.append("Команды для зачисления средств на биржу").append(N);
			b.append(N);
			b.append("пополнить DCR: " + OPERATIONS.DEPOSIT_DCR).append(N);
			b.append("пополнить PFC: " + OPERATIONS.DEPOSIT_PFC).append(N);
			Handlers.respond(bot, chatid, b.toString(), false);
			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.DEPOSIT_DCR)) {
			final DCRAddress dcr_address = settings.getExchangeAddressDCR();
			Handlers.respond(bot, chatid, "Засылай DCR на следующий адрес:", false);
			Handlers.respond(bot, chatid, dcr_address.toString(), false);
			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.DEPOSIT_PFC)) {
			final PFCAddress pfc_address = settings.getExchangeAddressPFC();
			Handlers.respond(bot, chatid, "Засылай PFC на следующий адрес:", false);
			Handlers.respond(bot, chatid, pfc_address.toString(), false);
			return true;
		}
		if (args.command.equalsIgnoreCase(OPERATIONS.WITHDRAW)) {
			this.withdrawHelp(bot, chatid);
			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.WITHDRAW_PFC)) {
			if (args.arguments.size() != 2) {
				this.withdrawHelp(bot, chatid);
				return true;
			}

			Double amountFloat = null;
			AmountPFC amount = null;
			final String amount_text = args.arguments.getElementAt(0).toLowerCase();
			try {
				amountFloat = Double.parseDouble(amount_text);
				amount = new AmountPFC(amountFloat);
			} catch (final Throwable e) {
				e.printStackTrace();

				Handlers.respond(bot, chatid, "Количество монет не распознано: " + amount_text, false);
				this.withdrawHelp(bot, chatid);
				return true;

			}

			final String address_text = args.arguments.getElementAt(1);
			final StringAnalysis anal = this.walletBackEnd.analyzeString(address_text);
			if (anal.PFCAddress == null) {
				Handlers.respond(bot, chatid, "Не удалось распознать адрес для вывода: " + address_text, false);
				Handlers.respond(bot, chatid, anal.Error, false);
				this.withdrawHelp(bot, chatid);
				return true;
			}

			{
				final PFCAddress exch_address = settings.getExchangeAddressPFC();
				final TransactionResult result = this.walletBackEnd.transferPFC(exch_address, anal.PFCAddress, amount);

				if (result.Success) {
					Handlers.respond(bot, chatid, "Монеты высланы на адрес " + result.PFC_ToAddress + "", false);
					Handlers.respond(bot, chatid, "Чек операции:", false);
					Handlers.respond(args.bot, chatid, "http://explorer.picfight.org/tx/" + result.PFC_TransactionReceipt, true);
				} else {
					Handlers.respond(bot, chatid, "Не удалось записать транзакцию: " + result.ErrorMessage, false);
				}

				this.showBalances(args);
				return true;
			}
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.WITHDRAW_DCR)) {
			if (args.arguments.size() != 2) {
				this.withdrawHelp(bot, chatid);
				return true;
			}

			Double amountFloat = null;
			AmountDCR amount = null;
			final String amount_text = args.arguments.getElementAt(0).toLowerCase();
			try {
				amountFloat = Double.parseDouble(amount_text);
				amount = new AmountDCR(amountFloat);
			} catch (final Throwable e) {
				e.printStackTrace();

				Handlers.respond(bot, chatid, "Количество монет не распознано: " + amount_text, false);
				this.withdrawHelp(bot, chatid);
				return true;

			}

			final String address_text = args.arguments.getElementAt(1);
			final StringAnalysis anal = this.walletBackEnd.analyzeString(address_text);
			if (anal.DCRAddress == null) {
				Handlers.respond(bot, chatid, "Не удалось распознать адрес для вывода: " + address_text, false);
				Handlers.respond(bot, chatid, anal.Error, false);
				this.withdrawHelp(bot, chatid);
				return true;
			}

			{
				final DCRAddress exch_address = settings.getExchangeAddressDCR();
				final TransactionResult result = this.walletBackEnd.transferDCR(exch_address, anal.DCRAddress, amount);

				if (result.Success) {

					Handlers.respond(bot, chatid, "Монеты высланы на адрес " + result.DCR_ToAddress + "", false);
					Handlers.respond(bot, chatid, "Чек операции:", false);
					Handlers.respond(args.bot, chatid, "https://dcrdata.decred.org/tx/" + result.DCR_TransactionReceipt, true);
				} else {
					Handlers.respond(bot, chatid, "Не удалось записать транзакцию: " + result.ErrorMessage, false);
				}
				this.showBalances(args);
				return true;
			}
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.SELL_PFC)) {
			if (args.arguments.size() == 0) {
				this.sellHelp(bot, chatid);
				return true;
			}

			Double amountFloat = null;
			AmountPFC amount = null;
			final String amount_text = args.arguments.getElementAt(0).toLowerCase();
			try {
				amountFloat = Double.parseDouble(amount_text);
				amount = new AmountPFC(amountFloat);

				if (amountFloat <= 0) {
					Handlers.respond(bot, chatid, "Количество монет должно быть положительным: " + amount_text, false);
					this.buyHelp(bot, chatid);
					return true;
				}
			} catch (final Throwable e) {
				e.printStackTrace();

				Handlers.respond(bot, chatid, "Количество монет не распознано: " + amount_text, false);
				this.buyHelp(bot, chatid);
				return true;
			}

			final boolean getQuote = true;
			final TradeResult result = this.walletBackEnd.tradePFC(TRADE_OPERATION.SELL, getQuote, amount);

			Handlers.respond(bot, chatid, result.toString(), false);

			return true;

		}

		if (args.command.equalsIgnoreCase(OPERATIONS.BUY_PFC)) {
			if (args.arguments.size() == 0) {
				this.buyHelp(bot, chatid);
				return true;
			}

			Double amountFloat = null;
			AmountPFC amount = null;
			final String amount_text = args.arguments.getElementAt(0).toLowerCase();
			try {
				amountFloat = Double.parseDouble(amount_text);
				amount = new AmountPFC(amountFloat);

				if (amountFloat <= 0) {
					Handlers.respond(bot, chatid, "Количество монет должно быть положительным: " + amount_text, false);
					this.buyHelp(bot, chatid);
					return true;
				}
			} catch (final Throwable e) {
				e.printStackTrace();

				Handlers.respond(bot, chatid, "Количество монет не распознано: " + amount_text, false);
				this.buyHelp(bot, chatid);
				return true;
			}

			final boolean getQuote = true;
			final TradeResult result = this.walletBackEnd.tradePFC(TRADE_OPERATION.BUY, getQuote, amount);

			Handlers.respond(bot, chatid, result.toString(), false);
			return true;

		}

		return false;
	}

	private void buyHelp (final AbsSender bot, final Long chatid) throws IOException {

		final StringBuilder b = new StringBuilder();
		b.append("Купить PFC:").append(N);
		b.append(OPERATIONS.BUY_PFC + " %количество% ").append(N);
		b.append(N);
		b.append("пример:").append(N);
		b.append(OPERATIONS.BUY_PFC + " 154.5").append(N);
		b.append(N);
		Handlers.respond(bot, chatid, b.toString(), false);

	}

	private void sellHelp (final AbsSender bot, final Long chatid) throws IOException {

		final StringBuilder b = new StringBuilder();
		b.append("Продать PFC:").append(N);
		b.append(OPERATIONS.SELL_PFC + " %количество% ").append(N);
		b.append(N);
		b.append("пример:").append(N);
		b.append(OPERATIONS.SELL_PFC + " 11.3").append(N);
		b.append(N);
		Handlers.respond(bot, chatid, b.toString(), false);

	}

	private void showMarketState (final HandleArgs args) throws IOException, BackendException {
		final StringBuilder b = new StringBuilder();

		b.append("В пуле биржи находится:").append(N);

		{
			final PFCAddress exch_pfc_address = EXCHANGE_PFC_ADDRESS();
			final PFCBalance exch_pfc_balance = this.walletBackEnd.getPFCBallance(exch_pfc_address, 1);
			b.append(exch_pfc_balance.Spendable.Value + " PFC").append(N);
			final DCRAddress exch_dcr_address = EXCHANGE_DCR_ADDRESS();
			final DCRBalance exch_dcr_balance = this.walletBackEnd.getDCRBallance(exch_dcr_address, 1);
			b.append(exch_dcr_balance.Spendable.Value + " DCR").append(N);

			final double dcr_for_1_pfc = exch_dcr_balance.Spendable.Value / exch_pfc_balance.Spendable.Value;
			final double usd_for_1_pfc = usd_for_1_pfc(dcr_for_1_pfc);
			b.append(N);
			b.append("1 PFC стоит " + round(dcr_for_1_pfc) + " DCR или " + round(usd_for_1_pfc) + "$").append(N);
			b.append(N);
			b.append(OPERATIONS.BUY_PFC + " - купить").append(N);
			b.append(OPERATIONS.SELL_PFC + " - продать").append(N);
			b.append(N);
		}
		b.append(
			"Стоимость монет считается аналогично алгоритму работы децентрализованных бирж типа UniSwap, когда цена автоматически балансируется состоянием пула.")
			.append(N);

		Handlers.respond(args.bot, args.update.message.chatID, b.toString(), false);

	}

	static String round (final double v) {
// final double r = 100000;
		return String.format("%.6f", v);
	}

	public static double usd_for_1_pfc (final double dcr_for_1_pfc) throws IOException {

		final MarketPair btcusdpair = MarketPair.newMarketPair(CoinSign.TETHER, CoinSign.BITCOIN);
		final Ticker btcusdticker = GetTicker.get(btcusdpair);
		final double usd_for_1_btc = btcusdticker.result.Last;

// L.d("usd_for_1_btc", usd_for_1_btc);

		final MarketPair dcrbtcpair = MarketPair.newMarketPair(CoinSign.BITCOIN, CoinSign.DECRED);
		final Ticker dcrbtcticker = GetTicker.get(dcrbtcpair);
		final double btc_for_1_dcr = dcrbtcticker.result.Last;

// L.d("btc_for_1_dcr", btc_for_1_dcr);

		final double usd_for_1_dcr = usd_for_1_btc * btc_for_1_dcr;

// L.d("usd_for_1_dcr", usd_for_1_dcr);

		final double usd_for_1_pfc = usd_for_1_dcr * dcr_for_1_pfc;

// L.d("usd_for_1_pfc", usd_for_1_pfc);

		// final double btc_per_pfc = Exchange.sellPriceBTC(rate);
		// final double usd_per_pfc = usd_per_btc * btc_per_pfc;

		return usd_for_1_pfc;
	}

	public static final DCRAddress EXCHANGE_DCR_ADDRESS () {
		final DCRAddress addr = new DCRAddress();
		addr.AddressString = "DsdJYEiPaw1WTC4HW5ULqj2tpLPUxzG2ECn";
		addr.Type = "DCR";
		return addr;
	}

	public static final PFCAddress EXCHANGE_PFC_ADDRESS () {
		final PFCAddress addr = new PFCAddress();
		addr.AddressString = "JsVjxJ697AAELw4TNEfXQm4pLjsMupjXLfj";
		addr.Type = "PFC";
		return addr;
	}

	private void showBalances (final HandleArgs args) throws BackendException, IOException {
		final UserSettings settings = args.settings;
		final AbsSender bot = args.bot;
		final Long chatid = args.update.message.chatID;

		final StringBuilder b = new StringBuilder();
		b.append("Твои балансы").append(N);
		b.append(N);
		{
			final PFCAddress pfc_address = settings.getExchangeAddressPFC();
			final PFCBalance pfc1 = this.walletBackEnd.getPFCBallance(pfc_address, 1);
			final PFCBalance pfc3 = this.walletBackEnd.getPFCBallance(pfc_address, 3);
			final double balance = pfc1.Spendable.Value;
			final double unconfirmed = pfc1.Unconfirmed.Value;
			final double tradable = pfc3.Spendable.Value;
			final String sign = "PFC";

			this.balanceString(b, balance, unconfirmed, tradable, sign);

		}
		b.append(N);
		{
			final DCRAddress dcr_address = settings.getExchangeAddressDCR();
			final DCRBalance dcr1 = this.walletBackEnd.getDCRBallance(dcr_address, 1);
			final DCRBalance dcr3 = this.walletBackEnd.getDCRBallance(dcr_address, 3);
			final double balance = dcr1.Spendable.Value;
			final double unconfirmed = dcr1.Unconfirmed.Value;
			final double tradable = dcr3.Spendable.Value;
			final String sign = "DCR";

			this.balanceString(b, balance, unconfirmed, tradable, sign);

		}
		b.append(N);
		b.append("Пополнить балансы - " + OPERATIONS.DEPOSIT).append(N);
		b.append("Вывести монеты с биржи - " + OPERATIONS.WITHDRAW).append(N);
		Handlers.respond(bot, chatid, b.toString(), false);
	}

	private void balanceString (final StringBuilder b, final double balance, final double unconfirmed, final double tradable,
		final String sign) {
		b.append("зачислено: " + balance + " " + sign).append(N);
		if (unconfirmed > 0) {
			b.append("ожидается: " + unconfirmed + " " + sign).append(N);
		}
		b.append("доступно для торгов: " + tradable + " " + sign).append(N);
	}

	private void withdrawHelp (final AbsSender bot, final Long chatid) throws IOException {
		final StringBuilder b = new StringBuilder();
		b.append("Команды для вывода монет с биржи").append(N);
		b.append(N);
		b.append("Вывести DCR: ").append(N);
		b.append(OPERATIONS.WITHDRAW_DCR + " %количество% %адрес%").append(N);
		b.append(N);
		b.append("Вывести PFC:").append(N);
		b.append(OPERATIONS.WITHDRAW_PFC + " %количество% %адрес%").append(N);
		b.append(N);
		b.append("Примеры").append(N);
		b.append(OPERATIONS.WITHDRAW_DCR + " 0.5 D1aBcDeFg12345abcD").append(N);
		b.append(OPERATIONS.WITHDRAW_PFC + " 120 JabcgeFg123456789H").append(N);
		b.append(N);
		Handlers.respond(bot, chatid, b.toString(), false);
	}

	private String userID (final Long chatid) {
		return "tg-" + chatid;
	}

	public static final String N = "\n";

	private void respondMenu (final AbsSender bot, final UserSettings settings, final Long chatid) throws IOException {
		try {

			final StringBuilder b = new StringBuilder();
			b.append("Этот бот-биржа продаёт и покупает пикфайт-коины (PFC) за декреды (DCR)").append(N);
			b.append(N);

			b.append("Команды для бота").append(N);
			b.append(N);
			b.append(OPERATIONS.BALANCE + " - посмотреть свои текущие балансы на бирже").append(N);
			b.append(OPERATIONS.DEPOSIT + " - пополнить балансы").append(N);
			b.append(OPERATIONS.WITHDRAW + " - вывести монеты с биржи").append(N);
			b.append(N);
			b.append(OPERATIONS.MARKET + " - информация о торгах").append(N);
			b.append(OPERATIONS.BUY_PFC + " - купить").append(N);
			b.append(OPERATIONS.SELL_PFC + " - продать").append(N);
			b.append(N);
			b.append("Обмен на BTC отключён, т.к. из-за высокой цены биткоина операции с ним стоят по $20-$50.").append(N);
			b.append(N);
			b.append("PFC-кошелёк можно скачать тут: https://github.com/picfight/pfcredit").append(N);
			b.append("DCR можно купить и продать на Бинансе: https://www.binance.com/en/trade/DCR_BTC").append(N);

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
