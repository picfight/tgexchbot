
package org.picfight.exchbot.lambda;

import com.jfixby.scarabei.api.json.Json;

public class ChatRequestResponse {

	public ChatRequestResponse () {
		this.build = "10001";
	}

	public TelegramUpdate input;
	public Object output;

	public String build;
	public boolean debug_mode;

	@Override
	public String toString () {
		return Json.serializeToString(this).toString();
	}

}
