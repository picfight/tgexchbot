
package org.picfight.exchbot.lambda.backend;

import java.io.IOException;

import org.picfight.exchbot.lambda.UserSettingsLanguage;

import com.jfixby.scarabei.api.debug.Debug;
import com.jfixby.scarabei.api.file.File;

public class UserSettings {

	private final File settingsFile;

	public UserSettings (final File settingsFile) throws IOException {
		this.settingsFile = settingsFile;
// if (!settingsFile.exists()) {
// final SrlzUserSettings set = new SrlzUserSettings();
// settingsFile.writeJson(set);
// }
	}

	public String getAccountName () {
		return this.data.accountName;
	}

	SrlzUserSettings data = new SrlzUserSettings();

	public boolean languageIsSet () {
		return this.data.language != null;
	}

	public void setLanguage (final UserSettingsLanguage L) throws IOException {
		this.data.language = L + "";
		this.writeFile();
	}

	public void readFromFile () throws IOException {
		final SrlzUserSettings set = this.settingsFile.readJson(SrlzUserSettings.class);
		this.data = set;
	}

	public void writeFile () throws IOException {
		this.settingsFile.writeJson(this.data);
	}

	public File userFolder () {
		return this.settingsFile.parent();
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

//
	public void setupExchangeAddress (final WalletBackEnd walletBackEnd, final String userID) throws IOException {
		if (this.data.exchangeAddress.get("btc") == null) {
			final BTCAddress add = walletBackEnd.getNewBTCAddress(userID);
			this.data.exchangeAddress.put("btc", add.AddressString);
		}
		if (this.data.exchangeAddress.get("pfc") == null) {
			final PFCAddress add = walletBackEnd.getNewPFCAddress(userID);
			this.data.exchangeAddress.put("pfc", add.AddressString);
		}
		this.settingsFile.writeJson(this.data);
	}

//
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

	public BTCAddress getPrivateAddressBTC () {
		final String string = this.data.privateAddress.get("btc");
		if (string == null) {
			return null;
		}
// Debug.checkNull("AddressString", string);
// Debug.checkEmpty("AddressString", string);
		final BTCAddress a = new BTCAddress();
		a.AddressString = string;
		a.Type = "BTC";
		return a;
	}

	public PFCAddress getPrivateAddressPFC () {
		final String string = this.data.privateAddress.get("pfc");
		if (string == null) {
			return null;
		}
// Debug.checkNull("AddressString", string);
// Debug.checkEmpty("AddressString", string);
		final PFCAddress a = new PFCAddress();
		a.AddressString = string;
		a.Type = "PFC";
		return a;
	}

	public void setupPrivateAddressBTC (final BTCAddress address) throws IOException {
		if (this.data.privateAddress.get("btc") == null) {
			this.data.privateAddress.put("btc", address.AddressString);
		}
		this.settingsFile.writeJson(this.data);
	}

	public void setupPrivateAddressPFC (final PFCAddress address) throws IOException {
		if (this.data.privateAddress.get("pfc") == null) {
			this.data.privateAddress.put("pfc", address.AddressString);
		}
		this.settingsFile.writeJson(this.data);
	}

	public boolean privateBTCAddressIsSet () {
		if (this.data.privateAddress.get("btc") == null) {
			return false;
		}
// if (this.data.exchangeAddress.get("pfc") == null) {
// return false;
// }
		return true;
	}

	public boolean privatePFCAddressIsSet () {
		if (this.data.privateAddress.get("pfc") == null) {
			return false;
		}
// if (this.data.exchangeAddress.get("pfc") == null) {
// return false;
// }
		return true;
	}

}
