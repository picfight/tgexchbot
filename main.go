package main

import (
	"github.com/jfixby/pin"
	"github.com/jfixby/pin/lang"
	"github.com/picfight/tgexchbot/cfg"
	"github.com/picfight/tgexchbot/connect"
	"github.com/picfight/tgexchbot/server"
	"path/filepath"
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
		client, err := connect.BTCWallet(conf)
		lang.CheckErr(err)
		br, err := client.GetBalance("default")
		lang.CheckErr(err)
		pin.D("BTC balance", br)

		//address, err := client.GetNewAddress("default")
		//lang.CheckErr(err)
		//pin.D("new address", address)

		client.Disconnect()
	}

	{
		client, err := connect.DCRWallet(conf)
		lang.CheckErr(err)
		br, err := client.GetBalance("default")
		lang.CheckErr(err)
		pin.D("DCR balance", br)

		//address, err := client.GetNewAddress("default")
		//lang.CheckErr(err)
		//pin.D("new address", address)

		client.Disconnect()
	}
	{
		client, err := connect.PFCWallet(conf)
		lang.CheckErr(err)
		br, err := client.GetBalance("default")
		lang.CheckErr(err)
		pin.D("PFC balance", br)

		//address, err := client.GetNewAddress("default")
		//lang.CheckErr(err)
		//pin.D("new address", address)

		client.Disconnect()
	}
	pin.D("Deploy server...")
	{
		srv := server.NewServer(conf.ServerConfig)
		srv.Start()
	}

}
