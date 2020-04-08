
package org.picfight.exchbot.lambda.backend;

import java.io.IOException;

import org.picfight.exchbot.back.BackEndConnector;

import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.collections.Map;
import com.jfixby.scarabei.api.json.JsonString;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.net.http.Http;
import com.jfixby.scarabei.api.net.http.HttpCall;
import com.jfixby.scarabei.api.net.http.HttpCallExecutor;
import com.jfixby.scarabei.api.net.http.HttpCallParams;
import com.jfixby.scarabei.api.net.http.HttpCallProgress;
import com.jfixby.scarabei.api.net.http.HttpURL;

public class ExchangeBackEnd {

	final public String host;
	final public int port;
	final public String access_key;

	public ExchangeBackEnd (final ExchangeBackEndArgs args) {
		this.host = args.host;
		this.port = args.port;
		this.access_key = args.access_key;
	}

	public Rate getRate () throws IOException {
		final Rate r = new Rate();

		final String command = "rate";
		final HttpURL Url = this.commadToUrl(command);

		final Map<String, String> params = Collections.newMap();
		final JsonString resultJson = BackEndConnector.retrieve(Url, params);

		L.d("resultJson", resultJson);

		return r;
	}

	private HttpURL commadToUrl (final String command) {
		final HttpURL url = Http.newURL(this.host + ":" + this.port + "/" + command);
		return url;
	}

	public BTCAddress obtainNewBTCAddress (final BTCAddressArgs a) throws IOException {
		final BTCAddress result = new BTCAddress();

		final String callResult = this.makeCall();

		return result;
	}

	private String makeCall () throws IOException {
		final HttpCallExecutor exe = Http.newCallExecutor();
		final HttpCallParams params = Http.newCallParams();

		final String url_string = "";
		final HttpURL http_url = Http.newURL(url_string);
		params.setURL(http_url);

		final HttpCall call = Http.newCall(params);
		final HttpCallProgress call_result = exe.execute(call);
		final String resultString = call_result.readResultAsString();
		L.d("resultString", resultString);
		return resultString;
	}

	public PFCAddress obtainNewPFCAddress (final PFCAddressArgs a) throws IOException {
		final PFCAddress result = new PFCAddress();

		final String callResult = this.makeCall();

		return result;
	}

}
