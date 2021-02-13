package main

import (

	pfcclient "github.com/picfight/pfcd/rpcclient"
	btcclient "github.com/stevenroose/go-bitcoin-core-rpc"
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
		hash, err := client.GetBestBlockHash()
		lang.CheckErr(err)
		height, err := client.GetBlockCount()
		lang.CheckErr(err)
		pin.D("best BTC block", hash, height)
		client.Shutdown()
	}
	//{
	//	client, err := connect.DCRD(conf)
	//	lang.CheckErr(err)
	//	hash, height, err := client.GetBestBlock()
	//	lang.CheckErr(err)
	//	pin.D("best DCR block", hash, height)
	//	client.Disconnect()
	//}
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
		printBTCBallance(client, "*", false)
		printBTCBallance(client, OutputWalletAccountName, true)

		client.Shutdown()
	}

	{
		client, err := connect.PFCWallet(conf)
		lang.CheckErr(err)

		OutputWalletAccountName := conf.PFCWalletConfig.OutputWalletAccountName
		printPFCBallance(client, "*", false)
		printPFCBallance(client, OutputWalletAccountName, true)

		client.Disconnect()
	}

	{
		//client, err := connect.DCRWallet(conf)
		//lang.CheckErr(err)
		//
		//OutputWalletAccountName := conf.DCRWalletConfig.OutputWalletAccountName
		//printDCRBallance(client, "*", false)
		//printDCRBallance(client, OutputWalletAccountName, true)
		//
		//client.Disconnect()
	}
	pin.D("Deploy server...")
	{
		srv := server.NewServer(conf)
		srv.Start()
	}

}


func printBTCBallance(client *btcclient.Client, s string, getAddress bool) {
	pin.D("Checking BTC account", s)
	//key := os.Getenv(BTCWKEY)
	//err := client.WalletPassphrase(key, 10)
	//lang.CheckErr(err)
	if getAddress {
		addr, err := client.GetAccountAddress(s)
		if err != nil {
			pin.D("Creating BTC account", s)
			err := client.CreateNewAccount(s)
			lang.CheckErr(err)
		} else {
			pin.D("BTC exchange address", addr)
		}
	}
	br, err := client.GetBalance(s)
	lang.CheckErr(err)
	pin.D("BTC balance", br)
}

func printPFCBallance(client *pfcclient.Client, s string, getAddress bool) {
	pin.D("Checking PFC account", s)
	key := os.Getenv(PFCWKEY)
	err := client.WalletPassphrase(key, 10)
	lang.CheckErr(err)
	if getAddress {
		addr, err := client.GetAccountAddress(s)
		if err != nil {
			pin.D("Creating PFC account", s)
			err := client.CreateNewAccount(s)
			lang.CheckErr(err)
		} else {
			pin.D("PFC exchange address", addr)
		}
	}
	br, err := client.GetBalance(s)
	lang.CheckErr(err)
	pin.D("PFC balance", br)
}


//func printDCRBallance(client *dcrclient.Client, s string, getAddress bool) {
//	pin.D("Checking DCR account", s)
//	key := os.Getenv(DCRWKEY)
//	err := client.WalletPassphrase(key, 10)
//	lang.CheckErr(err)
//	if getAddress {
//		addr, err := client.GetAccountAddress(s)
//		if err != nil {
//			pin.D("Creating DCR account", s)
//			err := client.CreateNewAccount(s)
//			lang.CheckErr(err)
//		} else {
//			pin.D("DCR exchange address", addr)
//		}
//	}
//	br, err := client.GetBalance(s)
//	lang.CheckErr(err)
//	pin.D("DCR balance", br)
//}
