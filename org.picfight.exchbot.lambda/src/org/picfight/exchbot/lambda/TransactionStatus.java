
package org.picfight.exchbot.lambda;

import org.picfight.exchbot.lambda.backend.Transaction;

public class TransactionStatus {

	public static final String ORDER_REGISTERED = "ORDER_REGISTERED";

	public Transaction transact;

	public String status;

}
