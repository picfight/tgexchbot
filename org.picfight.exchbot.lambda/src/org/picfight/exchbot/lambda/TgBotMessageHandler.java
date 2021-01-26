
package org.picfight.exchbot.lambda;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import org.picfight.exchbot.lambda.backend.AmountBTC;
import org.picfight.exchbot.lambda.backend.AmountPFC;
import org.picfight.exchbot.lambda.backend.BTCAddress;
import org.picfight.exchbot.lambda.backend.BTCBalance;
import org.picfight.exchbot.lambda.backend.BackendException;
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

import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.names.Names;
import com.jfixby.scarabei.api.sys.settings.SystemSettings;

public class TgBotMessageHandler implements Handler {
	public static final String WALLET_CHECK = "/walletcheck";
	private static final double SPREAD = 0.01;
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
			Handlers.respond(args.bot, chatid, "Bot is disabled for maintenance", false);
			return true;
		}

		final String accountName = this.userID(chatid);

		final UserSettings settings = this.transactionsBackEnd.getUserSettings(accountName, args.filesystem);
		args.settings = settings;

		if (args.command.equalsIgnoreCase(OPERATIONS.RESET_LANG)) {
			args.settings.setLanguage(null);
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.SET_LANG_CH)) {
			settings.setLanguage(UserSettingsLanguage.CH);
			this.respondMenu(args);
			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.SET_LANG_EN)) {
			settings.setLanguage(UserSettingsLanguage.EN);
			this.respondMenu(args);
			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.SET_LANG_RU)) {
			settings.setLanguage(UserSettingsLanguage.RU);
			this.respondMenu(args);
			return true;
		}

		if (!settings.languageIsSet()) {
			this.respondChooseLang(args.bot, settings, chatid);
			return true;
		}

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

		if (false || //
			args.command.equalsIgnoreCase(OPERATIONS.MENU) || //
			args.command.equalsIgnoreCase(OPERATIONS.START) || //
			args.command.equalsIgnoreCase(OPERATIONS.HELP) || //
			false) {
			this.respondMenu(args);
			return true;
		}

		L.e("Command not found", args.command);
		this.respondMenu(args);
		return true;
	}

	private void respondChooseLang (final AbsSender bot, final UserSettings settings, final Long chatid) throws IOException {

		final StringBuilder b = new StringBuilder();
		b.append("Set language").append(N);
		b.append(N);
		b.append(OPERATIONS.SET_LANG_RU + " - русский").append(N);
		b.append(OPERATIONS.SET_LANG_EN + " - english").append(N);
		b.append(OPERATIONS.SET_LANG_CH + " - 中文").append(N);
		Handlers.respond(bot, chatid, b.toString(), false);

	}

	private boolean processWithBackend (final HandleArgs args) throws BackendException, IOException {
		final UserSettings settings = args.settings;
		final AbsSender bot = args.bot;
		final Long chatid = args.update.message.chatID;

		if (!settings.exchangeAddressIsSet()) {
			settings.setupExchangeAddress(this.walletBackEnd);
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
			b.append(Translate.translate(args.settings.getLanguage(), Translate.TO_DEPOSIT)).append(N);
			b.append(N);
			b.append(Translate.translate(args.settings.getLanguage(), Translate.TO_DEPOSIT_BTC) + ": " + OPERATIONS.DEPOSIT_BTC)
				.append(N);
			b.append(Translate.translate(args.settings.getLanguage(), Translate.TO_DEPOSIT_PFC) + ": " + OPERATIONS.DEPOSIT_PFC)
				.append(N);
			Handlers.respond(bot, chatid, b.toString(), false);
			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.DEPOSIT_BTC)) {
			final BTCAddress btc_address = settings.getExchangeAddressBTC();
			Handlers.respond(bot, chatid, Translate.translate(args.settings.getLanguage(), Translate.DO_DEPOSIT_BTC) + ":", false);
			Handlers.respond(bot, chatid, btc_address.toString(), false);
			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.DEPOSIT_PFC)) {
			final PFCAddress pfc_address = settings.getExchangeAddressPFC();
			Handlers.respond(bot, chatid, Translate.translate(args.settings.getLanguage(), Translate.DO_DEPOSIT_PFC) + ":", false);
			Handlers.respond(bot, chatid, pfc_address.toString(), false);
			return true;
		}
		if (args.command.equalsIgnoreCase(OPERATIONS.WITHDRAW)) {
			this.withdrawHelp(args, chatid);
			return true;
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.WITHDRAW_PFC)) {
			if (args.arguments.size() != 2) {
				this.withdrawHelp(args, chatid);
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

				Handlers.respond(bot, chatid,
					Translate.translate(settings.getLanguage(), Translate.ERROR_UNRECOGNIZED_AMOUNT) + ": " + amount_text, false);
				this.withdrawHelp(args, chatid);
				return true;

			}
// Handlers.respond(bot, chatid,
// Translate.translate(settings.getLanguage(), Translate.TRANSACTION_FAILED) + ": " + result.ErrorMessage, false);
			final String address_text = args.arguments.getElementAt(1);
			final StringAnalysis anal = this.walletBackEnd.analyzeString(address_text);
			if (anal.PFCAddress == null) {
				Handlers.respond(bot, chatid,
					Translate.translate(settings.getLanguage(), Translate.UNRECOGNIZED_ADDRESS) + ": " + address_text, false);
				Handlers.respond(bot, chatid, anal.Error, false);
				this.withdrawHelp(args, chatid);
				return true;
			}

			{
				final PFCAddress exch_address = settings.getExchangeAddressPFC();
				final TransactionResult result = this.walletBackEnd.transferPFC(exch_address, anal.PFCAddress, amount);

				if (result.Success) {
					Handlers.respond(bot, chatid,
						Translate.translate(settings.getLanguage(), Translate.COINS_WERE_SENT) + " " + result.PFC_ToAddress + "",
						false);
					Handlers.respond(bot, chatid, Translate.translate(settings.getLanguage(), Translate.TRANSACTION_RECEIPT) + ":",
						false);
					Handlers.respond(args.bot, chatid, "http://explorer.picfight.org/tx/" + result.PFC_TransactionReceipt, true);
				} else {
					Handlers.respond(bot, chatid,
						Translate.translate(settings.getLanguage(), Translate.TRANSACTION_FAILED) + ": " + result.ErrorMessage, false);
				}

				this.showBalances(args);
				return true;
			}
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.WITHDRAW_BTC)) {
			if (args.arguments.size() != 2) {
				this.withdrawHelp(args, chatid);
				return true;
			}

			Double amountFloat = null;
			AmountBTC amount = null;
			final String amount_text = args.arguments.getElementAt(0).toLowerCase();
			try {
				amountFloat = Double.parseDouble(amount_text);
				amount = new AmountBTC(amountFloat);
			} catch (final Throwable e) {
				e.printStackTrace();

				Handlers.respond(bot, chatid,
					Translate.translate(settings.getLanguage(), Translate.ERROR_UNRECOGNIZED_AMOUNT) + ": " + amount_text, false);
				this.withdrawHelp(args, chatid);
				return true;

			}

			final String address_text = args.arguments.getElementAt(1);
			final StringAnalysis anal = this.walletBackEnd.analyzeString(address_text);
			if (anal.BTCAddress == null) {
				Handlers.respond(bot, chatid,
					Translate.translate(settings.getLanguage(), Translate.UNRECOGNIZED_ADDRESS) + ": " + address_text, false);
				Handlers.respond(bot, chatid, anal.Error, false);
				this.withdrawHelp(args, chatid);
				return true;
			}

			{
				final BTCAddress exch_address = settings.getExchangeAddressBTC();
				final TransactionResult result = this.walletBackEnd.transferBTC(exch_address, anal.BTCAddress, amount);

				if (result.Success) {

					Handlers.respond(bot, chatid,
						Translate.translate(settings.getLanguage(), Translate.COINS_WERE_SENT) + " " + result.BTC_ToAddress + "",
						false);
					Handlers.respond(bot, chatid, Translate.translate(settings.getLanguage(), Translate.TRANSACTION_RECEIPT) + ":",
						false);
					Handlers.respond(args.bot, chatid, "https://www.blockchain.com/btc/tx" + result.BTC_TransactionReceipt, true);
				} else {
					Handlers.respond(bot, chatid,
						Translate.translate(settings.getLanguage(), Translate.TRANSACTION_FAILED) + ": " + result.ErrorMessage, false);
				}
				this.showBalances(args);
				return true;
			}
		}

		if (args.command.equalsIgnoreCase(OPERATIONS.SELL_PFC)) {
			if (args.arguments.size() == 0) {
				this.sellHelp(args, chatid);
				return true;
			}

			Double amountFloat = null;
			AmountPFC amount = null;
			final String amount_text = args.arguments.getElementAt(0).toLowerCase();
			try {
				amountFloat = Double.parseDouble(amount_text);
				amount = new AmountPFC(amountFloat);

				if (amountFloat <= 0) {
					Handlers.respond(bot, chatid,
						Translate.translate(settings.getLanguage(), Translate.ERROR_POSITIVE_AMOUNT_REQURED) + ": " + amount_text,
						false);
					this.buyHelp(args, chatid);
					return true;
				}
			} catch (final Throwable e) {
				e.printStackTrace();

				Handlers.respond(bot, chatid,
					Translate.translate(settings.getLanguage(), Translate.ERROR_UNRECOGNIZED_AMOUNT) + ": " + amount_text, false);

				this.buyHelp(args, chatid);
				return true;
			}

			boolean getQuote = true;
			Double btc_for_1_pfc_order = 0.0d;
			if (args.arguments.size() == 3) {
				final String price_text = args.arguments.getElementAt(1).toLowerCase();
				try {
					btc_for_1_pfc_order = Double.parseDouble(price_text);
				} catch (final Throwable e) {
					e.printStackTrace();
					Handlers.respond(bot, chatid,
						Translate.translate(settings.getLanguage(), Translate.ERROR_UNRECOGNIZED_PRICE) + ": " + price_text, false);
					this.buyHelp(args, chatid);
					return true;
				}
				final String execute_text = args.arguments.getElementAt(2).toLowerCase();
				if ("execute".equals(execute_text)) {
					getQuote = false;
				}
			}

			final PFCAddress user_pfc_account = settings.getExchangeAddressPFC();
			final BTCAddress user_btc_account = settings.getExchangeAddressBTC();

			final PFCAddress exchange_pfc_account = EXCHANGE_PFC_ADDRESS();
			final BTCAddress exchange_btc_account = EXCHANGE_BTC_ADDRESS();

			final TradeResult result = this.walletBackEnd.tradePFC(//
				TRADE_OPERATION.SELL, //
				getQuote, //
				amount, //
				btc_for_1_pfc_order, //
				user_pfc_account, //
				user_btc_account, //
				exchange_pfc_account, //
				exchange_btc_account//
			);
			if (result.Success) {
				final double btc_for_1_pfc = result.BTCPFC_Executed_Price;
				final double usd_for_1_pfc = usd_for_1_pfc(btc_for_1_pfc);
				final double amount_usd = usd_for_1_pfc * result.PFC_Executed_Amount.Value;

				if (!result.Executed) {
					final StringBuilder b = new StringBuilder();
					b.append(Translate.translate(settings.getLanguage(), Translate.ORDER_FOR_EXECUTION) + ": "
						+ Translate.translate(settings.getLanguage(), Translate.ORDER_TO_SELL)).append(N);
					b.append(N);
					b.append(Translate.translate(settings.getLanguage(), Translate.TotalAmount) + ": ").append(N);
					b.append(result.PFC_Executed_Amount + " = " + round(result.BTC_Executed_Amount.Value, 8) + " BTC ("
						+ round(amount_usd, 2) + "$)").append(N);
					b.append(N);
					b.append(Translate.translate(settings.getLanguage(), Translate.Price) + ":").append(N);
					b.append("1 PFC = " + round(btc_for_1_pfc, 8) + " BTC = " + round(usd_for_1_pfc, 2) + "$").append(N);
					b.append(N);
					b.append(Translate.translate(settings.getLanguage(), Translate.TO_EXECUTE_ORDER) + ":");
					Handlers.respond(bot, chatid, b.toString(), false);

					Handlers.respond(bot, chatid, OPERATIONS.SELL_PFC + " " + round(result.PFC_Executed_Amount.Value, 8) + " "
						+ round(btc_for_1_pfc * (1 - SPREAD), 8) + " execute", false);
				} else {
					final StringBuilder b = new StringBuilder();
					b.append(Translate.translate(settings.getLanguage(), Translate.ORDER_EXECUTED) + ": "
						+ Translate.translate(settings.getLanguage(), Translate.ORDER_TO_SELL)).append(N);
					b.append(N);
					b.append(Translate.translate(settings.getLanguage(), Translate.TotalAmount) + ": ").append(N);
					b.append(result.PFC_Executed_Amount + " = " + round(result.BTC_Executed_Amount.Value, 8) + " BTC ("
						+ round(amount_usd, 2) + "$)").append(N);
					b.append(N);
					b.append(Translate.translate(settings.getLanguage(), Translate.Price) + ":").append(N);
					b.append("1 PFC = " + round(btc_for_1_pfc, 8) + " BTC = " + round(usd_for_1_pfc, 2) + "$").append(N);
					b.append(N);
					Handlers.respond(bot, chatid, b.toString(), false);
					this.saveOrder(args, result);
					this.showBalances(args);
				}
			} else {
				if (result.NoEnoughFunds) {
					Handlers.respond(bot, chatid,
						Translate.translate(settings.getLanguage(), Translate.NoEnoughCoinsForOrder) + result.PFC_Executed_Amount,
						false);
					this.showBalances(args);
				} else if (result.PriceNotMet) {
					final StringBuilder b = new StringBuilder();
					b.append(Translate.translate(settings.getLanguage(), Translate.ProceFor1PFCDrionExecution)).append(N);
					b.append(round(result.BTCPFC_Executed_Price, 8) + " BTC").append(N);
					b.append(Translate.translate(settings.getLanguage(), Translate.WasBelowLim)).append(N);
					b.append(round(result.Requested_Price_Btc_for_1_pfc, 8) + " BTC").append(N);
					b.append(N);
					b.append(Translate.translate(settings.getLanguage(), Translate.OrderWasNotExecuted));
					Handlers.respond(bot, chatid, b.toString(), false);
					this.showMarketState(args);
				} else if (result.UnfinishedTransaction) {
					this.reportUnfinishedTransaction(args, result);
				} else if (result.MinBTCAmountError) {
					this.reportMinBTCAmountError(args, result);
				} else {
					Handlers.respond(bot, chatid,
						Translate.translate(settings.getLanguage(), Translate.FailedToSubmitorder) + ": " + result.ErrorMessage, false);
				}
			}

			return true;

		}

		if (args.command.equalsIgnoreCase(OPERATIONS.BUY_PFC)) {
			if (args.arguments.size() == 0) {
				this.buyHelp(args, chatid);
				return true;
			}

			Double amountFloat = null;
			AmountPFC amount = null;
			final String amount_text = args.arguments.getElementAt(0).toLowerCase();
			try {
				amountFloat = Double.parseDouble(amount_text);
				amount = new AmountPFC(amountFloat);

				if (amountFloat <= 0) {
					Handlers.respond(bot, chatid,
						Translate.translate(settings.getLanguage(), Translate.ERROR_POSITIVE_AMOUNT_REQURED) + ": " + amount_text,
						false);
					this.buyHelp(args, chatid);
					return true;
				}
			} catch (final Throwable e) {
				e.printStackTrace();

				Handlers.respond(bot, chatid,
					Translate.translate(settings.getLanguage(), Translate.ERROR_UNRECOGNIZED_AMOUNT) + ": " + amount_text, false);

				this.buyHelp(args, chatid);
				return true;
			}

			boolean getQuote = true;
			Double btc_for_1_pfc_order = 0.0d;
			if (args.arguments.size() == 3) {
				final String price_text = args.arguments.getElementAt(1).toLowerCase();
				try {
					btc_for_1_pfc_order = Double.parseDouble(price_text);
				} catch (final Throwable e) {
					e.printStackTrace();
					Handlers.respond(bot, chatid,
						Translate.translate(settings.getLanguage(), Translate.ERROR_UNRECOGNIZED_PRICE) + ": " + price_text, false);
					this.buyHelp(args, chatid);
					return true;
				}
				final String execute_text = args.arguments.getElementAt(2).toLowerCase();
				if ("execute".equals(execute_text)) {
					getQuote = false;
				}
			}
			final PFCAddress user_pfc_account = settings.getExchangeAddressPFC();
			final BTCAddress user_btc_account = settings.getExchangeAddressBTC();

			final PFCAddress exchange_pfc_account = EXCHANGE_PFC_ADDRESS();
			final BTCAddress exchange_btc_account = EXCHANGE_BTC_ADDRESS();

			final TradeResult result = this.walletBackEnd.tradePFC(//
				TRADE_OPERATION.BUY, //
				getQuote, //
				amount, //
				btc_for_1_pfc_order, //
				user_pfc_account, //
				user_btc_account, //
				exchange_pfc_account, //
				exchange_btc_account//
			);

			if (result.Success) {
				final double btc_for_1_pfc = result.BTCPFC_Executed_Price;
				final double usd_for_1_pfc = usd_for_1_pfc(btc_for_1_pfc);
				final double amount_usd = usd_for_1_pfc * result.PFC_Executed_Amount.Value;

				if (!result.Executed) {
					final StringBuilder b = new StringBuilder();
					b.append(Translate.translate(settings.getLanguage(), Translate.ORDER_FOR_EXECUTION) + ": "
						+ Translate.translate(settings.getLanguage(), Translate.ORDER_TO_BUY)).append(N);
					b.append(N);
					b.append(Translate.translate(settings.getLanguage(), Translate.TotalAmount) + ": ").append(N);
					b.append(result.PFC_Executed_Amount + " = " + round(result.BTC_Executed_Amount.Value, 8) + " BTC ("
						+ round(amount_usd, 2) + "$)").append(N);
					b.append(N);
					b.append(Translate.translate(settings.getLanguage(), Translate.Price) + ":").append(N);
					b.append("1 PFC = " + round(btc_for_1_pfc, 8) + " BTC = " + round(usd_for_1_pfc, 2) + "$").append(N);
					b.append(N);
					b.append(Translate.translate(settings.getLanguage(), Translate.TO_EXECUTE_ORDER) + ":");
					Handlers.respond(bot, chatid, b.toString(), false);

					Handlers.respond(bot, chatid, OPERATIONS.BUY_PFC + " " + round(result.PFC_Executed_Amount.Value, 8) + " "
						+ round(btc_for_1_pfc * (1 + SPREAD), 8) + " execute", false);
				} else {
					final StringBuilder b = new StringBuilder();
					b.append(Translate.translate(settings.getLanguage(), Translate.ORDER_EXECUTED) + ": "
						+ Translate.translate(settings.getLanguage(), Translate.ORDER_TO_BUY)).append(N);
					b.append(N);
					b.append(Translate.translate(settings.getLanguage(), Translate.TotalAmount) + ": ").append(N);
					b.append(result.PFC_Executed_Amount + " = " + round(result.BTC_Executed_Amount.Value, 8) + " BTC ("
						+ round(amount_usd, 2) + "$)").append(N);
					b.append(N);
					b.append(Translate.translate(settings.getLanguage(), Translate.Price) + ":").append(N);
					b.append("1 PFC = " + round(btc_for_1_pfc, 8) + " BTC = " + round(usd_for_1_pfc, 2) + "$").append(N);
					b.append(N);
					Handlers.respond(bot, chatid, b.toString(), false);

					this.saveOrder(args, result);
					this.showBalances(args);
				}
			} else {
				if (result.NoEnoughFunds) {
					Handlers.respond(bot, chatid,
						Translate.translate(settings.getLanguage(), Translate.NoEnoughCoinsForOrder) + result.BTC_Executed_Amount,
						false);
					this.showBalances(args);
				} else if (result.PriceNotMet) {
					final StringBuilder b = new StringBuilder();
					b.append(Translate.translate(settings.getLanguage(), Translate.ProceFor1PFCDrionExecution)).append(N);
					b.append(round(result.BTCPFC_Executed_Price, 8) + " BTC").append(N);
					b.append(Translate.translate(settings.getLanguage(), Translate.WasAboveLim)).append(N);
					b.append(round(result.Requested_Price_Btc_for_1_pfc, 8) + " BTC").append(N);
					b.append(N);
					b.append(Translate.translate(settings.getLanguage(), Translate.OrderWasNotExecuted));
					Handlers.respond(bot, chatid, b.toString(), false);
					this.showMarketState(args);
				} else if (result.UnfinishedTransaction) {
					this.reportUnfinishedTransaction(args, result);
				} else if (result.MinBTCAmountError) {
					this.reportMinBTCAmountError(args, result);
				} else {
					Handlers.respond(bot, chatid,
						Translate.translate(settings.getLanguage(), Translate.FailedToSubmitorder) + ": " + result.ErrorMessage, false);
				}
			}
			return true;
		}
		return false;
	}

	private void reportMinBTCAmountError (final HandleArgs args, final TradeResult result) throws IOException {
		final UserSettings settings = args.settings;
		final AbsSender bot = args.bot;
		final Long chatid = args.update.message.chatID;
		final StringBuilder b = new StringBuilder();
		b.append(Translate.translate(settings.getLanguage(), Translate.EXCHANGE_BTC_AMOUNT_IS_TOO_SMALL)).append(N);
		b.append(
			Translate.translate(settings.getLanguage(), Translate.EXCHANGE_BTC_AMOUNT_EXECUTED) + ": " + result.BTC_Executed_Amount)
			.append(N);
		b.append(Translate.translate(settings.getLanguage(), Translate.EXCHANGE_BTC_AMOUNT_MIN) + ": " + result.MinBTCAmountValue)
			.append(N);
		Handlers.respond(bot, chatid, b.toString(), false);
	}

	private void reportUnfinishedTransaction (final HandleArgs args, final TradeResult result) throws IOException {
		final UserSettings settings = args.settings;
		final AbsSender bot = args.bot;
		final Long chatid = args.update.message.chatID;
		Handlers.respond(bot, chatid, Translate.translate(settings.getLanguage(), Translate.POOL_LEAK) + ":", false);
		Handlers.respond(bot, chatid, "" + result.toString(), false);
	}

	private void buyHelp (final HandleArgs args, final Long chatid) throws IOException {
		final UserSettings settings = args.settings;
		final AbsSender bot = args.bot;
		final StringBuilder b = new StringBuilder();
		b.append(Translate.translate(settings.getLanguage(), Translate.TO_BUY_PFC)).append(N);
		b.append(OPERATIONS.BUY_PFC + " %" + Translate.translate(settings.getLanguage(), Translate.Amount) + "%").append(N);
		b.append(N);
		b.append(Translate.translate(settings.getLanguage(), Translate.Example)).append(N);
		b.append(OPERATIONS.BUY_PFC + " 154.5").append(N);
		b.append(N);
		Handlers.respond(bot, chatid, b.toString(), false);

	}

	private void sellHelp (final HandleArgs args, final Long chatid) throws IOException {
		final UserSettings settings = args.settings;
		final AbsSender bot = args.bot;
		final StringBuilder b = new StringBuilder();
		b.append(Translate.translate(settings.getLanguage(), Translate.TO_SELL_PFC)).append(N);
		b.append(OPERATIONS.SELL_PFC + " %" + Translate.translate(settings.getLanguage(), Translate.Amount) + "%").append(N);
		b.append(N);
		b.append(Translate.translate(settings.getLanguage(), Translate.Example)).append(N);
		b.append(OPERATIONS.SELL_PFC + " 11.3").append(N);
		b.append(N);
		Handlers.respond(bot, chatid, b.toString(), false);

	}

	private void showMarketState (final HandleArgs args) throws IOException, BackendException {
		final StringBuilder b = new StringBuilder();
		final UserSettings settings = args.settings;

		b.append(Translate.translate(settings.getLanguage(), Translate.POOL_STATE) + "").append(N);
		b.append(N);
		{
			final PFCAddress exch_pfc_address = EXCHANGE_PFC_ADDRESS();
			final PFCBalance exch_pfc_balance = this.walletBackEnd.getPFCBallance(exch_pfc_address, 1);
			final BTCAddress exch_btc_address = EXCHANGE_BTC_ADDRESS();
			final BTCBalance exch_btc_balance = this.walletBackEnd.getBTCBallance(exch_btc_address, 1);
			{

				final double unconfirmed_pfc = exch_pfc_balance.Unconfirmed.Value;
				final double balance_pfc = exch_pfc_balance.Spendable.Value;
				final double unconfirmed_btc = exch_btc_balance.Unconfirmed.Value;
				final double balance_btc = exch_btc_balance.Spendable.Value;

				b.append(Translate.translate(settings.getLanguage(), Translate.Available) + "").append(N);
				b.append("" + balance_pfc + " PFC ").append(N);
				b.append("" + balance_btc + " BTC ").append(N);
				b.append(N);
				if (unconfirmed_btc > 0 || unconfirmed_pfc > 0) {
					b.append(Translate.translate(settings.getLanguage(), Translate.Unconfirmed) + "").append(N);
					if (unconfirmed_pfc > 0) {
						b.append("" + unconfirmed_pfc + " PFC ").append(N);
					}
					if (unconfirmed_btc > 0) {
						b.append("" + unconfirmed_btc + " BTC ").append(N);
					}
					b.append(N);
				}
			}

			if (exch_btc_balance.Spendable.Value != 0 && exch_pfc_balance.Spendable.Value != 0) {
				final double btc_for_1_pfc = exch_btc_balance.Spendable.Value / exch_pfc_balance.Spendable.Value;
				final double usd_for_1_pfc = usd_for_1_pfc(btc_for_1_pfc);
				b.append("1 PFC = " + round(btc_for_1_pfc, 8) + " BTC = " + round(usd_for_1_pfc, 2) + "$").append(N);
				b.append(N);

				b.append(OPERATIONS.BUY_PFC + " - " + Translate.translate(settings.getLanguage(), Translate.BUY_PFC)).append(N);
				b.append(OPERATIONS.SELL_PFC + " - " + Translate.translate(settings.getLanguage(), Translate.SELL_PFC)).append(N);
				b.append(OPERATIONS.BALANCE + " - " + Translate.translate(settings.getLanguage(), Translate.BALANCE)).append(N);

				b.append(N);
			} else {
				b.append(Translate.translate(settings.getLanguage(), Translate.POOL_IS_BALANCING)).append(N);
				b.append(OPERATIONS.BALANCE + " - " + Translate.translate(settings.getLanguage(), Translate.BALANCE)).append(N);
				b.append(N);
				b.append(OPERATIONS.MARKET + " - " + Translate.translate(settings.getLanguage(), Translate.MARKET)).append(N);
				b.append(OPERATIONS.BALANCE + " - " + Translate.translate(settings.getLanguage(), Translate.BALANCE)).append(N);
				b.append(N);
			}
		}
		b.append(Translate.translate(settings.getLanguage(), Translate.UniSwap)).append(N);

		Handlers.respond(args.bot, args.update.message.chatID, b.toString(), false);

	}

	static String round (final double v, final int digit) {
		final BigDecimal number = new BigDecimal(String.format("%." + digit + "f", v));
		return (number.stripTrailingZeros().toPlainString());
	}

	public static double usd_for_1_pfc (final double btc_for_1_pfc) throws IOException {
		final MarketPair btcusdpair = MarketPair.newMarketPair(CoinSign.TETHER, CoinSign.BITCOIN);
		final Ticker btcusdticker = GetTicker.get(btcusdpair);
		final double usd_for_1_btc = btcusdticker.result.Last;
		final double usd_for_1_pfc = usd_for_1_btc * btc_for_1_pfc;
		return usd_for_1_pfc;
	}

	public static final BTCAddress EXCHANGE_BTC_ADDRESS () {
		final BTCAddress addr = new BTCAddress();
		addr.AddressString = "12LU2RHsb4aLad54DXYar2EtqhR3v22ZJj";
		addr.Type = "BTC";
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
		b.append(Translate.translate(settings.getLanguage(), Translate.Your_balances)).append(N);
		b.append(N);
		{
			final PFCAddress user_pfc_address = settings.getExchangeAddressPFC();
			final PFCBalance user_pfc_balance = this.walletBackEnd.getPFCBallance(user_pfc_address, 1);
			final BTCAddress user_btc_address = settings.getExchangeAddressBTC();
			final BTCBalance user_btc_balance = this.walletBackEnd.getBTCBallance(user_btc_address, 1);
			{

				final double unconfirmed_pfc = user_pfc_balance.Unconfirmed.Value;
				final double balance_pfc = user_pfc_balance.Spendable.Value;
				final double unconfirmed_btc = user_btc_balance.Unconfirmed.Value;
				final double balance_btc = user_btc_balance.Spendable.Value;

				b.append(Translate.translate(settings.getLanguage(), Translate.Available) + "").append(N);
				b.append("" + balance_pfc + " PFC ").append(N);
				b.append("" + balance_btc + " BTC ").append(N);
				b.append(N);
				if (unconfirmed_btc > 0 || unconfirmed_pfc > 0) {
					b.append(Translate.translate(settings.getLanguage(), Translate.Unconfirmed) + "").append(N);
					if (unconfirmed_pfc > 0) {
						b.append("" + unconfirmed_pfc + " PFC ").append(N);
					}
					if (unconfirmed_btc > 0) {
						b.append("" + unconfirmed_btc + " BTC ").append(N);
					}
					b.append(N);
				}
			}
		}

		b.append(OPERATIONS.DEPOSIT + " - " + Translate.translate(settings.getLanguage(), Translate.DEPOSIT)).append(N);
		b.append(OPERATIONS.WITHDRAW + " - " + Translate.translate(settings.getLanguage(), Translate.WITHDRAW)).append(N);
		b.append(OPERATIONS.MARKET + " - " + Translate.translate(settings.getLanguage(), Translate.MARKET)).append(N);
		b.append(OPERATIONS.BUY_PFC + " - " + Translate.translate(settings.getLanguage(), Translate.BUY_PFC)).append(N);
		b.append(OPERATIONS.SELL_PFC + " - " + Translate.translate(settings.getLanguage(), Translate.SELL_PFC)).append(N);
		b.append(OPERATIONS.BALANCE + " - " + Translate.translate(settings.getLanguage(), Translate.BALANCE)).append(N);

		Handlers.respond(bot, chatid, b.toString(), false);
	}

	private void withdrawHelp (final HandleArgs args, final Long chatid) throws IOException {
		final AbsSender bot = args.bot;
		final UserSettings settings = args.settings;
		final StringBuilder b = new StringBuilder();
		b.append(Translate.translate(settings.getLanguage(), Translate.TO_WITHDRAW)).append(N);
		b.append(N);
		b.append(Translate.translate(settings.getLanguage(), Translate.TO_WITHDRAW_BTC) + " ").append(N);
		b.append(OPERATIONS.WITHDRAW_BTC + " %" + Translate.translate(settings.getLanguage(), Translate.Amount) + "% %"
			+ Translate.translate(settings.getLanguage(), Translate.Adress) + "%").append(N);
		b.append(N);
		b.append(Translate.translate(settings.getLanguage(), Translate.TO_WITHDRAW_PFC) + " ").append(N);
		b.append(OPERATIONS.WITHDRAW_PFC + " %" + Translate.translate(settings.getLanguage(), Translate.Amount) + "% %"
			+ Translate.translate(settings.getLanguage(), Translate.Adress) + "%").append(N);
		b.append(N);
		b.append(Translate.translate(settings.getLanguage(), Translate.Examples) + " ").append(N);
		b.append(OPERATIONS.WITHDRAW_BTC + " 0.5 11aBcDeFg12345abcD").append(N);
		b.append(OPERATIONS.WITHDRAW_PFC + " 120 JabcgeFg123456789H").append(N);
		b.append(N);
		Handlers.respond(bot, chatid, b.toString(), false);
	}

	private String userID (final Long chatid) {
		return "tg-" + chatid;
	}

	public static final String N = "\n";

	/** @param bot
	 * @param settings
	 * @param chatid
	 * @throws IOException */
	private void respondMenu (final HandleArgs args) throws IOException {

		final AbsSender bot = args.bot;
		final UserSettings settings = args.settings;
		final Long chatid = args.update.message.chatID;

		try {

			final StringBuilder b = new StringBuilder();
			b.append(Translate.translate(settings.getLanguage(), Translate.THIS_BOT)).append(N);
			{
				b.append(N);
				final PFCAddress exch_pfc_address = EXCHANGE_PFC_ADDRESS();
				final PFCBalance exch_pfc_balance = this.walletBackEnd.getPFCBallance(exch_pfc_address, 1);
				final BTCAddress exch_btc_address = EXCHANGE_BTC_ADDRESS();
				final BTCBalance exch_btc_balance = this.walletBackEnd.getBTCBallance(exch_btc_address, 1);
				final double btc_for_1_pfc = exch_btc_balance.Spendable.Value / exch_pfc_balance.Spendable.Value;
				final double usd_for_1_pfc = usd_for_1_pfc(btc_for_1_pfc);
				b.append("1 PFC = " + round(btc_for_1_pfc, 8) + " BTC = " + round(usd_for_1_pfc, 2) + "$").append(N);
			}
			b.append(N);

			b.append(Translate.translate(settings.getLanguage(), Translate.USE_THIS)).append(N);
			b.append(OPERATIONS.DEPOSIT + " - " + Translate.translate(settings.getLanguage(), Translate.DEPOSIT)).append(N);
			b.append(OPERATIONS.BALANCE + " - " + Translate.translate(settings.getLanguage(), Translate.BALANCE)).append(N);
			b.append(OPERATIONS.BUY_PFC + " - " + Translate.translate(settings.getLanguage(), Translate.BUY_PFC)).append(N);
			b.append(OPERATIONS.SELL_PFC + " - " + Translate.translate(settings.getLanguage(), Translate.SELL_PFC)).append(N);
			b.append(OPERATIONS.WITHDRAW + " - " + Translate.translate(settings.getLanguage(), Translate.WITHDRAW)).append(N);
			b.append(OPERATIONS.RESET_LANG + " - " + Translate.translate(settings.getLanguage(), Translate.RESET_LANG)).append(N);
			b.append(OPERATIONS.MARKET + " - " + Translate.translate(settings.getLanguage(), Translate.MARKET)).append(N);
			b.append(N);
// b.append(Translate.translate(settings.getLanguage(), Translate.NO_BTC)).append(N);
// b.append(N);
			b.append(
				Translate.translate(settings.getLanguage(), Translate.PFC_WALLET) + ": https://github.com/picfight/pfcredit/releases")
				.append(N);
			b.append(N);

			Handlers.respond(bot, chatid, b.toString(), false);

		} catch (final Throwable e) {
			L.e(e);

			final StringBuilder b = new StringBuilder();
			b.append("Backend is not responding: " + e.toString());
			b.append(N);

			Handlers.respond(bot, chatid, b.toString(), false);
		}

	}

	private ExecutedOrder loadLastOrder (final HandleArgs args) throws IOException {
		final File orderFile = args.filesystem.Executed.listAllChildren().getLast();
		return orderFile.readJson(ExecutedOrder.class);
	}

	private void saveOrder (final HandleArgs args, final TradeResult result) throws IOException {
		final String filename = System.currentTimeMillis() + ".json";
		final File orderFile = args.filesystem.Executed.child(filename);
		final ExecutedOrder order = new ExecutedOrder();
		order.timestamp = System.currentTimeMillis();
		order.date = (new Date(order.timestamp)).toString();
		order.price = result.BTCPFC_Executed_Price;
		order.size = result.PFC_Executed_Amount.Value;
		orderFile.writeJson(order);
	}

}
