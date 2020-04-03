
package org.picfight.exchbot.lambda;

import java.util.function.Function;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TelegramBotSpecs {
	public String token;
	public String botusername;
	public String path;
	public Function<Update, BotApiMethod> onUpdate;
}
