package connect

import (
	dcrclient "github.com/decred/dcrd/rpcclient"
	pfcclient "github.com/picfight/pfcd/rpcclient"

	"github.com/jfixby/pin"
	"github.com/picfight/tgexchbot/cfg"
)

func DCRD(conf *cfg.ConfigJson) (*dcrclient.Client, error) {
	pin.D("connect DCRD RPC")
	dcrconnect := cfg.RPCConnectionConfig{
		Host:            conf.DCRDConfig.Host,
		User:            conf.DCRDConfig.User,
		Pass:            conf.DCRDConfig.Pass,
		CertificateFile: conf.DCRDConfig.CertificateFile,
		Endpoint:        "ws",
	}

	return cfg.NewDCRConnection(dcrconnect)
}

func PFCD(conf *cfg.ConfigJson) (*pfcclient.Client, error) {
	pin.D("connect PFCD RPC")
	pfcconnect := cfg.RPCConnectionConfig{
		Host:            conf.PFCDConfig.Host,
		User:            conf.PFCDConfig.User,
		Pass:            conf.PFCDConfig.Pass,
		CertificateFile: conf.PFCDConfig.CertificateFile,
		Endpoint:        "ws",
	}

	return cfg.NewPFCConnection(pfcconnect)
}

func DCRWallet(conf *cfg.ConfigJson) (*dcrclient.Client, error) {
	pin.D("connect DCRWallet RPC")
	dcrconnect := cfg.RPCConnectionConfig{
		Host:            conf.DCRWalletConfig.Host,
		User:            conf.DCRWalletConfig.User,
		Pass:            conf.DCRWalletConfig.Pass,
		CertificateFile: conf.DCRWalletConfig.CertificateFile,
		Endpoint:        "ws",
	}

	return cfg.NewDCRConnection(dcrconnect)
}

func PFCWallet(conf *cfg.ConfigJson) (*pfcclient.Client, error) {
	pin.D("connect PFCWallet RPC")
	pfcconnect := cfg.RPCConnectionConfig{
		Host:            conf.PFCWalletConfig.Host,
		User:            conf.PFCWalletConfig.User,
		Pass:            conf.PFCWalletConfig.Pass,
		CertificateFile: conf.PFCWalletConfig.CertificateFile,
		Endpoint:        "ws",
	}

	return cfg.NewPFCConnection(pfcconnect)
}
