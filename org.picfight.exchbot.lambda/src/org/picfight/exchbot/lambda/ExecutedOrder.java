
package org.picfight.exchbot.lambda;

import org.picfight.exchbot.lambda.backend.TradeResult;

public class ExecutedOrder {

	public long timestamp;
	public String date;
	public double price;
	public double size;

	public TradeResult receipt;

}
