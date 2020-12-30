
package org.picfight.exchbot.lambda;

public class TelegramMessage {
// from=User{id=136289559, firstName='J', isBot=false, lastName='🐸', userName='JFixbi', languageCode='en'},

	public boolean hasText = false;
	public String text = "";
	public Long chatID = 0L;
	public User from = new User();

	@Override
	public String toString () {
		return "TelegramMessage [hasText=" + this.hasText + ", text=" + this.text + ", chatID=" + this.chatID + ", from="
			+ this.from + "]";
	}

}
