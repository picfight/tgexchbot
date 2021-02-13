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
		server.PrintPFCBallance(client, "*", false)
		server.PrintPFCBallance(client, OutputWalletAccountName, true)

		client.Disconnect()
	}
	{
		client, err := connect.DCRWallet(conf)
		lang.CheckErr(err)

		OutputWalletAccountName := conf.DCRWalletConfig.OutputWalletAccountName
		server.PrintDCRBallance(client, "*", false)
		server.PrintDCRBallance(client, OutputWalletAccountName, true)

		client.Disconnect()
	}
	pin.D("Deploy server...")
	{
		srv := server.NewServer(conf)
		srv.Start()
	}

}





