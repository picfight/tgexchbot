
package org.picfight.exchbot.lambda;

import org.picfight.exchbot.lambda.backend.AmountBTC;
import org.picfight.exchbot.lambda.backend.AmountPFC;

public class Result {

	public Boolean Success;
	public String Error_message;
	public String Btc_transaction_receipt;
	public String Pfc_transaction_receipt;

	public AmountBTC BtcAmount;
	public AmountPFC PfcAmount;

	@Override
	public String toString () {
		return "Result [Success=" + this.Success + ", Error_message=" + this.Error_message + ", Btc_transaction_receipt="
			+ this.Btc_transaction_receipt + ", Pfc_transaction_receipt=" + this.Pfc_transaction_receipt + ", BtcAmount="
			+ this.BtcAmount + ", PfcAmount=" + this.PfcAmount + "]";
	}

}
