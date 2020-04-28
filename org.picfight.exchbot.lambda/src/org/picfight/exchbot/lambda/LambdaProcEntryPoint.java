
package org.picfight.exchbot.lambda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.picfight.exchbot.lambda.backend.AmountBTC;
import org.picfight.exchbot.lambda.backend.AmountPFC;
import org.picfight.exchbot.lambda.backend.AvailableFunds;
import org.picfight.exchbot.lambda.backend.BTCBalance;
import org.picfight.exchbot.lambda.backend.Operation;
import org.picfight.exchbot.lambda.backend.PFCBalance;
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
	private static double expirationHours;

	static {
		ScarabeiDesktop.deploy();
		S3.installComponent(new DesktopS3());
		deployFS();
	}

	static public final WalletBackEnd walletBackEnd;
	static public final TransactionBackEnd transactionsBackEnd;
	static {
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
		{
			final FilesList taskFileList = rootFS.Newo.listAllChildren();
			for (final File f : taskFileList) {
				if (f.isFolder()) {
					continue;
				}
				Transaction s = f.readJson(Transaction.class);
				s = this.tryToExecute(s, rootFS, f);
			}
		}
		{
			final FilesList taskFileList = rootFS.Processing.listAllChildren();
			for (final File f : taskFileList) {
				if (f.isFolder()) {
					continue;
				}
				Transaction s = f.readJson(Transaction.class);
				s = this.tryToExecute(s, rootFS, f);
			}
		}

	}

	private Transaction tryToExecute (final Transaction s, final FilesystemSetup fs, final File file) throws IOException {
		final long now = System.currentTimeMillis();
		final Operation tr = s.operation;
		final long deadline = tr.timestamp + (long)(expirationHours * 60 * 60 * 1000);
		if (deadline < now) {
			return this.processExpired(s, fs, deadline, file);
		}
		if (tr.type.equalsIgnoreCase(Operation.BUY)) {
			final BTCBalance balance = walletBackEnd.getBalanceForBTCAddress(tr.exchangeBTCWallet);
			final AvailableFunds funds = walletBackEnd.getFunds();
			if (balance.amount.Value < funds.MinBTCOperation.Value && balance.amount.Value > 0) {
				return this.processNoEnoughBTCReceived(s, fs, balance, funds.MinBTCOperation, file);
			} else {
				return this.processBuyPFC(s, fs, balance, file);
			}
		} else if (tr.type.equalsIgnoreCase(Operation.SELL)) {
			final PFCBalance balance = walletBackEnd.getBalanceForPFCAddress(tr.exchangePFCWallet);

			final AvailableFunds funds = walletBackEnd.getFunds();

			final double priceBTC = funds.ExchangeRate;
			final double pfcAmount = funds.MinBTCOperation.Value / priceBTC;

			final AmountPFC minPFCOperation = new AmountPFC();
			minPFCOperation.Value = pfcAmount;

			if (balance.amount.Value < minPFCOperation.Value && balance.amount.Value > 0) {
				return this.processNoEnoughPFCReceived(s, fs, balance, minPFCOperation, file);
			} else {
				return this.processSellPFC(s, fs, balance, file);
			}
		}
		return null;
	}

	private Transaction processNoEnoughPFCReceived (final Transaction s, final FilesystemSetup fs, final PFCBalance balance,
		final AmountPFC minPFCOperation, final File ofile) throws IOException {
		final Status op = new Status();
		s.states.add(op);
		op.status = StateStrings.NO_ENOUGH_PFC;
		op.error_message = "No enough PFC to execute transaction. Received " + balance.amount + "PFC, required at least "
			+ minPFCOperation + "PFC";
		final String file_name = TransactionBackEnd.file_name(s);
		final File file = fs.NoEnoughPFC.child(file_name);
		ofile.delete();
		file.writeJson(s);
		return s;
	}

	private Transaction processNoEnoughBTCReceived (final Transaction s, final FilesystemSetup fs, final BTCBalance balance,
		final AmountBTC minBTCOperation, final File ofile) throws IOException {
		final Status op = new Status();
		s.states.add(op);
		op.status = StateStrings.NO_ENOUGH_BTC;
		op.error_message = "No enough BTC to execute transaction. Received " + balance.amount + "BTC, required at least "
			+ minBTCOperation + "BTC";
		final String file_name = TransactionBackEnd.file_name(s);
		final File file = fs.NoEnoughBTC.child(file_name);
		ofile.delete();
		file.writeJson(s);
		return s;
	}

	private Transaction processExpired (final Transaction s, final FilesystemSetup fs, final long deadline, final File ofile)
		throws IOException {
		final Status op = new Status();
		s.states.add(op);
		op.status = StateStrings.ORDER_EXPIRED;
		op.error_message = "Transaction expired at " + new Date(deadline);
		final String file_name = TransactionBackEnd.file_name(s);
		final File file = fs.Expired.child(file_name);
		file.writeJson(s);
		ofile.delete();
		return s;
	}

	private Transaction reportSuccess (final Transaction s, final FilesystemSetup fs, final Result transferResult,
		final File ofile) throws IOException {
		final Status op = new Status();
		s.states.add(op);
		op.status = StateStrings.EXECUTED;
		op.result = transferResult;
		final String file_name = TransactionBackEnd.file_name(s);
		final File file = fs.Executed.child(file_name);
		ofile.delete();
		file.writeJson(s);
		return s;
	}

	private Transaction processSellPFC (final Transaction s, final FilesystemSetup fs, final PFCBalance balance, final File ofile)
		throws IOException {

		final double pfcAmount = balance.amount.Value;
		final AvailableFunds funds = walletBackEnd.getFunds();
		final double priceBTC = Exchange.sellPriceBTC(funds);

		final double btcAmount = priceBTC * priceBTC;

		final Operation tr = s.operation;

		tr.btcAmount = new AmountBTC(btcAmount);

		if (funds.AvailableBTC.Value <= tr.btcAmount.Value) {
			return this.reportWalletNoEnoughBTC(s, fs, tr.btcAmount, funds, ofile);
		}

		final Result transferResult = walletBackEnd.transferBTC(tr);
		if (transferResult.success == true) {
			return this.reportSuccess(s, fs, transferResult, ofile);
		}
		return this.reportBackendError(s, fs, transferResult, ofile);
	}

	private Transaction reportBackendError (final Transaction s, final FilesystemSetup fs, final Result transferResult,
		final File ofile) throws IOException {
		final Status op = new Status();
		s.states.add(op);
		op.status = StateStrings.BACKEND_ERROR;
		op.result = transferResult;
		op.error_message = transferResult.error_message;
		final String file_name = TransactionBackEnd.file_name(s);
		final File file = fs.Error.child(file_name);
		ofile.delete();
		file.writeJson(s);
		return s;
	}

	private Transaction reportWalletNoEnoughBTC (final Transaction s, final FilesystemSetup fs, final AmountBTC btcAmount,
		final AvailableFunds balance, final File ofile) throws IOException {
		final Status op = new Status();
		s.states.add(op);
		op.status = StateStrings.BACKEND_ERROR;
		op.error_message = "Available " + balance.AvailableBTC + "BTC, required " + btcAmount + "BTC to execute operation.";
		final String file_name = TransactionBackEnd.file_name(s);
		final File file = fs.Error.child(file_name);
		ofile.delete();
		file.writeJson(s);
		return s;
	}

	private Transaction reportWalletNoEnoughPFC (final Transaction s, final FilesystemSetup fs, final AmountPFC pfcAmount,
		final AvailableFunds balance, final File ofile) throws IOException {
		final Status op = new Status();
		s.states.add(op);
		op.status = StateStrings.BACKEND_ERROR;
		op.error_message = "Available " + balance.AvailablePFC + "PFC, required " + pfcAmount + "PFC to execute operation.";
		final String file_name = TransactionBackEnd.file_name(s);
		final File file = fs.Error.child(file_name);
		ofile.delete();
		file.writeJson(s);
		return s;
	}

	private Transaction processBuyPFC (final Transaction s, final FilesystemSetup fs, final BTCBalance balance, final File ofile)
		throws IOException {
		final AvailableFunds funds = walletBackEnd.getFunds();
		final double priceBTC = Exchange.buyPriceBTC(funds);
		final double pfcAmount = balance.amount.Value / priceBTC;
		final Operation tr = s.operation;
		tr.pfcAmount = new AmountPFC(pfcAmount);

		if (funds.AvailablePFC.Value <= tr.pfcAmount.Value) {
			return this.reportWalletNoEnoughPFC(s, fs, tr.pfcAmount, funds, ofile);
		}

		final Result transferResult = walletBackEnd.transferPFC(tr);
		if (transferResult.success == true) {
			return this.reportSuccess(s, fs, transferResult, ofile);
		}
		return this.reportBackendError(s, fs, transferResult, ofile);
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

		final FilesystemSetup setup = new FilesystemSetup(root);

		return setup;
	}

	public static void init () {
	}

	public static FilesystemSetup FS () {
		return rootFS;
	}

}
