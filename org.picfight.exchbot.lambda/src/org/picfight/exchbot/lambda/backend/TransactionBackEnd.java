
package org.picfight.exchbot.lambda.backend;

import java.io.IOException;

import org.picfight.exchbot.lambda.FilesystemSetup;
import org.picfight.exchbot.lambda.Transaction;

import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.file.FilesList;

public class TransactionBackEnd {

	public TransactionBackEnd (final TransactionBackEndArgs transactionArgs) {
	}

	public File registerTransaction (final Transaction transact, final FilesystemSetup filesystem) throws IOException {
		final String order_name = file_name(transact);
		final File orderFile = filesystem.Newo.child(order_name);
		orderFile.writeJson(transact);
		return orderFile;
	}

	public static String file_name (final Transaction s) {
		final Transaction transact = s.transact;
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

	public Transaction findTransaction (final String searchterm, final FilesystemSetup filesystem) throws IOException {
		{
			final FilesList list = filesystem.Newo.listAllChildren(f -> f.getName().contains(searchterm));
			if (list.size() != 0) {
				final File transactFile = list.getLast();
				final Transaction transact = transactFile.readJson(Transaction.class);

// final TransactionStatus s = new TransactionStatus();
// s.transact = transact;
// s.status = TransactionStatus.ORDER_REGISTERED;

				return transact;
			}
		}

		{
			final FilesList list = filesystem.Executed.listAllChildren(f -> f.getName().contains(searchterm));
			if (list.size() != 0) {
				final File transactFile = list.getLast();
				final Transaction transact = transactFile.readJson(Transaction.class);
				return transact;
			}
		}

		{
			final FilesList list = filesystem.Expired.listAllChildren(f -> f.getName().contains(searchterm));
			if (list.size() != 0) {
				final File transactFile = list.getLast();
				final Transaction transact = transactFile.readJson(Transaction.class);
				return transact;
			}
		}

		{
			final FilesList list = filesystem.Processing.listAllChildren(f -> f.getName().contains(searchterm));
			if (list.size() != 0) {
				final File transactFile = list.getLast();
				final Transaction transact = transactFile.readJson(Transaction.class);
				return transact;
			}
		}

		return null;
	}

}
