
package org.picfight.exchbot.lambda.backend;

import java.io.IOException;

import org.picfight.exchbot.back.BackEndConnector;
import org.picfight.exchbot.lambda.TRADE_OPERATION;
import org.picfight.exchbot.lambda.TransactionResult;

import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.collections.Map;
import com.jfixby.scarabei.api.err.Err;
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

	public AvailableFunds getFunds () throws BackendException {
		final String command = "rate";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("access_key", this.access_key);
		JsonString resultJson;
		try {
			resultJson = BackEndConnector.retrieve(Url, params);
		} catch (final IOException e) {
			e.printStackTrace();
			throw new BackendException(e);
		}
		final AvailableFunds r = Json.deserializeFromString(AvailableFunds.class, resultJson);
		return r;
	}

	private HttpURL commadToUrl (final String command) {
		final HttpURL url = Http.newURL(this.host + ":" + this.port + "/" + command);
		return url;
	}

	public StringAnalysis analyzeString (final String text) throws BackendException {
		final String command = "analyze_string";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("access_key", this.access_key);
		params.put("raw_text", text);
		JsonString resultJson;
		try {
			resultJson = BackEndConnector.retrieve(Url, params);
		} catch (final IOException e) {
			e.printStackTrace();
			throw new BackendException(e);
		}
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

	public PFCBalance getPFCBallance (final PFCAddress pfc_address, final int confirmations) throws BackendException {
		final String command = "get_balance_pfc";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("access_key", this.access_key);
		params.put("pfc_address", pfc_address.AddressString);
		params.put("min_confirmations", confirmations + "");
		JsonString resultJson;
		try {
			resultJson = BackEndConnector.retrieve(Url, params);
		} catch (final IOException e) {
			e.printStackTrace();
			throw new BackendException(e);
		}
		final PFCBalance r = Json.deserializeFromString(PFCBalance.class, resultJson);
		return r;
	}

	public DCRBalance getDCRBallance (final DCRAddress dcr_address, final int confirmations) throws BackendException {
		final String command = "get_balance_dcr";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("Access_key", this.access_key);
		params.put("Dcr_address", dcr_address.AddressString);
		params.put("Min_confirmations", confirmations + "");
		JsonString resultJson;
		try {
			resultJson = BackEndConnector.retrieve(Url, params);
		} catch (final IOException e) {
			e.printStackTrace();
			throw new BackendException(e);
		}
		final DCRBalance r = Json.deserializeFromString(DCRBalance.class, resultJson);
		return r;
	}

	public BTCBalance getBTCBallance (final BTCAddress btc_address, final int confirmations) throws BackendException {
		final String command = "get_balance_btc";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("access_key", this.access_key);
		params.put("btc_address", btc_address.AddressString);
		params.put("min_confirmations", confirmations + "");
		JsonString resultJson;
		try {
			resultJson = BackEndConnector.retrieve(Url, params);
		} catch (final IOException e) {
			e.printStackTrace();
			throw new BackendException(e);
		}
		final BTCBalance r = Json.deserializeFromString(BTCBalance.class, resultJson);
		return r;
	}

	public TransactionResult transferPFC (final PFCAddress fromAccountAddress, final PFCAddress toAddress, final AmountPFC amount)
		throws BackendException {
		final String command = "transfer_pfc";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("Access_key", this.access_key + "");
		params.put("Pfc_fromaccountaddress", fromAccountAddress + "");
		params.put("Pfc_toaddress", toAddress + "");
		params.put("Pfc_amount", amount.Value + "");

		JsonString resultJson;
		try {
			resultJson = BackEndConnector.retrieve(Url, params);
		} catch (final IOException e) {
			e.printStackTrace();
			throw new BackendException(e);
		}
		final TransactionResult r = Json.deserializeFromString(TransactionResult.class, resultJson);
		return r;

	}

	public TransactionResult transferDCR (final DCRAddress fromAccountAddress, final DCRAddress toAddress, final AmountDCR amount)
		throws BackendException {
		final String command = "transfer_dcr";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("Access_key", this.access_key + "");
		params.put("Dcr_fromaccountaddress", fromAccountAddress + "");
		params.put("Dcr_toaddress", toAddress + "");
		params.put("Dcr_amount", amount.Value + "");

		JsonString resultJson;
		try {
			resultJson = BackEndConnector.retrieve(Url, params);
		} catch (final IOException e) {
			e.printStackTrace();
			throw new BackendException(e);
		}
		final TransactionResult r = Json.deserializeFromString(TransactionResult.class, resultJson);
		return r;

	}

	public TransactionResult transferBTC (final Operation t) throws BackendException {
		Err.throwNotImplementedYet();
		return null;
	}

	public BTCInputs listBTCInputs (final BTCAddress address, final String accountName) throws BackendException {
		final String command = "list_btc_inputs";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("Access_key", this.access_key + "");
		params.put("Btc_address", address.AddressString + "");
		params.put("Account_name", accountName);
		JsonString resultJson;
		try {
			resultJson = BackEndConnector.retrieve(Url, params);
		} catch (final IOException e) {
			e.printStackTrace();
			throw new BackendException(e);
		}
		final BTCInputs r = Json.deserializeFromString(BTCInputs.class, resultJson);
		return r;
	}

	public PFCInputs listPFCInputs (final PFCAddress address, final String accountName) throws BackendException {
		final String command = "list_pfc_inputs";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("Access_key", this.access_key + "");
		params.put("Pfc_address", address.AddressString + "");
		params.put("Account_name", accountName);
		JsonString resultJson;
		try {
			resultJson = BackEndConnector.retrieve(Url, params);
		} catch (final IOException e) {
			e.printStackTrace();
			throw new BackendException(e);
		}
		final PFCInputs r = Json.deserializeFromString(PFCInputs.class, resultJson);
		return r;
	}

	public BTCAddress getNewBTCAddress (final String accountName) throws BackendException {
		final String command = "new_btc_address";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("Access_key", this.access_key);
		params.put("Account_name", accountName);
		JsonString resultJson;
		try {
			resultJson = BackEndConnector.retrieve(Url, params);
		} catch (final IOException e) {
			e.printStackTrace();
			throw new BackendException(e);
		}
		final BTCAddress r = Json.deserializeFromString(BTCAddress.class, resultJson);
		return r;
	}

	public PlottedChart plotChart (final String Chart_data_json) throws BackendException {
		final String command = "plot_chart";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("Access_key", this.access_key);
		params.put("Chart_data_json", Chart_data_json);
		JsonString resultJson;
		try {
			resultJson = BackEndConnector.retrieve(Url, params);
		} catch (final IOException e) {
			e.printStackTrace();
			throw new BackendException(e);
		}
		final PlottedChart r = Json.deserializeFromString(PlottedChart.class, resultJson);
		return r;
	}

	public PFCAddress getNewPFCAddress (final String accountName) throws BackendException {
		final String command = "new_pfc_address";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("Access_key", this.access_key);
		params.put("Account_name", accountName);
		JsonString resultJson;
		try {
			resultJson = BackEndConnector.retrieve(Url, params);
		} catch (final IOException e) {
			e.printStackTrace();
			throw new BackendException(e);
		}
		final PFCAddress r = Json.deserializeFromString(PFCAddress.class, resultJson);
		return r;
	}

	public DCRAddress getNewDCRAddress (final String accountName) throws BackendException {
		final String command = "new_dcr_address";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("Access_key", this.access_key);
		params.put("Account_name", accountName);
		JsonString resultJson;
		try {
			resultJson = BackEndConnector.retrieve(Url, params);
		} catch (final IOException e) {
			e.printStackTrace();
			throw new BackendException(e);
		}
		final DCRAddress r = Json.deserializeFromString(DCRAddress.class, resultJson);
		return r;
	}

	public TradeResult tradePFC (//
		final TRADE_OPERATION op, //
		final boolean getQuote, //
		final AmountPFC amount, //
		final Double dcr_for_1_pfc_order, //
		final PFCAddress user_pfc_account, //
		final DCRAddress user_dcr_account, //
		final PFCAddress exchange_pfc_account, //
		final DCRAddress exchange_dcr_account//
	) throws BackendException {

		final String command = "trade_pfc";
		final HttpURL Url = this.commadToUrl(command);
		final Map<String, String> params = Collections.newMap();
		params.put("Access_key", this.access_key + "");
		params.put("Pfc_amount", amount.Value + "");
		params.put("Operation", op + "");
		params.put("Getquote", getQuote + "");

		params.put("User_pfc_account", user_pfc_account + "");
		params.put("User_dcr_account", user_dcr_account + "");
		params.put("Exchange_pfc_account", exchange_pfc_account + "");
		params.put("Exchange_dcr_account", exchange_dcr_account + "");

		params.put("Dcr_for_1_pfc_order", dcr_for_1_pfc_order + "");

		JsonString resultJson;
		try {
			resultJson = BackEndConnector.retrieve(Url, params);
		} catch (final IOException e) {
			e.printStackTrace();
			throw new BackendException(e);
		}
		final TradeResult r = Json.deserializeFromString(TradeResult.class, resultJson);
		return r;
	}

}
