
package org.picfight.exchbot.lambda;

import java.io.IOException;

import com.jfixby.scarabei.api.file.File;

public class FilesystemSetup {

	public FilesystemSetup (final File root) {
		final FilesystemSetup setup = this;
		setup.Root = root;
		setup.Orders = setup.Root.child("orders");
		setup.Newo = setup.Orders.child("new");
		setup.Executed = setup.Orders.child("executed");
		setup.Expired = setup.Orders.child("expired");
		setup.Processing = setup.Orders.child("processing");
		setup.Error = setup.Processing.child("error");
		setup.NoEnoughBTC = setup.Processing.child("no_btc");
		setup.NoEnoughPFC = setup.Processing.child("no_pfc");
	}

	public File Root;
	public File Orders;

	public File Newo;
	public File Executed;
	public File Expired;
	public File Processing;

	public File Error;
	public File NoEnoughBTC;
	public File NoEnoughPFC;

	public void deploy () throws IOException {
		final FilesystemSetup setup = this;
		setup.Newo.makeFolder();
		setup.Executed.makeFolder();
		setup.Expired.makeFolder();
		setup.Error.makeFolder();
		setup.NoEnoughBTC.makeFolder();
		setup.NoEnoughPFC.makeFolder();
	}

}
