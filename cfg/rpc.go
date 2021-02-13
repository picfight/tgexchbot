package cfg

import (
	"fmt"

	dcrclient "github.com/decred/dcrd/rpcclient"
	"github.com/jfixby/pin/lang"
	pfcclient "github.com/picfight/pfcd/rpcclient"
	"io/ioutil"
)

type RPCConnectionConfig struct {
	Host            string
	Endpoint        string
	User            string
	Pass            string
	CertificateFile string
}

func NewDCRConnection(config RPCConnectionConfig) (*dcrclient.Client, error) {
	file := config.CertificateFile
	fmt.Println("reading: " + file)
	cert, err := ioutil.ReadFile(file)
	lang.CheckErr(err)
	cfg := &dcrclient.ConnConfig{
		Host:                 config.Host,
		Endpoint:             config.Endpoint,
		User:                 config.User,
		Pass:                 config.Pass,
		Certificates:         cert,
		DisableAutoReconnect: true,
		HTTPPostMode:         false,
	}
	legacy, err := dcrclient.New(cfg, nil)
	if err != nil {
		return nil, err
	}
	return legacy, nil
}

func NewPFCConnection(config RPCConnectionConfig) (*pfcclient.Client, error) {
	file := config.CertificateFile
	fmt.Println("reading: " + file)
	cert, err := ioutil.ReadFile(file)
	lang.CheckErr(err)
	cfg := &pfcclient.ConnConfig{
		Host:                 config.Host,
		Endpoint:             config.Endpoint,
		User:                 config.User,
		Pass:                 config.Pass,
		Certificates:         cert,
		DisableAutoReconnect: true,
		HTTPPostMode:         false,
	}
	legacy, err := pfcclient.New(cfg, nil)
	if err != nil {
		return nil, err
	}
	return legacy, nil
}
