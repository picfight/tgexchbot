
package org.picfight.exchbot.lambda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.picfight.exchbot.lambda.backend.AvailableFunds;
import org.picfight.exchbot.lambda.backend.Balance;
import org.picfight.exchbot.lambda.backend.Transaction;
import org.picfight.exchbot.lambda.backend.TransactionBackEnd;
import org.picfight.exchbot.lambda.backend.TransactionBackEndArgs;
import org.picfight.exchbot.lambda.backend.WalletBackEnd;
import org.picfight.exchbot.lambda.backend.WalletBackEndArgs;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.jfixby.scarabei.api.collections.Map;
import com.jfixby.scarabei.api.err.Err;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.file.FilesList;
import com.jfixby.scarabei.api.io.IO;
import com.jfixby.scarabei.api.json.Json;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.names.ID;
import com.jfixby.scarabei.api.names.Names;
import com.jfixby.scarabei.api.sys.settings.ExecutionMode;
import com.jfixby.scarabei.api.sys.settings.SystemSettings;
import com.jfixby.scarabei.aws.api.AWSCredentialsViaSystemSettingsProvider;
import com.jfixby.scarabei.aws.api.s3.S3;
import com.jfixby.scarabei.aws.api.s3.S3Component;
import com.jfixby.scarabei.aws.api.s3.S3FileSystem;
import com.jfixby.scarabei.aws.api.s3.S3FileSystemConfig;
import com.jfixby.scarabei.aws.desktop.s3.DesktopS3;
import com.jfixby.scarabei.red.desktop.ScarabeiDesktop;

public class LambdaProcEntryPoint implements RequestStreamHandler {
	private static FilesystemSetup rootFS;
	private static double minBTCOperation;
	private static double expirationHours;

	static {
		ScarabeiDesktop.deploy();
		S3.installComponent(new DesktopS3());
		deployFS();
	}

	static public final WalletBackEnd walletBackEnd;
	static public final TransactionBackEnd transactionsBackEnd;
	static {
		minBTCOperation = Double.parseDouble(SystemSettings.getRequiredStringParameter(Names.newID("MIN_BTC_OPERATION")));
		expirationHours = Double.parseDouble(SystemSettings.getRequiredStringParameter(Names.newID("EXPIRATION_TIMEOUT_HOURS")));

		final WalletBackEndArgs walletArgs = new WalletBackEndArgs();
		walletArgs.access_key = SystemSettings.getRequiredStringParameter(Names.newID("ACCESS_KEY"));
		walletArgs.host = SystemSettings.getRequiredStringParameter(Names.newID("BACKEND_HOST"));// "https://exchange.picfight.org";
		walletArgs.port = Integer.parseInt(SystemSettings.getRequiredStringParameter(Names.newID("BACKEND_PORT")));// "https://exchange.picfight.org";

		walletBackEnd = new WalletBackEnd(walletArgs);

		final TransactionBackEndArgs transactionArgs = new TransactionBackEndArgs();
		transactionsBackEnd = new TransactionBackEnd(transactionArgs);
	}

	@Override
	public void handleRequest (final InputStream input, final OutputStream output, final Context context) {
		try {
			final com.jfixby.scarabei.api.io.InputStream is = IO.newInputStream( () -> input);
			is.open();
			final String data = is.readAllToString();
			is.close();
			L.d("DATA", data);

			final CloudWatchEvent dt = Json.deserializeFromString(CloudWatchEvent.class, data);

			this.handle(dt);
// final ObjectMapper objectReader = new ObjectMapper();
// final Update update = objectReader.readValue(dt.body, Update.class);
//// update = objectReader.readValue(input, );
// L.d("Update @ '" + getFormattedTimestamp(update) + "' : " + update);
// L.d("Starting handling update " + update.getUpdateId());
// this.handleUpdate(update);
// L.d("Finished handling update " + update.getUpdateId());

			final String response = "{ \"statusCode\": 200, \"headers\": {\"Content-Type\": \"application/json\"}, \"body\": \"\" }";

			final com.jfixby.scarabei.api.io.OutputStream os = IO.newOutputStream( () -> output);
			os.open();
			os.write(response.getBytes());
			os.flush();
			os.close();
		} catch (final Throwable e) {
			e.printStackTrace();
		}

	}

	private void handle (final CloudWatchEvent dt) throws IOException {

		final FilesList taskFileList = rootFS.newo.listAllChildren();

		for (final File f : taskFileList) {
			final Transaction t = f.readJson(Transaction.class);
			final TransactionStatus s = this.tryToExecute(t, rootFS);
			f.delete();
		}

	}

	private TransactionStatus tryToExecute (final Transaction t, final FilesystemSetup fs) throws IOException {

		final long now = System.currentTimeMillis();
		final long deadline = t.timestamp + (long)(expirationHours * 60 * 60 * 1000);
		if (deadline > now) {
			final TransactionStatus s = this.processExpired(t, fs, deadline);
			return s;
		}
		if (t.type.equalsIgnoreCase(Transaction.BUY)) {
			final Balance balance = walletBackEnd.getBalanceForAddress(t.exchangeBTCWallet.AddressString);
			if (balance.value < minBTCOperation) {
				final TransactionStatus s = this.processNoEnoughBTC(t, fs, balance, minBTCOperation);
				return s;
			} else {

				this.processBuyPFC(t, fs, balance);

// Exchange.buyPrice(rate)
// balance
			}
		} else if (t.type.equalsIgnoreCase(Transaction.SELL)) {

		}

		return null;

	}

	private TransactionStatus processBuyPFC (final Transaction t, final FilesystemSetup fs, final Balance balance)
		throws IOException {
		final AvailableFunds funds = walletBackEnd.getFunds();
		final double priceBTC = Exchange.buyPriceBTC(funds);
		final double pfcAmount = balance.value / priceBTC;

		if (funds.AvailablePFC <= pfcAmount) {
			return this.reportNoEnoughPFC(t, fs, pfcAmount, balance);
		}

		final TransferResult transferResult = walletBackEnd.transferPFC(t);

	}

	private TransactionStatus reportNoEnoughPFC (final Transaction t, final FilesystemSetup fs, final double pfcAmount,
		final Balance balance) throws IOException {
		final TransactionStatus s = new TransactionStatus();
		s.transact = t;
		s.status = TransactionStatus.ORDER_STUCK;
		s.error_message = "Balance is " + balance.value + "PFC, required " + pfcAmount + "PFC";
		final String file_name = TransactionBackEnd.file_name(t);
		final File file = fs.expired.child(file_name);
		file.writeJson(s);
		return s;
	}

	private TransactionStatus processExpired (final Transaction t, final FilesystemSetup fs, final long deadline)
		throws IOException {
		final TransactionStatus s = new TransactionStatus();
		s.transact = t;
		s.status = TransactionStatus.ORDER_EXPIRED;
		s.error_message = "Transaction expired at " + new Date(deadline);
		final String file_name = TransactionBackEnd.file_name(t);
		final File file = fs.expired.child(file_name);
		file.writeJson(s);
		return s;
	}

	private TransactionStatus processNoEnoughBTC (final Transaction t, final FilesystemSetup fs, final Balance balance,
		final double minBTCOperation) throws IOException {
		final TransactionStatus s = new TransactionStatus();
		s.transact = t;
		s.status = TransactionStatus.ORDER_STUCK;
		s.error_message = "No enough BTC to execute transaction. Received " + balance.value + "BTC, required at least "
			+ minBTCOperation + "BTC";
		final String file_name = TransactionBackEnd.file_name(t);
		final File file = fs.processing.child(file_name);
		file.writeJson(s);
		return s;
	}

	private static void deployFS () {
		try {
			rootFS = deployS3FileSystem();
		} catch (final IOException e) {
			Err.reportError(e);
		}
	}

	public static FilesystemSetup deployS3FileSystem () throws IOException {
		final S3Component s3 = S3.invoke();

		SystemSettings.setExecutionMode(ExecutionMode.EARLY_DEVELOPMENT);
		final Map<ID, Object> settings = SystemSettings.listAllSettings();
// L.d("System settings", settings);

		final S3FileSystemConfig s3config = s3.newFileSystemConfig();
		s3config.awsCredentialsProvider = new AWSCredentialsViaSystemSettingsProvider();
		s3config.bucketName = AWSCredentialsViaSystemSettingsProvider.readParam(s3.BUCKET_NAME());

		L.d("Connecting to S3 Bucket", s3config.bucketName + " at " + s3config.awsCredentialsProvider.getRegionName() + " ...");
		L.d("s3config", s3config.toString());

		final S3FileSystem fs = s3.newFileSystem(s3config);

		final File root = fs.ROOT().child("exchangebot");
		root.makeFolder();

		final FilesystemSetup setup = new FilesystemSetup();

		setup.root = root;

		setup.orders = root.child("orders");
		setup.newo = setup.orders.child("new");
		setup.newo.makeFolder();
		setup.done = setup.orders.child("executed");
		setup.done.makeFolder();
		setup.expired = setup.orders.child("expired");
		setup.expired.makeFolder();
		setup.processing = setup.orders.child("processing");
		setup.processing.makeFolder();

		return setup;
	}

	public static void init () {
	}

	public static FilesystemSetup FS () {
		return rootFS;
	}

}
