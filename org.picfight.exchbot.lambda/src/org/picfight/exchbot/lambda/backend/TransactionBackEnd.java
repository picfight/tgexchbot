
package org.picfight.exchbot.lambda.backend;

import java.io.IOException;

import org.picfight.exchbot.lambda.FilesystemSetup;
import org.picfight.exchbot.lambda.Transaction;

import com.jfixby.scarabei.api.file.File;

public class TransactionBackEnd {

	public TransactionBackEnd (final TransactionBackEndArgs transactionArgs) {
	}

	public static String file_name (final Transaction s) {
// final Operation transact = s.operation;
// if (transact.type.equals(Operation.BUY)) {
// return transact.type + " " + transact.clientPFCWallet.AddressString + " " + transact.exchangeBTCWallet.AddressString
// + " " + transact.timestamp + ".json";
// }
// if (transact.type.equals(Operation.SELL)) {
// return transact.type + " " + transact.clientBTCWallet.AddressString + " " + transact.exchangePFCWallet.AddressString
// + " " + transact.timestamp + ".json";
// }
		throw new Error();
	}

	public UserSettings getUserSettings (final String userID, final FilesystemSetup filesystem) throws IOException {
		final String filename = userID;

		final File userFolder = filesystem.Users.child(filename);
		if (!userFolder.exists()) {
			userFolder.makeFolder();
		}

		final File settingsFile = userFolder.child("settings.json");
		final UserSettings s = new UserSettings(settingsFile);
		if (settingsFile.exists()) {
			s.readFromFile();
		} else {
			s.data.accountName = userID;
			s.writeFile();
		}
		return s;
	}

}
