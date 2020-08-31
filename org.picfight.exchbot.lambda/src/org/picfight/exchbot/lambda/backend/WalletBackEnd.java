
package org.picfight.exchbot.lambda.backend;

import java.io.IOException;

import org.picfight.exchbot.back.BackEndConnector;
import org.picfight.exchbot.lambda.Result;

import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.collections.Map;
import com.jfixby.scarabei.api.json.Json;
import com.jfixby.scarabei.api.json.JsonString;
import com.jfixby.scarabei.api.net.http.Http;
import com.jfixby.scarabei.api.net.http.HttpURL;

public class WalletBackEnd {

	final public String host;
	final public int port;
	final public String access_key;

	public WalletBackEnd (final WalletBackEndArgs args) {
		this.host = args.host;
		this.port = args.port;
		this.access_key = args.access_key;
	}

	public AvailableFunds getFunds () throws IOException {
		final String command = "rate";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("access_key", this.access_key);
		final JsonString resultJson = BackEndConnector.retrieve(Url, params);
		final AvailableFunds r = Json.deserializeFromString(AvailableFunds.class, resultJson);
		return r;
	}

	private HttpURL commadToUrl (final String command) {
		final HttpURL url = Http.newURL(this.host + ":" + this.port + "/" + command);
		return url;
	}

	public StringAnalysis analyzeString (final String text) throws IOException {
		final String command = "analyze_string";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("access_key", this.access_key);
		params.put("raw_text", text);
		final JsonString resultJson = BackEndConnector.retrieve(Url, params);
		final StringAnalysis r = Json.deserializeFromString(StringAnalysis.class, resultJson);
		return r;
	}

	private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

	public static String bytesToHex (final byte[] bytes) {
		final char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			final int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars).toLowerCase();
	}

	public PFCBalance getPFCBallance (final PFCAddress pfc_address, final String accountName) throws IOException {
		final String command = "get_balance_pfc";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("access_key", this.access_key);
		params.put("pfc_address", pfc_address.AddressString);
		params.put("Account_name", accountName);
		final JsonString resultJson = BackEndConnector.retrieve(Url, params);
		final PFCBalance r = Json.deserializeFromString(PFCBalance.class, resultJson);
		return r;
	}

	public BTCBalance getBTCBallance (final BTCAddress btc_address, final String accountName) throws IOException {
		final String command = "get_balance_btc";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("access_key", this.access_key);
		params.put("btc_address", btc_address.AddressString);
		params.put("Account_name", accountName);
		final JsonString resultJson = BackEndConnector.retrieve(Url, params);
		final BTCBalance r = Json.deserializeFromString(BTCBalance.class, resultJson);
		return r;
	}

// public BTCBalance totalBTCReceivedForAddress (final BTCAddress address, final String accountName) throws IOException {

// }
//
// public PFCBalance totalPFCReceivedForAddress (final PFCAddress address, final String accountName) throws IOException {

// }

	public Result transferPFC (final Operation t, final AmountPFC pfcAmount) throws IOException {
		final String command = "transfer_pfc";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("access_key", this.access_key + "");
		params.put("client_pfc_wallet", t.clientPFCWallet.AddressString + "");
		params.put("pfc_amount", pfcAmount + "");
		final JsonString resultJson = BackEndConnector.retrieve(Url, params);
		final Result r = Json.deserializeFromString(Result.class, resultJson);
		return r;
	}

	public Result transferBTC (final Operation t, final AmountBTC btcAmount) throws IOException {
		final String command = "transfer_btc";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("access_key", this.access_key + "");
		params.put("client_btc_wallet", t.clientPFCWallet.AddressString + "");
		params.put("btc_amount", btcAmount.Value + "");
		final JsonString resultJson = BackEndConnector.retrieve(Url, params);
		final Result r = Json.deserializeFromString(Result.class, resultJson);
		return r;
	}

	public BTCInputs listBTCInputs (final BTCAddress address, final String accountName) throws IOException {
		final String command = "list_btc_inputs";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("Access_key", this.access_key + "");
		params.put("Btc_address", address.AddressString + "");
		params.put("Account_name", accountName);
		final JsonString resultJson = BackEndConnector.retrieve(Url, params);
		final BTCInputs r = Json.deserializeFromString(BTCInputs.class, resultJson);
		return r;
	}

	public PFCInputs listPFCInputs (final PFCAddress address, final String accountName) throws IOException {
		final String command = "list_pfc_inputs";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("Access_key", this.access_key + "");
		params.put("Pfc_address", address.AddressString + "");
		params.put("Account_name", accountName);
		final JsonString resultJson = BackEndConnector.retrieve(Url, params);
		final PFCInputs r = Json.deserializeFromString(PFCInputs.class, resultJson);
		return r;
	}

	public BTCAddress getNewBTCAddress (final String accountName) throws IOException {
		final String command = "new_btc_address";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("Access_key", this.access_key);
		params.put("Account_name", accountName);
		final JsonString resultJson = BackEndConnector.retrieve(Url, params);
		final BTCAddress r = Json.deserializeFromString(BTCAddress.class, resultJson);
		return r;
	}

	public PFCAddress getNewPFCAddress (final String accountName) throws IOException {
		final String command = "new_pfc_address";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("Access_key", this.access_key);
		params.put("Account_name", accountName);
		final JsonString resultJson = BackEndConnector.retrieve(Url, params);
		final PFCAddress r = Json.deserializeFromString(PFCAddress.class, resultJson);
		return r;
	}

// public BTCAddress obtainNewBTCAddress (final UserSettings settings) throws IOException {
// final File folder = settings.userFolder();
// final File poolFile = folder.child("btc.address.pool.json");
// final BTCAddressPool pool = poolFile.readJson(BTCAddressPool.class);
// while (true) {
// final BTCAddress address = pool.obtainNewAddress(this, settings.getAccountName());
// final BTCInputs inputs = this.listBTCInputs(address, settings.getAccountName());
// if (inputs.size() == 0) {
// return address;
// }
// }
// }
//
// public PFCAddress obtainNewPFCAddress (final UserSettings settings) throws IOException {
// final File folder = settings.userFolder();
// final File poolFile = folder.child("pfc.address.pool.json");
// final PFCAddressPool pool = poolFile.readJson(PFCAddressPool.class);
// while (true) {
// final PFCAddress address = pool.obtainNewAddress(this, settings.getAccountName());
// final PFCInputs inputs = this.listPFCInputs(address, settings.getAccountName());
// if (inputs.size() == 0) {
// return address;
// }
// }
// }

}
