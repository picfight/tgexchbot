
package org.picfight.exchbot.lambda;

import java.util.ArrayList;

import org.picfight.exchbot.lambda.backend.Transaction;

public class TransactionStatus {

	public static final String ORDER_REGISTERED = "REGISTERED";
	public static final String ORDER_EXPIRED = "EXPIRED";

	public static final String NO_ENOUGH_BTC = "NO_ENOUGH_BTC";
	public static final String NO_ENOUGH_PFC = "NO_ENOUGH_PFC";

	public static final String BACKEND_ERROR = "BACKEND_ERROR";
	public static final String EXECUTED = "EXECUTED";

	public Transaction transact;

	public ArrayList<Operation> operations = new ArrayList<>();

}
