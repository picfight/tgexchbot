package server

import (
	"encoding/json"
	"fmt"
	"github.com/jfixby/coin"
	"github.com/jfixby/pin"
	"github.com/jfixby/pin/lang"
	"github.com/picfight/pfcd/dcrjson"
	"github.com/picfight/picfightcoin"
	"github.com/picfight/tgexchbot/cfg"
	"github.com/picfight/tgexchbot/connect"
	"io"
	"log"
	"net/http"
	"path"
	"time"
)

type HttpsServer struct {
	config *cfg.ConfigJson
	//handler func(s *HttpsServer) (w http.ResponseWriter, r *http.Request)
}

func (s HttpsServer) Start() {
	http.HandleFunc("/", s.Handler)

	pin.D(fmt.Sprintf("Starting server at port: %v", s.config.ServerConfig.Port))

	// Use ListenAndServeTLS() instead of ListenAndServe() which accepts two extra parameters.
	// We need to specify both the certificate file and the key file (which we've named
	// https-server.crt and https-server.key).
	err := http.ListenAndServeTLS(fmt.Sprintf(":%v", s.config.ServerConfig.Port), s.config.ServerConfig.CertificateFile, s.config.ServerConfig.CertificateKeyFile, nil)
	if err != nil {
		log.Fatal(err)
	}
}

type HttpsServerConfig struct {
	Port               int    `json: "Port"`
	AccessKey          string `json: "accessKey"`
	CertificateFile    string `json: "certfile"`
	CertificateKeyFile string `json: "certfile_key"`
}

func NewServer(cfg *cfg.ConfigJson) *HttpsServer {
	return &HttpsServer{config: cfg,}
}

func (s *HttpsServer) Handler(w http.ResponseWriter, r *http.Request) {
	uri := r.RequestURI
	_, command := path.Split(uri)
	//pin.D("dir", dir)
	pin.D("command", command)
	//params := r.URL.Query()
	//pin.D("params", params)

	//post := r.PostForm
	//pin.D("post", post)

	//pin.D("header", r.Header)

	access_key := r.Header.Get("Access_key")
	pin.D("access_key", access_key)

	responseString := s.processRequest(command, access_key, r.Header)

	w.Header().Add("Content-Type", "application/json")
	io.WriteString(w, responseString)
}

func (s *HttpsServer) processRequest(command string, key string, params http.Header) string {
	if command == "rate" {
		return s.processRate()
	}

	return `{"status":"ok"}`
}

func (s *HttpsServer) processRate() string {
	rate := Rate{}
	{
		s := picfightcoin.PicFightCoinSubsidy()
		now := int64(time.Now().Unix())
		launchTime := int64(1573134568)
		diff := now - launchTime
		blocks := float64(diff) * 1.0 / (5 * 60)
		height := int64(blocks)
		amount := coin.Amount{AtomsValue: s.EstimateSupply(height)}
		rate.CirculatingSupplyCoins = amount.ToCoins()
	}
	{
		client, err := connect.PFCWallet(s.config)
		lang.CheckErr(err)
		br, err := client.GetBalance("default")
		lang.CheckErr(err)
		pin.D("PFC balance", br)

		balance, err := client.GetBalance("default")
		lang.CheckErr(err)
		client.Disconnect()

		accBalance := findAccount("default", balance)
		rate.AvailablePFC = accBalance.Spendable
	}
	{
		client, err := connect.BTCWallet(s.config)
		lang.CheckErr(err)
		br, err := client.GetBalance("default")
		lang.CheckErr(err)
		pin.D("BTC balance", br)

		balance, err := client.GetBalance("default")
		lang.CheckErr(err)
		client.Disconnect()

		btcAmount := balance.ToBTC()
		pin.D("   av btc amount", btcAmount)

		pfcAmount := rate.CirculatingSupplyCoins
		pin.D("total pfc amount", pfcAmount)

		rate.PFC2BTC = pfcAmount / btcAmount
	}

	return toJson(rate)
}

func toJson(v interface{}) string {
	bytes, err := json.MarshalIndent(v, "", "	")
	lang.CheckErr(err)
	return string(bytes)
}

func findAccount(name string, l *dcrjson.GetBalanceResult) *dcrjson.GetAccountBalanceResult {
	for _, value := range l.Balances {
		if name == value.AccountName {
			return &value
		}
	}
	return nil
}
