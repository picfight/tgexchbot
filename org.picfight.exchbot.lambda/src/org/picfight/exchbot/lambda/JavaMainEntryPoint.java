
package org.picfight.exchbot.lambda;

import java.io.IOException;

import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.sys.settings.ExecutionMode;
import com.jfixby.scarabei.api.sys.settings.SystemSettings;
import com.jfixby.scarabei.red.desktop.ScarabeiDesktop;

public class JavaMainEntryPoint {
	static {
		ScarabeiDesktop.deploy();
	}

	public static void main (final String[] args) throws IOException {
		final ChatRequestResponse requesthandler = new ChatRequestResponse();
		requesthandler.input = new TelegramUpdate();

// final VKAlbumsList albumsbums = VKClient.listAlbums();

// L.d("albumsbums", albumsbums);

// final VKAlbumContent album = VKClient.getAlbum(244963407L, null);

// L.d("album", album);
// Sys.exit();
// https://vk.com/picfight?z=album-62111632_267182269
		requesthandler.input.message.text = "/walletcheck https://vk.com/jfixbi";
// requesthandler.input.message.text = "/vkalbums";
		if (ChatBotActionHandler.handler == null) {
			ChatBotActionHandler.handler = new ChatBotActionHandler();
			SystemSettings.setExecutionMode(ExecutionMode.EARLY_DEVELOPMENT);
		}

		ChatBotActionHandler.handler.handleRequest(requesthandler);

		L.d("requesthandler", requesthandler);
	}

}
