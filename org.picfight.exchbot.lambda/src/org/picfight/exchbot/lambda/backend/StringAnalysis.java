
package org.picfight.exchbot.lambda.backend;

import com.jfixby.scarabei.api.json.Json;

public class StringAnalysis {

	public BTCAddress BTCAddress = null;
	public PFCAddress PFCAddress = null;
	public DCRAddress DCRAddress = null;
	public String Error = null;

	@Override
	public String toString () {
		return Json.serializeToString(this) + "";
	}

}
