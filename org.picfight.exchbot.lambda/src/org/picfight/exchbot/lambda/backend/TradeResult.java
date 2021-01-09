
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

	public double DCRPFC_Ratio_BeforeTrade;
// DCRPFC_Ratio_BeforeTrade float64 `json: "DCRPFC_Ratio_BeforeTrade"`

	public double DCRPFC_Ratio_AfterTrade;
// DCRPFC_Ratio_AfterTrade float64 `json: "DCRPFC_Ratio_AfterTrade"`

	public AmountDCR DCR_InPool_BeforeTrade;
// DCR_InPool_BeforeTrade float64 `json: "DCR_InPool_BeforeTrade"`

	public AmountPFC PFC_InPool_BeforeTrade;
// PFC_InPool_BeforeTrade float64 `json: "PFC_InPool_BeforeTrade"`

	public double PoolConstant;
// PoolConstant float64 `json: "PoolConstant"`

	public AmountPFC PFC_InPool_AfterTrade;
// PFC_InPool_AfterTrade float64 `json: "PFC_InPool_AfterTrade"`

	public AmountDCR DCR_InPool_AfterTrade;
// DCR_InPool_AfterTrade float64 `json: "DCR_InPool_AfterTrade"`

	public AmountDCR DCR_Executed_Amount;
// DCR_Executed_Amount float64 `json: "DCR_Executed_Amount"`

	public AmountPFC PFC_Executed_Amount;
// PFC_Executed_Amount float64 `json: "PFC_Executed_Amount"`

	public double DCRPFC_Executed_Price;
// DCRPFC_Executed_Price float64 `json: "DCRPFC_Executed_Price"`

	public TransactionResult DCR_Transaction;
// DCR_Transaction TransactionResult `json: "DCR_Transaction"`

	public TransactionResult PFC_Transaction;
// PFC_Transaction TransactionResult `json: "PFC_Transaction"`

	public double Requested_Price_Dcr_for_1_pfc;
// Requested_Price_Dcr_for_1_pfc float64 `json: "Requested_Price_Dcr_for_1_pfc"`

	public boolean UnfinishedTransaction;
// UnfinishedTransaction bool `json: "UnfinishedTransaction"`

	@Override
	public String toString () {
		return Json.serializeToString(this).toString();
	}
	// TransactionResult

}
