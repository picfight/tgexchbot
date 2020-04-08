
package org.picfight.exchbot.back;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;

import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.red.desktop.ScarabeiDesktop;

public class TestKeystore {

	static {
		// for localhost testing only
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {

			@Override
			public boolean verify (final String hostname, final javax.net.ssl.SSLSession sslSession) {
				if (hostname.equals("localhost")) {
					return true;
				}
				return false;
			}
		});
	}

	public static void main (final String[] args) throws Exception {
		ScarabeiDesktop.deploy();
		{
			final FileInputStream is = new FileInputStream("D:\\PICFIGHT\\src\\github.com\\picfight\\tgexchbot\\java.ks");

			final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(is, "power007".toCharArray());
			final Enumeration<String> list = keystore.aliases();
			for (; list.hasMoreElements();) {
				final String key = list.nextElement();
				L.d("key", key);
				final Certificate c = keystore.getCertificate(key);
// final Key K = keystore.getKey(key, "power007".toCharArray());
				L.d("C", c);
			}
		}
	}

}
