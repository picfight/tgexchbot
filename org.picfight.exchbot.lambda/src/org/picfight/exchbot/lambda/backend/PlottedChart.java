
package org.picfight.exchbot.lambda.backend;

import com.jfixby.scarabei.api.json.Json;

public class PlottedChart {
// ImageBase64 string`json:"ImageBase64"`
// Error string`json:"Error"`
// Success bool`json:"Success"`

	public String ImageBase64;
	public String Error;
	public boolean Success;

	@Override
	public String toString () {
		return Json.serializeToString(this).toString();
	}

}
