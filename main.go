package main

import (
	"github.com/jfixby/pin"
	"github.com/jfixby/pin/lang"
	"github.com/picfight/tgexchbot/cfg"
	"path/filepath"
)

func main() {
	//http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
	//	fmt.Fprintf(w, "Hello, %q", html.EscapeString(r.URL.Path))
	//})
	//log.Println("Listening on localhost:8080")
	//log.Fatal(http.ListenAndServe(":8080", nil))
	filePath, err := filepath.Abs("tgexchbot.cfg")
	lang.CheckErr(err)
	{

		conf, err := cfg.ReadCfgFile(filePath)
		lang.CheckErr(err)

		pfcconnect := cfg.RPCConnectionConfig{
			Host:            conf.PFCDConfig.Host,
			User:            conf.PFCDConfig.User,
			Pass:            conf.PFCDConfig.Pass,
			CertificateFile: conf.PFCDConfig.CertificateFile,
		}

		client, err := cfg.NewPFCConnection(pfcconnect)
		lang.CheckErr(err)
		hash, err := client.GetBlockHash(0)
		lang.CheckErr(err)
		pin.D("hash", hash)
	}

}
