
package org.picfight.exchbot.back;

public class BackEndConnection {

	public BackEndConnection (final BackEndConnectionConfig cfg) {
		this.host = cfg.host;
		this.port = cfg.port;
		this.certFile = cfg.certFile;

	}

}
