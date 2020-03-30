package tgexchbot

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
		data.BTCDConfig.Host = "127.0.0.1:9000"
		data.BTCDConfig.User = "u"
		data.BTCDConfig.Pass = "p"
		//data.BTCDConfig.CertificateFile = "~/.btcd/rpc.cert"
	}
	{
		data.PFCDConfig.Host = "127.0.0.1:8000"
		data.PFCDConfig.User = "u"
		data.PFCDConfig.Pass = "p"
		//data.PFCDConfig.CertificateFile = "~/.pfcd/rpc.cert"
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
