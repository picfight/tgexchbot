
package org.picfight.exchbot.lambda;

import java.util.ArrayList;

import org.picfight.exchbot.lambda.backend.Operation;

public class Transaction {

	public Operation operation;

	public ArrayList<Status> states = new ArrayList<>();

}
