
package org.picfight.exchbot.lambda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.picfight.exchbot.lambda.backend.AvailableFunds;
import org.picfight.exchbot.lambda.backend.BTCBalance;
import org.picfight.exchbot.lambda.backend.PFCBalance;
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

		final FilesList taskFileList = rootFS.Newo.listAllChildren();

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
			return this.processExpired(t, fs, deadline);
		}
		if (t.type.equalsIgnoreCase(Transaction.BUY)) {
			final BTCBalance balance = walletBackEnd.getBalanceForBTCAddress(t.exchangeBTCWallet);

			if (balance.value < minBTCOperation && balance.value > 0) {
				return this.processNoEnoughBTCReceived(t, fs, balance, minBTCOperation);
			} else {
				return this.processBuyPFC(t, fs, balance);
			}
		} else if (t.type.equalsIgnoreCase(Transaction.SELL)) {
			final PFCBalance balance = walletBackEnd.getBalanceForPFCAddress(t.exchangePFCWallet);

			final double priceBTC = Exchange.exchangeRateBTC();
			final double pfcAmount = minBTCOperation / priceBTC;
			final double minPFCOperation = pfcAmount;

			if (balance.value < minPFCOperation && balance.value > 0) {
				return this.processNoEnoughPFCReceived(t, fs, balance, minBTCOperation);
			} else {
				return this.processSellPFC(t, fs, balance);
			}
		}
		return null;
	}

	private TransactionStatus processNoEnoughPFCReceived (final Transaction t, final FilesystemSetup fs, final PFCBalance balance,
		final double minPFCOperation) throws IOException {
		final TransactionStatus s = new TransactionStatus();
		s.transact = t;
		s.status = TransactionStatus.NO_ENOUGH_PFC;
		s.error_message = "No enough PFC to execute transaction. Received " + balance.value + "PFC, required at least "
			+ minPFCOperation + "PFC";
		final String file_name = TransactionBackEnd.file_name(t);
		final File file = fs.NoEnoughPFC.child(file_name);
		file.writeJson(s);
		return s;
	}

	private TransactionStatus processNoEnoughBTCReceived (final Transaction t, final FilesystemSetup fs, final BTCBalance balance,
		final double minBTCOperation) throws IOException {
		final TransactionStatus s = new TransactionStatus();
		s.transact = t;
		s.status = TransactionStatus.NO_ENOUGH_BTC;
		s.error_message = "No enough BTC to execute transaction. Received " + balance.value + "BTC, required at least "
			+ minBTCOperation + "BTC";
		final String file_name = TransactionBackEnd.file_name(t);
		final File file = fs.NoEnoughBTC.child(file_name);
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
		final File file = fs.Expired.child(file_name);
		file.writeJson(s);
		return s;
	}

	private TransactionStatus reportSuccess (final Transaction t, final FilesystemSetup fs, final TransferResult transferResult)
		throws IOException {
		final TransactionStatus s = new TransactionStatus();
		s.transact = t;
		s.status = TransactionStatus.EXECUTED;
		s.result = transferResult;
		final String file_name = TransactionBackEnd.file_name(t);
		final File file = fs.Executed.child(file_name);
		file.writeJson(s);
		return s;
	}

	private TransactionStatus processSellPFC (final Transaction t, final FilesystemSetup fs, final PFCBalance balance)
		throws IOException {

		final double pfcAmount = balance.value;
		final AvailableFunds funds = walletBackEnd.getFunds();
		final double priceBTC = Exchange.sellPriceBTC(funds);

		final double btcAmount = priceBTC * priceBTC;

		t.btcAmount = btcAmount;

		if (funds.AvailableBTC <= btcAmount) {
			return this.reportWalletNoEnoughBTC(t, fs, btcAmount, funds);
		}

		final TransferResult transferResult = walletBackEnd.transferBTC(t);
		if (transferResult.success == true) {
			return this.reportSuccess(t, fs, transferResult);
		}
		return null;
	}

	private TransactionStatus reportWalletNoEnoughBTC (final Transaction t, final FilesystemSetup fs, final double btcAmount,
		final AvailableFunds balance) throws IOException {
		final TransactionStatus s = new TransactionStatus();
		s.transact = t;
		s.status = TransactionStatus.BACKEND_ERROR;
		s.error_message = "Available " + balance.AvailableBTC + "BTC, required " + btcAmount + "BTC to execute operation.";
		final String file_name = TransactionBackEnd.file_name(t);
		final File file = fs.Error.child(file_name);
		file.writeJson(s);
		return s;
	}

	private TransactionStatus reportWalletNoEnoughPFC (final Transaction t, final FilesystemSetup fs, final double pfcAmount,
		final AvailableFunds balance) throws IOException {
		final TransactionStatus s = new TransactionStatus();
		s.transact = t;
		s.status = TransactionStatus.BACKEND_ERROR;
		s.error_message = "Available " + balance.AvailablePFC + "PFC, required " + pfcAmount + "PFC to execute operation.";
		final String file_name = TransactionBackEnd.file_name(t);
		final File file = fs.Error.child(file_name);
		file.writeJson(s);
		return s;
	}

	private TransactionStatus processBuyPFC (final Transaction t, final FilesystemSetup fs, final BTCBalance balance)
		throws IOException {
		final AvailableFunds funds = walletBackEnd.getFunds();
		final double priceBTC = Exchange.buyPriceBTC(funds);
		final double pfcAmount = balance.value / priceBTC;

		t.pfcAmount = pfcAmount;

		if (funds.AvailablePFC <= pfcAmount) {
			return this.reportWalletNoEnoughPFC(t, fs, pfcAmount, funds);
		}

		final TransferResult transferResult = walletBackEnd.transferPFC(t);
		if (transferResult.success == true) {
			return this.reportSuccess(t, fs, transferResult);
		}
		return null;
	}

// ----------------------

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

		setup.Root = root;
		setup.Orders = root.child("orders");
		{
			setup.Newo = setup.Orders.child("new");
			setup.Newo.makeFolder();

			setup.Executed = setup.Orders.child("executed");
			setup.Executed.makeFolder();

			setup.Expired = setup.Orders.child("expired");
			setup.Expired.makeFolder();

			setup.Processing = setup.Orders.child("processing");
			setup.Processing.makeFolder();

			{
				setup.Error = setup.Processing.child("error");
				setup.Error.makeFolder();

				setup.NoEnoughBTC = setup.Processing.child("no_btc");
				setup.NoEnoughBTC.makeFolder();

				setup.NoEnoughPFC = setup.Processing.child("no_pfc");
				setup.NoEnoughPFC.makeFolder();
			}
		}

		return setup;
	}

	public static void init () {
	}

	public static FilesystemSetup FS () {
		return rootFS;
	}

}
