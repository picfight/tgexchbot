package cfg

import (
	"encoding/json"
	"github.com/jfixby/pin/lang"
	"github.com/picfight/pfcd/dcrutil"
	"io/ioutil"
	"path/filepath"
)

type ConfigJson struct {
	PFCDConfig       RPCConfig         `json: "pfcdconfig"`
	DCRDConfig       RPCConfig         `json: "dcrdconfig"`
	PFCWalletConfig  RPCConfig         `json: "pfcwalletconfig"`
	DCRWalletConfig  RPCConfig         `json: "dcrwalletconfig"`
	ServerConfig     HttpsServerConfig `json: "serverconfig"`
}


type HttpsServerConfig struct {
	Port int `json: "Port"`
	// AccessKey          string `json: "accessKey"`
	CertificateFile    string `json: "certfile"`
	CertificateKeyFile string `json: "certfile_key"`
}

type RPCConfig struct {
	Host                    string `json: "host"`
	User                    string `json: "user"`
	Pass                    string `json: "password"`
	CertificateFile         string `json: "certfile"`
	OutputWalletAccountName string `json: "wallet_account_name"`
}

func ReadCfgFile(filePath string) (*ConfigJson, error) {
	file, err := ioutil.ReadFile(filePath)
	lang.CheckErr(err)

	data := &ConfigJson{}
	err = json.Unmarshal([]byte(file), &data)

	lang.CheckErr(err)

	{
		defaultHomeDir := dcrutil.AppDataDir("pfcd", false)
		defaultRPCCertFile := filepath.Join(defaultHomeDir, "rpc.cert")

		if data.PFCDConfig.CertificateFile == "" {
			data.PFCDConfig.CertificateFile = defaultRPCCertFile
		}
	}

	{
		defaultHomeDir := dcrutil.AppDataDir("dcrd", false)
		defaultRPCCertFile := filepath.Join(defaultHomeDir, "rpc.cert")

		if data.DCRDConfig.CertificateFile == "" {
			data.DCRDConfig.CertificateFile = defaultRPCCertFile
		}
	}

	{
		defaultHomeDir := dcrutil.AppDataDir("pfcwallet", false)
		defaultRPCCertFile := filepath.Join(defaultHomeDir, "rpc.cert")

		if data.PFCWalletConfig.CertificateFile == "" {
			data.PFCWalletConfig.CertificateFile = defaultRPCCertFile
		}
	}

	{
		defaultHomeDir := dcrutil.AppDataDir("dcrwallet", false)
		defaultRPCCertFile := filepath.Join(defaultHomeDir, "rpc.cert")

		if data.DCRWalletConfig.CertificateFile == "" {
			data.DCRWalletConfig.CertificateFile = defaultRPCCertFile
		}
	}

	return data, nil

}
