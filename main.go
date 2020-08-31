package main

import (
	"path/filepath"

	"github.com/jfixby/pin"
	"github.com/jfixby/pin/lang"
	"github.com/picfight/tgexchbot/cfg"
	"github.com/picfight/tgexchbot/connect"
	"github.com/picfight/tgexchbot/server"
)

func main() {
	filePath, err := filepath.Abs("tgexchbot.cfg")
	lang.CheckErr(err)
	conf, err := cfg.ReadCfgFile(filePath)
	lang.CheckErr(err)
	pin.S("conf", conf)
	{
		client, err := connect.BTCD(conf)
		lang.CheckErr(err)
		hash, height, err := client.GetBestBlock()
		lang.CheckErr(err)
		pin.D("best BTC block", hash, height)
		client.Disconnect()
	}
	{
		client, err := connect.DCRD(conf)
		lang.CheckErr(err)
		hash, height, err := client.GetBestBlock()
		lang.CheckErr(err)
		pin.D("best DCR block", hash, height)
		client.Disconnect()
	}
	{
		client, err := connect.PFCD(conf)
		lang.CheckErr(err)
		hash, height, err := client.GetBestBlock()
		lang.CheckErr(err)
		pin.D("best PFC block", hash, height)
		client.Disconnect()
	}
	//-------------------------------------------
	{
		client, err := connect.PFCWallet(conf)
		lang.CheckErr(err)

		OutputWalletAccountName := conf.PFCWalletConfig.OutputWalletAccountName

		_, err = client.GetAccountAddress(OutputWalletAccountName)
		if err != nil {
			err := client.CreateNewAccount(OutputWalletAccountName)
			lang.CheckErr(err)
		}

		br, err := client.GetBalance(OutputWalletAccountName)
		lang.CheckErr(err)
		pin.D("PFC balance", br)

		client.Disconnect()
	}
	{
		client, err := connect.BTCWallet(conf)
		lang.CheckErr(err)

		OutputWalletAccountName := conf.BTCWalletConfig.OutputWalletAccountName

		_, err = client.GetAccountAddress(OutputWalletAccountName)
		if err != nil {
			err := client.CreateNewAccount(OutputWalletAccountName)
			lang.CheckErr(err)
		}

		br, err := client.GetBalance(OutputWalletAccountName)
		lang.CheckErr(err)
		pin.D("BTC balance", br)

		client.Disconnect()
	}

	{
		client, err := connect.DCRWallet(conf)
		lang.CheckErr(err)

		OutputWalletAccountName := conf.DCRWalletConfig.OutputWalletAccountName

		_, err = client.GetAccountAddress(OutputWalletAccountName)
		if err != nil {
			err := client.CreateNewAccount(OutputWalletAccountName)
			lang.CheckErr(err)
		}

		br, err := client.GetBalance(OutputWalletAccountName)
		lang.CheckErr(err)
		pin.D("DCR balance", br)

		client.Disconnect()
	}
	pin.D("Deploy server...")
	{
		srv := server.NewServer(conf)
		srv.Start()
	}

}
