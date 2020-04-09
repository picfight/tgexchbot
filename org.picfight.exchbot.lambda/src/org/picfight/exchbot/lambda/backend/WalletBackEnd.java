
package org.picfight.exchbot.lambda.backend;

import java.io.IOException;

import org.picfight.exchbot.back.BackEndConnector;

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

	public Rate getRate () throws IOException {

		final String command = "rate";
		final HttpURL Url = this.commadToUrl(command);

		final Map<String, String> params = Collections.newMap();
		params.put("access_key", this.access_key);

		final JsonString resultJson = BackEndConnector.retrieve(Url, params);

// L.d("resultJson", resultJson);

		final Rate r = Json.deserializeFromString(Rate.class, resultJson);

		return r;
	}

	private HttpURL commadToUrl (final String command) {
		final HttpURL url = Http.newURL(this.host + ":" + this.port + "/" + command);
		return url;
	}

	public BTCAddress obtainNewBTCAddress () throws IOException {
		final String command = "new_btc_address";
		final HttpURL Url = this.commadToUrl(command);

		final Map<String, String> params = Collections.newMap();
		params.put("access_key", this.access_key);

		final JsonString resultJson = BackEndConnector.retrieve(Url, params);

		final BTCAddress r = Json.deserializeFromString(BTCAddress.class, resultJson);

		return r;
	}

	public StringAnalysis analyzeString (final String text) throws IOException {
		final String command = "analyze_string";
		final HttpURL Url = this.commadToUrl(command);

		final Map<String, String> params = Collections.newMap();
		params.put("access_key", this.access_key);
// params.put("raw_text", bytesToHex(text.getBytes()));
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

//
// private String makeCall () throws IOException {
// final HttpCallExecutor exe = Http.newCallExecutor();
// final HttpCallParams params = Http.newCallParams();
//
// final String url_string = "";
// final HttpURL http_url = Http.newURL(url_string);
// params.setURL(http_url);
//
// final HttpCall call = Http.newCall(params);
// final HttpCallProgress call_result = exe.execute(call);
// final String resultString = call_result.readResultAsString();
// L.d("resultString", resultString);
// return resultString;
// }

	public PFCAddress obtainNewPFCAddress () throws IOException {

		final String command = "new_pfc_address";
		final HttpURL Url = this.commadToUrl(command);

		final Map<String, String> params = Collections.newMap();
		params.put("access_key", this.access_key);

		final JsonString resultJson = BackEndConnector.retrieve(Url, params);

		final PFCAddress r = Json.deserializeFromString(PFCAddress.class, resultJson);

		return r;
	}

}
