
package org.picfight.exchbot.lambda.backend;

import com.jfixby.scarabei.api.json.Json;

public class TradeResult {

	public boolean Success;
	public String ErrorMessage;
	public String Operation;
	public boolean GetQuote;
	public double DCRPFC_Price_BeforeTrade;
	public double DCRPFC_Price_AfterTrade;
	public double DCR_InPool_BeforeTrade;
	public double PFC_InPool_BeforeTrade;
	public double PoolConstant;
	public double PFC_InPool_AfterTrade;
	public double DCR_InPool_AfterTrade;
	public double DCR_Executed_Amount;
	public double PFC_Executed_Amount;
	public double DCRPFC_Executed_Price;

	@Override
	public String toString () {
		return Json.serializeToString(this).toString();
	}

}
