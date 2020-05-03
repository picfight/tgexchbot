
package org.picfight.exchbot.lambda.backend;

import java.io.IOException;

import org.picfight.exchbot.lambda.UserSettingsLanguage;

import com.jfixby.scarabei.api.debug.Debug;
import com.jfixby.scarabei.api.file.File;

public class UserSettings {

	private final File settingsFile;

	public UserSettings (final File settingsFile) throws IOException {
		this.settingsFile = settingsFile;
		if (!settingsFile.exists()) {
			final SrlzUserSettings set = new SrlzUserSettings();
			settingsFile.writeJson(set);
		}
	}

	SrlzUserSettings data = new SrlzUserSettings();

	public boolean languageIsSet () {
		return this.data.language != null;
	}

	public void setLanguage (final UserSettingsLanguage L) throws IOException {
		this.data.language = L + "";
		this.settingsFile.writeJson(this.data);
	}

	public void readFromFile () throws IOException {
		final SrlzUserSettings set = this.settingsFile.readJson(SrlzUserSettings.class);
		this.data = set;
	}

	public boolean exchangeAddressIsSet () {
		if (this.data.exchangeAddress.get("btc") == null) {
			return false;
		}
		if (this.data.exchangeAddress.get("pfc") == null) {
			return false;
		}
		return true;
	}

	public void setupExchangeAddress (final WalletBackEnd walletBackEnd) throws IOException {
		if (this.data.exchangeAddress.get("btc") == null) {
			final BTCAddress add = walletBackEnd.obtainNewBTCAddress();
			this.data.exchangeAddress.put("btc", add.AddressString);
		}
		if (this.data.exchangeAddress.get("pfc") == null) {
			final PFCAddress add = walletBackEnd.obtainNewPFCAddress();
			this.data.exchangeAddress.put("pfc", add.AddressString);
		}
		this.settingsFile.writeJson(this.data);
	}

	public BTCAddress getExchangeAddressBTC () {
		final String string = this.data.exchangeAddress.get("btc");
		Debug.checkNull("AddressString", string);
		Debug.checkEmpty("AddressString", string);
		final BTCAddress a = new BTCAddress();
		a.AddressString = string;
		a.Type = "BTC";
		return a;
	}

	public PFCAddress getExchangeAddressPFC () {
		final String string = this.data.exchangeAddress.get("pfc");
		Debug.checkNull("AddressString", string);
		Debug.checkEmpty("AddressString", string);
		final PFCAddress a = new PFCAddress();
		a.AddressString = string;
		a.Type = "PFC";
		return a;
	}

}
