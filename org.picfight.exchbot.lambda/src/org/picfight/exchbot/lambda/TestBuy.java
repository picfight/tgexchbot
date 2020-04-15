
package org.picfight.exchbot.lambda;

import java.io.IOException;

import com.jfixby.scarabei.api.sys.settings.ExecutionMode;
import com.jfixby.scarabei.api.sys.settings.SystemSettings;

public class TestBuy {

	public static void main (final String[] args) throws IOException {
		LambdaEntryPoint.init();

		final ChatRequestResponse requesthandler = new ChatRequestResponse();
		final FilesystemSetup filesystem = LambdaEntryPoint.FS();
		requesthandler.input = new TelegramUpdate();
		requesthandler.debug_mode = true;

		requesthandler.input.message.text = "/buy_pfc JsFeTx96cSC2MtyBbDV1JV1m6mXwS8h8zhC";
		requesthandler.input.message.chatID = -1L;

		ChatBotActionHandler.handler = new ChatBotActionHandler(requesthandler.debug_mode);
		SystemSettings.setExecutionMode(ExecutionMode.RELEASE_CANDIDATE);

		ChatBotActionHandler.handler.handleRequest(requesthandler, filesystem);

	}

}
