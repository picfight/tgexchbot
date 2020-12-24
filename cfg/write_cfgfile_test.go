package cfg

import (
	"encoding/json"
	"github.com/jfixby/pin"
	"github.com/jfixby/pin/lang"
	"io/ioutil"
	"os"
	"path/filepath"
	"testing"
)

func TestWriteCfgFile(t *testing.T) {
	filePath, err := filepath.Abs("test_config.json")
	lang.CheckErr(err)

	data := ConfigJson{}
	{
		data.BTCDConfig.Host = "127.0.0.1:18334"
		data.BTCDConfig.User = "u"
		data.BTCDConfig.Pass = "p"
		//data.BTCDConfig.CertificateFile = "~/.btcd/rpc.cert"
	}
	{
		data.PFCDConfig.Host = "127.0.0.1:9709"
		data.PFCDConfig.User = "u"
		data.PFCDConfig.Pass = "p"
		//data.PFCDConfig.CertificateFile = "~/.pfcd/rpc.cert"
	}
	{
		data.DCRDConfig.Host = "127.0.0.1:9109"
		data.DCRDConfig.User = "u"
		data.DCRDConfig.Pass = "p"
		//data.PFCDConfig.CertificateFile = "~/.pfcd/rpc.cert"
	}

	{
		data.BTCWalletConfig.Host = "127.0.0.1:18332"
		data.BTCWalletConfig.User = "u"
		data.BTCWalletConfig.Pass = "p"
		data.BTCWalletConfig.OutputWalletAccountName = "output"
		//data.BTCDConfig.CertificateFile = "~/.btcd/rpc.cert"
	}
	{
		data.PFCWalletConfig.Host = "127.0.0.1:9710"
		data.PFCWalletConfig.User = "u"
		data.PFCWalletConfig.Pass = "p"
		data.PFCWalletConfig.OutputWalletAccountName = "output"
		//data.PFCDConfig.CertificateFile = "~/.pfcd/rpc.cert"
	}
	{
		data.DCRWalletConfig.Host = "127.0.0.1:9110"
		data.DCRWalletConfig.User = "u"
		data.DCRWalletConfig.Pass = "p"
		data.DCRWalletConfig.OutputWalletAccountName = "output"
		//data.PFCDConfig.CertificateFile = "~/.pfcd/rpc.cert"
	}
	{
		data.ServerConfig.Port = 8000
		data.ServerConfig.CertificateFile = "/home/ec2-user/go/src/github.com/picfight/tgexchbot/https-server.crt"
		data.ServerConfig.CertificateKeyFile = "/home/ec2-user/go/src/github.com/picfight/tgexchbot/https-server.key"
	}

	{
		data.ExchangeSettings.ExchangeMargin = 0.11 //11%
		data.ExchangeSettings.ExchangeRate = 0.000001
		data.ExchangeSettings.MinBTCOperation = 0.00001
	}

	bytes, err := json.MarshalIndent(data, "", "	")
	lang.CheckErr(err)

	err = ioutil.WriteFile(filePath, bytes, os.FileMode(777))
	lang.CheckErr(err)

	pin.D("cfg", filePath)

}

func TestReadCfgFile(t *testing.T) {
	filePath, err := filepath.Abs("test_config.json")
	lang.CheckErr(err)
	cfg, err := ReadCfgFile(filePath)
	lang.CheckErr(err)
	pin.S("cfg", cfg)
}
