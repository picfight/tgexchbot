
package org.picfight.exchbot.back;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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
import com.jfixby.scarabei.api.io.IO;
import com.jfixby.scarabei.api.io.InputStream;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.names.Names;
import com.jfixby.scarabei.api.sys.settings.SystemSettings;
import com.jfixby.scarabei.red.desktop.ScarabeiDesktop;

public class TestSSL {

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

	public static void main (final String[] args) throws IOException, NoSuchAlgorithmException, KeyManagementException {
		ScarabeiDesktop.deploy();

		final URL url = new URL("https://exchange.picfight.org:8080/");
// final URL url = new URL("https://google.com/");
		final URLConnection connection = url.openConnection();
		// JMD - this is a better way to do it that doesn't override the default SSL factory.
		if (connection instanceof HttpsURLConnection) {
			final HttpsURLConnection conHttps = (HttpsURLConnection)connection;
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
						L.d("checkServerTrusted", authType);
						L.d("                  ", list);
						if (list.size() != 1) {
							throw new CertificateException();
						}
						final X509Certificate element = list.getElementAt(0);
						final byte[] signature = element.getSignature();
						final String hexSring = bytesToHex(signature);
						L.d("hex", hexSring);

						final String pinnedSslCertSign = SystemSettings
							.getRequiredStringParameter(Names.newID("PINNED_SSL_CERT_SIG_STRING"));
						// 30640230377E2096900EBA8D4F7FA3CA1AF6A22877A4D827002CE81D00FCCF3AA93095E49D2F3631B8976C2A0FDAE157634B289402304050C6ADBF56624316694B0A5F55BA511417F0B0698D634CFB60301D87D996AB66C5B49221A9E85144F20907FE491D72

						if (!pinnedSslCertSign.equals(hexSring)) {
							throw new CertificateException();
						}

					}

					@Override
					public X509Certificate[] getAcceptedIssuers () {
						return new X509Certificate[0];
					}

				}};

			// Get a new SSL context
			final SSLContext sc = SSLContext.getInstance("TLSv1.2");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
// sc.init(null, null, new java.security.SecureRandom());
// Set our connection to use this SSL context, with the "Trust all" manager in place.
			conHttps.setSSLSocketFactory(sc.getSocketFactory());
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
		final java.io.InputStream stream = connection.getInputStream();

		final InputStream is = IO.newInputStream( () -> stream);
		is.open();
		final String data = is.readAllToString();
		is.close();
		L.d("data", data);

	}

}
