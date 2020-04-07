
package org.picfight.exchbot.lambda.backend;

import java.io.IOException;

import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.net.http.Http;
import com.jfixby.scarabei.api.net.http.HttpCall;
import com.jfixby.scarabei.api.net.http.HttpCallExecutor;
import com.jfixby.scarabei.api.net.http.HttpCallParams;
import com.jfixby.scarabei.api.net.http.HttpCallProgress;
import com.jfixby.scarabei.api.net.http.HttpURL;

public class ExchangeBackEnd {

	final public String host;
	final public long port;
	final public String access_key;

	public ExchangeBackEnd (final ExchangeBackEndArgs args) {
		this.host = args.host;
		this.port = args.port;
		this.access_key = args.access_key;

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
