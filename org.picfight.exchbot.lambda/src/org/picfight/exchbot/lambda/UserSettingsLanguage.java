
package org.picfight.exchbot.lambda;

public enum UserSettingsLanguage {
	CH, RU, EN;

	public static UserSettingsLanguage resolve (final String language) {
		if (CH.toString().equals(language)) {
			return CH;
		}
		if (RU.toString().equals(language)) {
			return RU;
		}
		if (EN.toString().equals(language)) {
			return EN;
		}
		return null;
	}

}
