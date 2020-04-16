
package org.picfight.exchbot.lambda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfixby.scarabei.api.collections.Map;
import com.jfixby.scarabei.api.err.Err;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.io.IO;
import com.jfixby.scarabei.api.json.Json;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.names.ID;
import com.jfixby.scarabei.api.sys.settings.ExecutionMode;
import com.jfixby.scarabei.api.sys.settings.SystemSettings;
import com.jfixby.scarabei.aws.api.AWSCredentialsViaSystemSettingsProvider;
import com.jfixby.scarabei.aws.api.s3.S3;
import com.jfixby.scarabei.aws.api.s3.S3Component;
import com.jfixby.scarabei.aws.api.s3.S3FileSystem;
import com.jfixby.scarabei.aws.api.s3.S3FileSystemConfig;
import com.jfixby.scarabei.aws.desktop.s3.DesktopS3;
import com.jfixby.scarabei.red.desktop.ScarabeiDesktop;

public class LambdaEntryPoint implements RequestStreamHandler {
	private static FilesystemSetup rootFS;

	static {
		ScarabeiDesktop.deploy();
		S3.installComponent(new DesktopS3());
		deployFS();
	}

	@Override
	public void handleRequest (final InputStream input, final OutputStream output, final Context context) {
		try {
			final com.jfixby.scarabei.api.io.InputStream is = IO.newInputStream( () -> input);
			is.open();
			final String data = is.readAllToString();
			is.close();
			L.d("DATA", data);

			final Data dt = Json.deserializeFromString(Data.class, data);
			final ObjectMapper objectReader = new ObjectMapper();
			final Update update = objectReader.readValue(dt.body, Update.class);
// update = objectReader.readValue(input, );
			L.d("Update @ '" + getFormattedTimestamp(update) + "' : " + update);
			L.d("Starting handling update " + update.getUpdateId());
			this.handleUpdate(update);
			L.d("Finished handling update " + update.getUpdateId());

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

	private void handleUpdate (final Update update) {
		if (update.getMessage() == null) {
			return;
		}

		final ChatRequestResponse requesthandler = new ChatRequestResponse();
		requesthandler.input = this.updatetoTelegramUpdate(update);

		if (ChatBotActionHandler.handler == null) {
			ChatBotActionHandler.handler = new ChatBotActionHandler(false);
			SystemSettings.setExecutionMode(ExecutionMode.RELEASE_CANDIDATE);
		}

		ChatBotActionHandler.handler.handleRequest(requesthandler, rootFS);

		L.d("requesthandler", requesthandler);

	}

	private TelegramUpdate updatetoTelegramUpdate (final Update update) {
		L.d("debug", update);

		final TelegramUpdate u = new TelegramUpdate();
		if (update.hasMessage()) {
			final Message msg = update.getMessage();
			if (msg.hasText()) {
				final String text = msg.getText();
				u.message.text = text;
				u.message.hasText = true;
			}
			u.message.chatID = update.getMessage().getChatId();

			u.message.from.firstName = update.getMessage().getFrom().getFirstName();
			u.message.from.lastName = update.getMessage().getFrom().getLastName();
			u.message.from.userName = update.getMessage().getFrom().getUserName();
		}

		return u;
	}

	private static String getFormattedTimestamp (final Update update) {
		if (update.getMessage() != null) {
			final Instant instant = Instant.ofEpochSecond(update.getMessage().getDate());
			return DateTimeFormatter.ISO_INSTANT.format(instant);
		}
		return "<unknown date>";
	}

	public static void init () {
	}

	public static FilesystemSetup FS () {
		return rootFS;
	}

}
