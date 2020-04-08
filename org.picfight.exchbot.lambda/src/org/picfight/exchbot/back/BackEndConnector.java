
package org.picfight.exchbot.back;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.collections.List;
import com.jfixby.scarabei.api.collections.Map;
import com.jfixby.scarabei.api.io.IO;
import com.jfixby.scarabei.api.io.InputStream;
import com.jfixby.scarabei.api.json.Json;
import com.jfixby.scarabei.api.json.JsonString;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.names.Names;
import com.jfixby.scarabei.api.net.http.HttpURL;
import com.jfixby.scarabei.api.sys.settings.SystemSettings;

public class BackEndConnector {

	public BackEndConnector () {
	}

	public static JsonString retrieve (final HttpURL Url, final Map<String, String> params) throws IOException {
		final URL url = new URL(Url.toString());

		// final URL url = new URL("https://google.com/");
		final HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
		connection.setReadTimeout(10000);
		connection.setConnectTimeout(15000);
		connection.setRequestMethod("POST");
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		// JMD - this is a better way to do it that doesn't override the default SSL factory.
		{
			final HttpsURLConnection conHttps = connection;
			// Set up a Trust all manager
			final TrustManager[] trustAllCerts = new TrustManager[] {
				//
				new X509TrustManager() {
					@Override
					public void checkClientTrusted (final X509Certificate[] chain, final String authType) throws CertificateException {
						final List<X509Certificate> list = Collections.newList(chain);
						L.d("checkClientTrusted", authType);
						L.d("                  ", list);
						throw new CertificateException();
					}

					@Override
					public void checkServerTrusted (final X509Certificate[] chain, final String authType) throws CertificateException {
						final List<X509Certificate> list = Collections.newList(chain);
						if (list.size() != 1) {
							L.d("checkServerTrusted", authType);
							L.d("                  ", list);
							throw new CertificateException();
						}
						final X509Certificate element = list.getElementAt(0);
						final byte[] signature = element.getSignature();
						final String hexSring = bytesToHex(signature);

						final String pinnedSslCertSign = SystemSettings
							.getRequiredStringParameter(Names.newID("PINNED_SSL_CERT_SIG_STRING"));
						// 30640230377E2096900EBA8D4F7FA3CA1AF6A22877A4D827002CE81D00FCCF3AA93095E49D2F3631B8976C2A0FDAE157634B289402304050C6ADBF56624316694B0A5F55BA511417F0B0698D634CFB60301D87D996AB66C5B49221A9E85144F20907FE491D72

						if (!pinnedSslCertSign.equals(hexSring)) {
							L.d("     hex", hexSring);
							L.d("expected", pinnedSslCertSign);
							throw new CertificateException();
						}

					}

					@Override
					public X509Certificate[] getAcceptedIssuers () {
						return new X509Certificate[0];
					}

				}};

			// Get a new SSL context
			try {
				final SSLContext sc = SSLContext.getInstance("TLSv1.2");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				conHttps.setSSLSocketFactory(sc.getSocketFactory());
			} catch (final Exception e) {
				throw new IOException(e);
			}
			// sc.init(null, null, new java.security.SecureRandom());
			// Set our connection to use this SSL context, with the "Trust all" manager in place.

			// Also force it to trust all hosts
			final HostnameVerifier allHostsValid = new HostnameVerifier() {
				@Override
				public boolean verify (final String hostname, final SSLSession session) {
					L.d("hostname", hostname);

					return false;
				}
			};
			// and set the hostname verifier.
			conHttps.setHostnameVerifier(allHostsValid);
		}

		{
// params.put("name", "Freddie the Fish");
// params.put("email", "fishie@seamail.example.com");
// params.put("reply_to_thread", 10394);
// params.put("message",
// "Shark attacks in Botany Bay have gotten out of control. We need more defensive dolphins to protect the schools here, but Mayor
// Porpoise is too busy stuffing his snout with lobsters. He's so shellfish.");

			final StringBuilder postData = new StringBuilder();
			for (final String key : params.keys()) {
				final String value = params.get(key);
				if (postData.length() != 0) {
					postData.append('&');
				}
				postData.append(URLEncoder.encode(key, "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(value, "UTF-8"));
			}
			final byte[] postDataBytes = postData.toString().getBytes("UTF-8");
			connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			connection.getOutputStream().write(postDataBytes);
		}

		final java.io.InputStream stream = connection.getInputStream();

		final InputStream is = IO.newInputStream( () -> stream);
		is.open();
		final String data = is.readAllToString();
		is.close();
// L.d("data", data);

		return Json.newJsonString(data);
	}

	static private String getQuery (final Map<String, String> params) throws UnsupportedEncodingException {
		final StringBuilder result = new StringBuilder();
		boolean first = true;

// for (final String key : params.keys()) {
// // connection.addRequestProperty(key, params.get(key));
// // connection.setRequestProperty(key, params.get(key));
//
// }

		for (final String key : params.keys()) {
			if (first) {
				first = false;
			} else {
				result.append("&");
			}

			result.append(URLEncoder.encode(key, "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(params.get(key), "UTF-8"));
		}

		return result.toString();
	}

	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex (final byte[] bytes) {
		final char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			final int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}

}
