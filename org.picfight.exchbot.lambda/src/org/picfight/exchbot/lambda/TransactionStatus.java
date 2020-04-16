
package org.picfight.exchbot.lambda;

import org.picfight.exchbot.lambda.backend.Transaction;

public class TransactionStatus {

	public static final String ORDER_REGISTERED = "REGISTERED";

	public static final String ORDER_STUCK = "STUCK";

	public static final String ORDER_EXPIRED = "EXPIRED";

	public Transaction transact;

	public String status;

	public String error_message;

}
