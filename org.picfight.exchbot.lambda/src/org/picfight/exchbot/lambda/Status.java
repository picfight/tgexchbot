
package org.picfight.exchbot.lambda;

import org.picfight.exchbot.lambda.backend.AmountBTC;
import org.picfight.exchbot.lambda.backend.AmountPFC;

public class Status {
	public String status;
	public String error_message;
	public TransactionResult result;

	public AmountPFC processingPfcAmount;
	public AmountBTC processingBtcAmount;

}
