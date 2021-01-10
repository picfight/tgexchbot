
package org.picfight.exchbot.lambda;

import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.collections.Map;

public class Translate {

	public static final Map<UserSettingsLanguage, Map<String, String>> mappings = Collections.newMap();
	public static final String THIS_BOT = "Этот бот-биржа продаёт и покупает пикфайт-коины (PFC) за декреды (DCR)";
	public static final String USE_THIS = "Используй следующие команды:";
	public static final String DEPOSIT = "зачислить монеты на биржу";
	public static final String BALANCE = "посмотреть свои текущие балансы на бирже";
	public static final String BUY_PFC = "купить монеты";
	public static final String SELL_PFC = "продать монеты";
	public static final String WITHDRAW = "вывести монеты с биржи на свой кошелёк";
	public static final String MARKET = "информация о состоянии торгового пула";
	public static final String RESET_LANG = "сменить язык";
	public static final String NO_BTC = "Обмен на BTC отключён, т.к. из-за высокой цены биткоина операции с ним стоят по $20-$50";
	public static final String PFC_WALLET = "PFC-кошелёк можно скачать тут";
	public static final String DCR_BINANCE = "DCR можно купить и продать на Бинансе";

	public static final String POOL_LEAK = "Похоже пул протёк. Отправь @JFixby следующий отчёт об ошибке";

	public static final String TO_DEPOSIT = "Команды для зачисления средств на биржу";
	public static final String TO_WITHDRAW = "Команды для вывода монет с биржи";

	public static final String TO_DEPOSIT_DCR = "пополнить DCR";
	public static final String TO_DEPOSIT_PFC = "пополнить PFC";

	public static final String TO_WITHDRAW_DCR = "вывести DCR";
	public static final String TO_WITHDRAW_PFC = "вывести PFC";

	public static final String POOL_STATE = "Состояние пула биржи";

	public static final String Your_balances = "Твои балансы";

	public static final String OrderWasNotExecuted = "Ордер не выполнился";

	public static final String POOL_IS_BALANCING = "Пул балансируется...";

	public static final String Examples = "примеры";
	public static final String Example = "пример";

	public static final String ProceFor1PFCDrionExecution = "Цена за 1 PFC на бирже в момент выполнения ордера";

	public static final String Available = "Доступно для торгов";
	public static final String Unconfirmed = "и ещё ожидается";

	public static final String UniSwap = "Стоимость монет считается аналогично алгоритму работы децентрализованных бирж типа UniSwap, когда цена автоматически балансируется состоянием пула.";

	public static final String Amount = "количество";
	public static final String Adress = "адрес кошелька";

	public static final String DO_DEPOSIT_PFC = "Засылай PFC на следующий адрес";
	public static final String DO_DEPOSIT_DCR = "Засылай DCR на следующий адрес";

	public static final String UNRECOGNIZED_AMOUNT = "Количество монет не распознано";

	public static final String WasAboveLim = "была выше запрошенной";
	public static final String WasBelowLim = "была ниже запрошенной";

	public static final String FailedToSubmitorder = "Не удалось выставить ордер";

	public static final String NoEnoughCoinsForOrder = "Не хватает монет для ордера. Нужно";

	public static final String TO_SELL_PFC = "Купить PFC";
	public static final String TO_BUY_PFC = "Продать PFC";

	public static final String TRANSACTION_RECEIPT = "Чек операции";
	public static final String TRANSACTION_FAILED = "Не удалось записать транзакцию";

	public static final String COINS_WERE_SENT = "Монеты высланы на адрес";

	public static final String TO_EXECUTE_ORDER = "Для выполнения сделки по этой цене отправь следующую команду";

	public static final String UNRECOGNIZED_ADDRESS = "Неправильный адрес для вывода";

	static {
		mappings.put(UserSettingsLanguage.RU, Collections.newMap());
		mappings.put(UserSettingsLanguage.CH, Collections.newMap());
		mappings.put(UserSettingsLanguage.EN, Collections.newMap());
		final Map<String, String> ru = mappings.get(UserSettingsLanguage.RU);
		final Map<String, String> en = mappings.get(UserSettingsLanguage.EN);
		final Map<String, String> ch = mappings.get(UserSettingsLanguage.CH);

		ru.put(TO_SELL_PFC, TO_SELL_PFC);
		en.put(TO_SELL_PFC, "Command to sell PFC");
		ch.put(TO_SELL_PFC, "出售PFC的命令");

		ru.put(TO_BUY_PFC, TO_BUY_PFC);
		en.put(TO_BUY_PFC, "Command to buy PFC");
		ch.put(TO_BUY_PFC, "购买PFC的命令");

		ru.put(UNRECOGNIZED_ADDRESS, UNRECOGNIZED_ADDRESS);
		en.put(UNRECOGNIZED_ADDRESS, "Invalid address");
		ch.put(UNRECOGNIZED_ADDRESS, "无效地址");

		ru.put(TRANSACTION_FAILED, TRANSACTION_FAILED);
		en.put(TRANSACTION_FAILED, "Transaction failed");
		ch.put(TRANSACTION_FAILED, "交易失败");

		ru.put(TRANSACTION_RECEIPT, TRANSACTION_RECEIPT);
		en.put(TRANSACTION_RECEIPT, "Transaction receipt");
		ch.put(TRANSACTION_RECEIPT, "交易收据");

		ru.put(COINS_WERE_SENT, COINS_WERE_SENT);
		en.put(COINS_WERE_SENT, "The coins were sent to ");
		ch.put(COINS_WERE_SENT, "硬币被发送到");

		ru.put(TO_EXECUTE_ORDER, TO_EXECUTE_ORDER);
		en.put(TO_EXECUTE_ORDER, "To execute order send the following command");
		ch.put(TO_EXECUTE_ORDER, "要执行订单，请发送以下命令");

		ru.put(WasBelowLim, WasBelowLim);
		en.put(WasBelowLim, " whis is below the requested price");
		ch.put(WasBelowLim, "这低于要求的价格");

		ru.put(FailedToSubmitorder, FailedToSubmitorder);
		en.put(FailedToSubmitorder, "Failed to submit order");
		ch.put(FailedToSubmitorder, "这高于要求的价格");

		ru.put(FailedToSubmitorder, FailedToSubmitorder);
		en.put(FailedToSubmitorder, "Failed to submit order");
		ch.put(FailedToSubmitorder, "这高于要求的价格");

		ru.put(WasAboveLim, WasAboveLim);
		en.put(WasAboveLim, " whis is abolve the requested price");
		ch.put(WasAboveLim, "这高于要求的价格");

		ru.put(UniSwap, UniSwap);
		en.put(UniSwap,
			"Exchange rates are calculated using what is termed the “constant product formula” similar to the UniSwap decentralized exchange protocol.");
		ch.put(UniSwap, "常数乘积公式用于计算汇率。这类似于UniSwap分散式交换协议。");

		ru.put(ProceFor1PFCDrionExecution, ProceFor1PFCDrionExecution);
		en.put(ProceFor1PFCDrionExecution, "Price for 1PFC during order execution was");
		ch.put(ProceFor1PFCDrionExecution, "订单执行期间1PFC的价格为");

		ru.put(OrderWasNotExecuted, OrderWasNotExecuted);
		en.put(OrderWasNotExecuted, "Order was not executed");
		ch.put(OrderWasNotExecuted, "订单未执行");

		ru.put(POOL_STATE, POOL_STATE);
		en.put(POOL_STATE, "Trading pool state");
		ch.put(POOL_STATE, "交易池状态");

		ru.put(POOL_IS_BALANCING, POOL_IS_BALANCING);
		en.put(POOL_IS_BALANCING, "Pool is balancing...");
		ch.put(POOL_IS_BALANCING, "池正在等待余额...");

		ru.put(Unconfirmed, Unconfirmed);
		en.put(Unconfirmed, "Unconfirmed");
		ch.put(Unconfirmed, "等待交货");

		ru.put(Available, Available);
		en.put(Available, "Available");
		ch.put(Available, "可用资金");

		ru.put(Your_balances, Your_balances);
		en.put(Your_balances, "Your balances");
		ch.put(Your_balances, "您的帐户余额");

		ru.put(Example, Example);
		en.put(Example, "example");
		ch.put(Example, "例");

		ru.put(Adress, Adress);
		en.put(Adress, "wallet adress");
		ch.put(Adress, "钱包地址");

		ru.put(Amount, Amount);
		en.put(Amount, "amount");
		ch.put(Amount, "金额");

		ru.put(TO_WITHDRAW, TO_WITHDRAW);
		en.put(TO_WITHDRAW, "Use the following commands to withdraw funds");
		ch.put(TO_WITHDRAW, "使用以下命令提取资金");

		ru.put(Examples, Examples);
		en.put(Examples, "examples");
		ch.put(Examples, "例子");

		ru.put(TO_WITHDRAW_PFC, TO_WITHDRAW_PFC);
		en.put(TO_WITHDRAW_PFC, "Withdtaw PFC");
		ch.put(TO_WITHDRAW_PFC, "取PFC");

		ru.put(TO_WITHDRAW_DCR, TO_WITHDRAW_DCR);
		en.put(TO_WITHDRAW_DCR, "Withdraw DCR");
		ch.put(TO_WITHDRAW_DCR, "取DCR");

		ru.put(UNRECOGNIZED_AMOUNT, UNRECOGNIZED_AMOUNT);
		en.put(UNRECOGNIZED_AMOUNT, "Invalid amount");
		ch.put(UNRECOGNIZED_AMOUNT, "无效金额");

		ru.put(TO_DEPOSIT_PFC, TO_DEPOSIT_PFC);
		en.put(TO_DEPOSIT_PFC, "Top up PFC balance");
		ch.put(TO_DEPOSIT_PFC, "PFC充值余额");

		ru.put(TO_DEPOSIT_DCR, TO_DEPOSIT_DCR);
		en.put(TO_DEPOSIT_DCR, "Top up DCR balance");
		ch.put(TO_DEPOSIT_DCR, "DCR充值余额");

		ru.put(TO_DEPOSIT, TO_DEPOSIT);
		en.put(TO_DEPOSIT, "Commands for deposits");
		ch.put(TO_DEPOSIT, "这些是存入资金的命令");

		ru.put(POOL_LEAK, POOL_LEAK);
		en.put(POOL_LEAK, "Pool malfunction. Send the following error report to @JFixby");
		ch.put(POOL_LEAK, "事务池失败。请将错误报告转发给 @JFixby");

		ru.put(WITHDRAW, WITHDRAW);
		en.put(WITHDRAW, "withdraw coins");
		ch.put(WITHDRAW, "取钱");

		ru.put(DEPOSIT, DEPOSIT);
		en.put(DEPOSIT, "deposit coins");
		ch.put(DEPOSIT, "存入资金");

		ru.put(BALANCE, BALANCE);
		en.put(BALANCE, "check your ballances");
		ch.put(BALANCE, "查看钱包余额");

		ru.put(BUY_PFC, BUY_PFC);
		en.put(BUY_PFC, "buy coins");
		ch.put(BUY_PFC, "买硬币");

		ru.put(SELL_PFC, SELL_PFC);
		en.put(SELL_PFC, "sell coins");
		ch.put(SELL_PFC, "卖硬币");

		ru.put(WITHDRAW, WITHDRAW);
		en.put(WITHDRAW, "withdraw coins to your wallet");
		ch.put(WITHDRAW, "提款到钱包");

		ru.put(MARKET, MARKET);
		en.put(MARKET, "trading pool status");
		ch.put(MARKET, "交易池状态");

		ru.put(RESET_LANG, RESET_LANG);
		en.put(RESET_LANG, "change language");
		ch.put(RESET_LANG, "设定语言");

		ru.put(NO_BTC, NO_BTC);
		en.put(NO_BTC, "BTC trading is not available due to the high transaction fees ~$20-$50");
		ch.put(NO_BTC, "由于高昂的交易费用〜$ 20- $ 50，BTC交易无法进行");

		ru.put(PFC_WALLET, PFC_WALLET);
		en.put(PFC_WALLET, "You can download PFC-wallet here");
		ch.put(PFC_WALLET, "您可以在此处下载PFC钱包");

		ru.put(DCR_BINANCE, DCR_BINANCE);
		en.put(DCR_BINANCE, "You can sell and buy DCR on Binance");
		ch.put(DCR_BINANCE, "您可以在Binance上买卖DCR");

		ru.put(THIS_BOT, THIS_BOT);
		en.put(THIS_BOT, "This bot-exchange sells and buys picfight coins (PFC) for decreds (DCR)");
		ch.put(THIS_BOT, "这是一个交换机器人。它为DCR买卖PFC硬币。");

		ru.put(USE_THIS, USE_THIS);
		en.put(USE_THIS, "Use the following commands for trading:");
		ch.put(USE_THIS, "使用以下命令:");

	}

	public static String translate (final UserSettingsLanguage target, final String input) {
		final Map<String, String> dictionary = mappings.get(target);
		final String output = dictionary.get(input);
		if (output != null) {
			return output;
		}
		return input;
	}

}
