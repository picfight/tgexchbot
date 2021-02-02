
package org.picfight.exchbot.lambda.backend;

import org.picfight.exchbot.lambda.TransactionResult;

import com.jfixby.scarabei.api.json.Json;

public class TradeResult {

	public boolean Success;
// Success bool `json: "Success"`

	public boolean NoEnoughFunds;
// Success bool `json: "NoEnoughFunds"`

	public boolean PriceNotMet;
// Success bool `json: "PriceNotMet"`

	public String ErrorMessage;
// ErrorMessage string `json: "ErrorMessage"`

	public String Operation;
// Operation string `json: "Operation"`

	public boolean Executed;
// Executed bool `json: "Executed"`

	public double BTCPFC_Ratio_BeforeTrade;
// BTCPFC_Ratio_BeforeTrade float64 `json: "BTCPFC_Ratio_BeforeTrade"`

	public double BTCPFC_Ratio_AfterTrade;
// BTCPFC_Ratio_AfterTrade float64 `json: "BTCPFC_Ratio_AfterTrade"`

	public AmountBTC BTC_InPool_BeforeTrade;
// BTC_InPool_BeforeTrade float64 `json: "BTC_InPool_BeforeTrade"`

	public AmountPFC PFC_InPool_BeforeTrade;
// PFC_InPool_BeforeTrade float64 `json: "PFC_InPool_BeforeTrade"`

	public double PoolConstant;
// PoolConstant float64 `json: "PoolConstant"`

	public AmountPFC PFC_InPool_AfterTrade;
// PFC_InPool_AfterTrade float64 `json: "PFC_InPool_AfterTrade"`

	public AmountBTC BTC_InPool_AfterTrade;
// BTC_InPool_AfterTrade float64 `json: "BTC_InPool_AfterTrade"`

	public AmountBTC BTC_Fee;
// BTC_Fee AmountBTC `json: "BTC_Fee"`

	public AmountBTC BTC_Executed_Amount;
// BTC_Executed_Amount float64 `json: "BTC_Executed_Amount"`

	public AmountPFC PFC_Executed_Amount;
// PFC_Executed_Amount float64 `json: "PFC_Executed_Amount"`

	public double BTCPFC_Executed_Price;
// BTCPFC_Executed_Price float64 `json: "BTCPFC_Executed_Price"`

	public TransactionResult BTC_Transaction;
// BTC_Transaction TransactionResult `json: "BTC_Transaction"`

	public TransactionResult PFC_Transaction;
// PFC_Transaction TransactionResult `json: "PFC_Transaction"`

	public double Requested_Price_Btc_for_1_pfc;
// Requested_Price_Btc_for_1_pfc float64 `json: "Requested_Price_Btc_for_1_pfc"`

	public boolean UnfinishedTransaction;
// UnfinishedTransaction bool `json: "UnfinishedTransaction"`

	public boolean MinBTCAmountError;
// MinBTCAmountError bool `json: "MinBTCAmountError"`

	public AmountBTC MinBTCAmountValue;
// MinBTCAmountValue AmountBTC `json: "MinBTCAmountValue"`

	@Override
	public String toString () {
		return Json.serializeToString(this).toString();
	}
	// TransactionResult

}
