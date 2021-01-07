
package org.picfight.exchbot.lambda;

import org.picfight.exchbot.lambda.backend.AmountDCR;
import org.picfight.exchbot.lambda.backend.AmountPFC;

public class Result {

	public Boolean Success;
	public String ErrorMessage;

	public String DCR_FromAccountAddress;
	public String DCR_ResolvedAccountName;
	public AmountDCR DCR_Amount;
	public String DCR_ToAddress;
	public String DCR_TransactionReceipt;

	public String PFC_FromAccountAddress;
	public String PFC_ResolvedAccountName;
	public AmountPFC PFC_Amount;
	public String PFC_ToAddress;
	public String PFC_TransactionReceipt;

	@Override
	public String toString () {
		return "Result [Success=" + this.Success + ", ErrorMessage=" + this.ErrorMessage + ", DCR_FromAccountAddress="
			+ this.DCR_FromAccountAddress + ", DCR_ResolvedAccountName=" + this.DCR_ResolvedAccountName + ", DCR_Amount="
			+ this.DCR_Amount + ", DCR_ToAddress=" + this.DCR_ToAddress + ", DCR_TransactionReceipt=" + this.DCR_TransactionReceipt
			+ ", PFC_FromAccountAddress=" + this.PFC_FromAccountAddress + ", PFC_ResolvedAccountName=" + this.PFC_ResolvedAccountName
			+ ", PFC_Amount=" + this.PFC_Amount + ", PFC_ToAddress=" + this.PFC_ToAddress + ", PFC_TransactionReceipt="
			+ this.PFC_TransactionReceipt + "]";
	}

}
