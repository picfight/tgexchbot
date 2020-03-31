package cfg

import (
	"encoding/json"
	"github.com/btcsuite/btcutil"
	"github.com/jfixby/pin/lang"
	"github.com/picfight/pfcd/dcrutil"
	"io/ioutil"
	"path/filepath"
)

type ConfigJson struct {
	BTCDConfig      RPCConfig `json: "btcdconfig"`
	PFCDConfig      RPCConfig `json: "pfcdconfig"`
	BTCWalletConfig RPCConfig `json: "btcwalletconfig"`
	PFCWalletConfig RPCConfig `json: "pfcwalletconfig"`
}

type RPCConfig struct {
	Host            string `json: "host"`
	User            string `json: "user"`
	Pass            string `json: "password"`
	CertificateFile string `json: "certfile"`
}

func ReadCfgFile(filePath string) (*ConfigJson, error) {
	file, err := ioutil.ReadFile(filePath)
	lang.CheckErr(err)

	data := &ConfigJson{}
	err = json.Unmarshal([]byte(file), &data)

	lang.CheckErr(err)

	{
		//defaultConfigFilename := "btcd.conf"
		//defaultDataDirname := "data"
		defaultHomeDir := btcutil.AppDataDir("btcd", false)
		//defaultConfigFile := filepath.Join(defaultHomeDir, defaultConfigFilename)
		//defaultDataDir := filepath.Join(defaultHomeDir, defaultDataDirname)
		//defaultRPCKeyFile := filepath.Join(defaultHomeDir, "rpc.key")
		defaultRPCCertFile := filepath.Join(defaultHomeDir, "rpc.cert")

		if data.BTCDConfig.CertificateFile == "" {
			data.BTCDConfig.CertificateFile = defaultRPCCertFile
		}
	}

	{
		//defaultConfigFilename := "btcd.conf"
		//defaultDataDirname := "data"
		defaultHomeDir := dcrutil.AppDataDir("pfcd", false)
		//defaultConfigFile := filepath.Join(defaultHomeDir, defaultConfigFilename)
		//defaultDataDir := filepath.Join(defaultHomeDir, defaultDataDirname)
		//defaultRPCKeyFile := filepath.Join(defaultHomeDir, "rpc.key")
		defaultRPCCertFile := filepath.Join(defaultHomeDir, "rpc.cert")

		if data.PFCDConfig.CertificateFile == "" {
			data.PFCDConfig.CertificateFile = defaultRPCCertFile
		}
	}

	{
		//defaultConfigFilename := "btcd.conf"
		//defaultDataDirname := "data"
		defaultHomeDir := btcutil.AppDataDir("btcwallet", false)
		//defaultConfigFile := filepath.Join(defaultHomeDir, defaultConfigFilename)
		//defaultDataDir := filepath.Join(defaultHomeDir, defaultDataDirname)
		//defaultRPCKeyFile := filepath.Join(defaultHomeDir, "rpc.key")
		defaultRPCCertFile := filepath.Join(defaultHomeDir, "rpc.cert")

		if data.BTCWalletConfig.CertificateFile == "" {
			data.BTCWalletConfig.CertificateFile = defaultRPCCertFile
		}
	}

	{
		//defaultConfigFilename := "btcd.conf"
		//defaultDataDirname := "data"
		defaultHomeDir := dcrutil.AppDataDir("pfcwallet", false)
		//defaultConfigFile := filepath.Join(defaultHomeDir, defaultConfigFilename)
		//defaultDataDir := filepath.Join(defaultHomeDir, defaultDataDirname)
		//defaultRPCKeyFile := filepath.Join(defaultHomeDir, "rpc.key")
		defaultRPCCertFile := filepath.Join(defaultHomeDir, "rpc.cert")

		if data.PFCWalletConfig.CertificateFile == "" {
			data.PFCWalletConfig.CertificateFile = defaultRPCCertFile
		}
	}

	return data, nil

}
