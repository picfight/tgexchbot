package tgexchbot

import (
	"fmt"
	btcclient "github.com/btcsuite/btcd/rpcclient"
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

func NewBTCConnection(config RPCConnectionConfig) (*btcclient.Client, error) {
	file := config.CertificateFile
	fmt.Println("reading: " + file)
	cert, err := ioutil.ReadFile(file)
	lang.CheckErr(err)
	cfg := &btcclient.ConnConfig{
		Host:                 config.Host,
		Endpoint:             config.Endpoint,
		User:                 config.User,
		Pass:                 config.Pass,
		Certificates:         cert,
		DisableAutoReconnect: true,
		HTTPPostMode:         false,
	}
	legacy, err := btcclient.New(cfg, nil)
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
