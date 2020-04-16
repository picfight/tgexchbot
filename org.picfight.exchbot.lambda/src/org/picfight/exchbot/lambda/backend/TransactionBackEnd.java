
package org.picfight.exchbot.lambda.backend;

import java.io.IOException;

import org.picfight.exchbot.lambda.FilesystemSetup;
import org.picfight.exchbot.lambda.TransactionStatus;

import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.file.FilesList;

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

	public TransactionStatus findTransaction (final String searchterm, final FilesystemSetup filesystem) throws IOException {
		{
			final FilesList list = filesystem.newo.listAllChildren(f -> f.getName().contains(searchterm));
			if (list.size() != 0) {
				final File transactFile = list.getLast();
				final Transaction transact = transactFile.readJson(Transaction.class);

				final TransactionStatus s = new TransactionStatus();
				s.transact = transact;
				s.status = TransactionStatus.ORDER_REGISTERED;

				return s;
			}
		}

		{
			final FilesList list = filesystem.done.listAllChildren(f -> f.getName().contains(searchterm));
			if (list.size() != 0) {
				final File transactFile = list.getLast();
				final TransactionStatus transact = transactFile.readJson(TransactionStatus.class);
				return transact;
			}
		}

		{
			final FilesList list = filesystem.expired.listAllChildren(f -> f.getName().contains(searchterm));
			if (list.size() != 0) {
				final File transactFile = list.getLast();
				final TransactionStatus transact = transactFile.readJson(TransactionStatus.class);
				return transact;
			}
		}

		{
			final FilesList list = filesystem.processing.listAllChildren(f -> f.getName().contains(searchterm));
			if (list.size() != 0) {
				final File transactFile = list.getLast();
				final TransactionStatus transact = transactFile.readJson(TransactionStatus.class);
				return transact;
			}
		}

		return null;
	}

}
