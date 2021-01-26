
package org.picfight.exchbot.lambda;

import org.picfight.exchbot.lambda.backend.AmountBTC;
import org.picfight.exchbot.lambda.backend.AmountPFC;

public class TransactionResult {

	public Boolean Success;
	public String ErrorMessage;

	public String BTC_FromAccountAddress;
	public String BTC_ResolvedAccountName;
	public AmountBTC BTC_Amount;
	public String BTC_ToAddress;
	public String BTC_TransactionReceipt;

	public String PFC_FromAccountAddress;
	public String PFC_ResolvedAccountName;
	public AmountPFC PFC_Amount;
	public String PFC_ToAddress;
	public String PFC_TransactionReceipt;

	@Override
	public String toString () {
		return "Result [Success=" + this.Success + ", ErrorMessage=" + this.ErrorMessage + ", BTC_FromAccountAddress="
			+ this.BTC_FromAccountAddress + ", BTC_ResolvedAccountName=" + this.BTC_ResolvedAccountName + ", BTC_Amount="
			+ this.BTC_Amount + ", BTC_ToAddress=" + this.BTC_ToAddress + ", BTC_TransactionReceipt=" + this.BTC_TransactionReceipt
			+ ", PFC_FromAccountAddress=" + this.PFC_FromAccountAddress + ", PFC_ResolvedAccountName=" + this.PFC_ResolvedAccountName
			+ ", PFC_Amount=" + this.PFC_Amount + ", PFC_ToAddress=" + this.PFC_ToAddress + ", PFC_TransactionReceipt="
			+ this.PFC_TransactionReceipt + "]";
	}

}
