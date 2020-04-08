
package org.picfight.exchbot.back;

import java.io.IOException;

import com.jfixby.scarabei.api.json.JsonString;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.net.http.Http;
import com.jfixby.scarabei.api.net.http.HttpURL;
import com.jfixby.scarabei.red.desktop.ScarabeiDesktop;

public class TestConnection {

	public static void main (final String[] args) throws IOException {

		ScarabeiDesktop.deploy();

		final HttpURL url = Http.newURL("https://exchange.picfight.org:8080/");
		final JsonString data = BackEndConnector.retrieve(url, null);
		L.d("data", data);

	}

}
