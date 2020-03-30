package main

import (
	"github.com/jfixby/pin"
	"github.com/jfixby/pin/lang"
	"github.com/picfight/tgexchbot/tgexchbot"
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

		cfg, err := tgexchbot.ReadCfgFile(filePath)
		lang.CheckErr(err)

		pfcconnect := tgexchbot.RPCConnectionConfig{
			Host:            cfg.PFCDConfig.Host,
			User:            cfg.PFCDConfig.User,
			Pass:            cfg.PFCDConfig.Pass,
			CertificateFile: cfg.PFCDConfig.CertificateFile,
		}

		client, err := tgexchbot.NewPFCConnection(pfcconnect)
		lang.CheckErr(err)
		hash, err := client.GetBlockHash(0)
		lang.CheckErr(err)
		pin.D("hash", hash)
	}

}
