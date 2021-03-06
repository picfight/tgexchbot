
package org.picfight.exchbot.lambda.backend;

import java.io.IOException;

import org.picfight.exchbot.lambda.User;
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
		if (L == null) {
			this.data.language = null;
		} else {
			this.data.language = L + "";
		}
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
		if (this.data.exchangeAddress.get("pfc") == null) {
			return false;
		}
		if (this.data.exchangeAddress.get("dcr") == null) {
			return false;
		}
		if ("".equals(this.data.exchangeAddress.get("pfc"))) {
			return false;
		}
		if ("".equals(this.data.exchangeAddress.get("dcr"))) {
			return false;
		}
		return true;
	}

//
	public void setupExchangeAddress (final WalletBackEnd walletBackEnd) throws BackendException, IOException {
		final String userID = this.data.accountName;
		if (this.data.exchangeAddress.get("dcr") == null || "".equals(this.data.exchangeAddress.get("dcr"))) {
			final DCRAddress add = walletBackEnd.getNewDCRAddress(userID);
			this.data.exchangeAddress.put("dcr", add.AddressString);
		}
		if (this.data.exchangeAddress.get("pfc") == null || "".equals(this.data.exchangeAddress.get("dcr"))) {
			final PFCAddress add = walletBackEnd.getNewPFCAddress(userID);
			this.data.exchangeAddress.put("pfc", add.AddressString);
		}
		this.settingsFile.writeJson(this.data);
	}

	public DCRAddress getExchangeAddressDCR () {
		final String string = this.data.exchangeAddress.get("dcr");
		Debug.checkNull("AddressString", string);
		Debug.checkEmpty("AddressString", string);
		final DCRAddress a = new DCRAddress();
		a.AddressString = string;
		a.Type = "DCR";
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

	public UserSettingsLanguage getLanguage () {
		return UserSettingsLanguage.resolve(this.data.language);
	}

	public void setUser (final Long chatid, final User from) {
		this.data.chatid = chatid;
		this.data.firstName = from.firstName;
		this.data.lastName = from.lastName;
		this.data.userName = from.userName;
	}

}
