
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

}
