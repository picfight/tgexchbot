
package org.picfight.exchbot.lambda;

import java.io.IOException;

import com.jfixby.scarabei.api.sys.settings.ExecutionMode;
import com.jfixby.scarabei.api.sys.settings.SystemSettings;

public class TestSell {

	public static void main (final String[] args) throws IOException {
		LambdaEntryPoint.init();

		final ChatRequestResponse requesthandler = new ChatRequestResponse();
		final FilesystemSetup filesystem = LambdaEntryPoint.FS();
		requesthandler.input = new TelegramUpdate();
		requesthandler.debug_mode = true;

		requesthandler.input.message.text = "/sell_pfc 1KsJZdKiEu2BgGarBGtRPt2wqGPSoQtUfF";
		requesthandler.input.message.chatID = -1L;

		ChatBotActionHandler.handler = new ChatBotActionHandler(requesthandler.debug_mode);
		SystemSettings.setExecutionMode(ExecutionMode.RELEASE_CANDIDATE);

		ChatBotActionHandler.handler.handleRequest(requesthandler, filesystem);

	}

}
