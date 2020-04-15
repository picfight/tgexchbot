
package org.picfight.exchbot.lambda.backend;

import java.io.IOException;

import org.picfight.exchbot.lambda.FilesystemSetup;

import com.jfixby.scarabei.api.file.File;

public class TransactionBackEnd {

	public TransactionBackEnd (final TransactionBackEndArgs transactionArgs) {
	}

	public File registerTransaction (final Transaction transact, final FilesystemSetup filesystem) throws IOException {
		final String order_name = this.order_name(transact);
		final File orderFile = filesystem.newo.child(order_name);
		orderFile.writeJson(transact);
		return orderFile;
	}

	private String order_name (final Transaction transact) {
		if (transact.type.equals(Transaction.BUY)) {
			return transact.type + " " + transact.clientPFCWallet.AddressString + " " + transact.exchangeBTCWallet.AddressString
				+ " " + transact.timestamp + ".json";
		}
		if (transact.type.equals(Transaction.SELL)) {
			return transact.type + " " + transact.clientBTCWallet.AddressString + " " + transact.exchangePFCWallet.AddressString
				+ " " + transact.timestamp + ".json";
		}
		throw new Error();
	}

}
