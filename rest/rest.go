package main

import (
	"fmt"
	btcclient "github.com/btcsuite/btcd/rpcclient"
	"github.com/jfixby/pin"
	pfcclient "github.com/picfight/pfcd/rpcclient"
	"io/ioutil"
)

func main() {
	//http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
	//	fmt.Fprintf(w, "Hello, %q", html.EscapeString(r.URL.Path))
	//})
	//log.Println("Listening on localhost:8080")
	//log.Fatal(http.ListenAndServe(":8080", nil))

	{
		pfcconnect := RPCConnectionConfig{
			Host:            "127.0.0.1",
			User:            "u",
			Pass:            "p",
			CertificateFile: "~/.pfcd/rpc.cert",
		}

		client, err := NewPFCConnection(pfcconnect)
		pin.CheckTestSetupMalfunction(err)
		hash, err := client.GetBlockHash(0)
		pin.CheckTestSetupMalfunction(err)
		pin.D("hash", hash)
	}

}

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
	pin.CheckTestSetupMalfunction(err)
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
	pin.CheckTestSetupMalfunction(err)
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
