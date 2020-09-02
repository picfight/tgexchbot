package main

import (
	"os"
	"path/filepath"

	"github.com/jfixby/pin"
	"github.com/jfixby/pin/lang"
	"github.com/picfight/tgexchbot/cfg"
	"github.com/picfight/tgexchbot/connect"
	"github.com/picfight/tgexchbot/server"
)

const BTCWKEY = "BTCWKEY"
const PFCWKEY = "PFCWKEY"
const DCRWKEY = "DCRWKEY"

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
		client, err := connect.BTCWallet(conf)
		lang.CheckErr(err)

		OutputWalletAccountName := conf.BTCWalletConfig.OutputWalletAccountName

		pin.D("Checking BTC account", OutputWalletAccountName)
		key := os.Getenv(BTCWKEY)
		err = client.WalletPassphrase(key, 10)
		lang.CheckErr(err)
		addr, err := client.GetAccountAddress(OutputWalletAccountName)
		if err != nil {
			pin.D("Creating BTC account", OutputWalletAccountName)
			err := client.CreateNewAccount(OutputWalletAccountName)
			lang.CheckErr(err)
		} else {
			pin.D("BTC exchange address", addr)
		}

		br, err := client.GetBalance(OutputWalletAccountName)
		lang.CheckErr(err)
		pin.D("BTC balance", br)

		client.Disconnect()
	}

	{
		client, err := connect.PFCWallet(conf)
		lang.CheckErr(err)

		OutputWalletAccountName := conf.PFCWalletConfig.OutputWalletAccountName

		pin.D("Checking PFC account", OutputWalletAccountName)
		key := os.Getenv(PFCWKEY)
		err = client.WalletPassphrase(key, 10)
		lang.CheckErr(err)
		addr, err := client.GetAccountAddress(OutputWalletAccountName)
		if err != nil {
			pin.D("Creating PFC account", OutputWalletAccountName)
			err := client.CreateNewAccount(OutputWalletAccountName)
			lang.CheckErr(err)
		} else {
			pin.D("BTC exchange address", addr)
		}

		br, err := client.GetBalance(OutputWalletAccountName)
		lang.CheckErr(err)
		pin.D("PFC balance", br)

		client.Disconnect()
	}

	// {
	// 	client, err := connect.DCRWallet(conf)
	// 	lang.CheckErr(err)

	// 	OutputWalletAccountName := conf.DCRWalletConfig.OutputWalletAccountName

	// 	pin.D("Checking DCR account", OutputWalletAccountName)
	// 	key := os.Getenv(DCRWKEY)
	// 	err = client.WalletPassphrase(key, 10)
	// 	lang.CheckErr(err)
	// 	_, err = client.GetAccountAddress(OutputWalletAccountName)
	// 	if err != nil {
	// 		pin.D("Creating DCR account", OutputWalletAccountName)
	// 		err := client.CreateNewAccount(OutputWalletAccountName)
	// 		lang.CheckErr(err)
	// 	}

	// 	br, err := client.GetBalance(OutputWalletAccountName)
	// 	lang.CheckErr(err)
	// 	pin.D("DCR balance", br)

	// 	client.Disconnect()
	// }
	pin.D("Deploy server...")
	{
		srv := server.NewServer(conf)
		srv.Start()
	}

}
