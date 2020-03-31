package main

import (
	"github.com/jfixby/pin"
	"github.com/jfixby/pin/lang"
	"github.com/picfight/tgexchbot/cfg"
	"path/filepath"
)

func main() {
	filePath, err := filepath.Abs("tgexchbot.cfg")
	lang.CheckErr(err)
	conf, err := cfg.ReadCfgFile(filePath)
	lang.CheckErr(err)
	pin.S("conf", conf)

	{
		pin.D("connect PFCD RPC")
		pfcconnect := cfg.RPCConnectionConfig{
			Host:            conf.PFCDConfig.Host,
			User:            conf.PFCDConfig.User,
			Pass:            conf.PFCDConfig.Pass,
			CertificateFile: conf.PFCDConfig.CertificateFile,
			Endpoint:        "ws",
		}

		client, err := cfg.NewPFCConnection(pfcconnect)
		lang.CheckErr(err)
		hash, height, err := client.GetBestBlock()
		lang.CheckErr(err)
		pin.D("best PFC block", hash, height)
	}
	{
		pin.D("connect BTCD RPC")
		btcconnect := cfg.RPCConnectionConfig{
			Host:            conf.BTCDConfig.Host,
			User:            conf.BTCDConfig.User,
			Pass:            conf.BTCDConfig.Pass,
			CertificateFile: conf.BTCDConfig.CertificateFile,
			Endpoint:        "ws",
		}

		client, err := cfg.NewBTCConnection(btcconnect)
		lang.CheckErr(err)
		hash, height, err := client.GetBestBlock()
		lang.CheckErr(err)
		pin.D("best BTC block", hash, height)
	}
	{
		pin.D("connect PFCWallet RPC")
		pfcconnect := cfg.RPCConnectionConfig{
			Host:            conf.PFCWalletConfig.Host,
			User:            conf.PFCWalletConfig.User,
			Pass:            conf.PFCWalletConfig.Pass,
			CertificateFile: conf.PFCWalletConfig.CertificateFile,
			Endpoint:        "ws",
		}

		client, err := cfg.NewPFCConnection(pfcconnect)
		lang.CheckErr(err)
		br, err := client.GetBalance("default")
		lang.CheckErr(err)
		pin.D("PFC balance", br)
	}
	{
		pin.D("connect BTCWallet RPC")
		btcconnect := cfg.RPCConnectionConfig{
			Host:            conf.BTCWalletConfig.Host,
			User:            conf.BTCWalletConfig.User,
			Pass:            conf.BTCWalletConfig.Pass,
			CertificateFile: conf.BTCWalletConfig.CertificateFile,
			Endpoint:        "ws",
		}

		client, err := cfg.NewBTCConnection(btcconnect)
		lang.CheckErr(err)
		br, err := client.GetBalance("default")
		lang.CheckErr(err)
		pin.D("BTC balance", br)
	}
}
