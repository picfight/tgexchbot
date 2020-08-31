package server

import (
	"encoding/json"
	"fmt"
	"os"
	"strconv"
	"strings"

	"github.com/btcsuite/btcd/btcjson"
	btccfg "github.com/btcsuite/btcd/chaincfg"
	"github.com/picfight/pfcd/dcrutil"

	"io"
	"log"
	"net/http"
	"path"
	"time"

	"github.com/btcsuite/btcutil"
	"github.com/jfixby/coin"
	"github.com/jfixby/pin"
	"github.com/jfixby/pin/lang"
	"github.com/picfight/pfcd/dcrjson"
	"github.com/picfight/picfightcoin"
	"github.com/picfight/tgexchbot/cfg"
	"github.com/picfight/tgexchbot/connect"
)

type HttpsServer struct {
	config *cfg.ConfigJson
	//handler func(s *HttpsServer) (w http.ResponseWriter, r *http.Request)
}

const ACCESS_KEY = "TGEXCHBOTKEY"

func (s HttpsServer) Start() {
	pin.D("Check access key...")
	isnotset := checkAccessKey("")
	if isnotset {
		set := os.Getenv(ACCESS_KEY)
		pin.D("System settings:")
		for _, e := range os.Environ() {
			pair := strings.SplitN(e, "=", 2)
			pin.D(pair[0])
		}
		pin.D("")
		pin.D("Access key", set)
		panic(fmt.Sprintf("%v is not set", ACCESS_KEY))
	}

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

func NewServer(cfg *cfg.ConfigJson) *HttpsServer {
	return &HttpsServer{config: cfg}
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
	pin.D("response", responseString)
	io.WriteString(w, responseString)
}

func (s *HttpsServer) processRequest(command string, access_key string, params http.Header) string {
	valid := checkAccessKey(access_key)
	if !valid {
		return `{"error":"Access denied, invalid key"}`
	}

	if command == "rate" {
		return s.processRate()
	}

	if command == "get_balance_btc" {
		btc_address := params["Btc_address"][0]
		account_name := params["Account_name"][0]
		return s.getBalanceBTC(btc_address, account_name)
	}

	if command == "get_balance_pfc" {
		pfc_address := params["Pfc_address"][0]
		account_name := params["Account_name"][0]
		return s.getBalancePFC(pfc_address, account_name)
	}

	if command == "transfer_btc" {
		client_btc_wallet := params["Client_btc_wallet"][0]
		btc_amount, err := strconv.ParseFloat(params["Btc_amount"][0], 64)
		if err != nil {
			return fmt.Sprintf(`{"status":"%v"}`, err)
		}
		return s.TransferBTC(client_btc_wallet, btc_amount, err)
	}

	if command == "transfer_pfc" {
		client_pfc_wallet := params["Client_pfc_wallet"][0]
		pfc_amount, err := strconv.ParseFloat(params["Pfc_amount"][0], 64)
		if err != nil {
			return fmt.Sprintf(`{"status":"%v"}`, err)
		}
		return s.TransferPFC(client_pfc_wallet, pfc_amount, err)
	}

	if command == "new_pfc_address" {
		account_name := params["Account_name"][0]
		return s.obrtainPFCAddress(account_name)
	}

	if command == "new_btc_address" {
		account_name := params["Account_name"][0]
		return s.obrtainBTCAddress(account_name)
	}

	if command == "analyze_string" {
		raw_text := params["Raw_text"]
		//pin.D("Raw_text", raw_text)
		return s.AnalyzeString(raw_text[0])
	}

	return `{"status":"ok"}`
}

func checkAccessKey(received_key string) bool {
	set := os.Getenv(ACCESS_KEY)
	return received_key == set
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
		rate.CirculatingSupplyCoins.Value = amount.ToCoins()
	}
	{
		client, err := connect.PFCWallet(s.config)
		lang.CheckErr(err)
		br, err := client.GetBalance(s.config.PFCWalletConfig.OutputWalletAccountName)
		lang.CheckErr(err)
		pin.D("PFC balance", br)

		balance, err := client.GetBalance(s.config.PFCWalletConfig.OutputWalletAccountName)
		lang.CheckErr(err)
		client.Disconnect()

		accBalance := findAccount(s.config.PFCWalletConfig.OutputWalletAccountName, balance)

		pin.D("PFC accBalance", accBalance)

		rate.AvailablePFC.Value = accBalance.Spendable
	}
	{
		client, err := connect.BTCWallet(s.config)
		lang.CheckErr(err)
		br, err := client.GetBalance(s.config.BTCWalletConfig.OutputWalletAccountName)
		lang.CheckErr(err)
		pin.D("BTC balance", br)

		balance, err := client.GetBalance(s.config.BTCWalletConfig.OutputWalletAccountName)
		lang.CheckErr(err)
		client.Disconnect()

		rate.AvailableBTC.Value = balance.ToBTC()
	}
	{
		client, err := connect.BTCWallet(s.config)
		lang.CheckErr(err)
		br, err := client.GetBalance(s.config.BTCWalletConfig.OutputWalletAccountName)
		lang.CheckErr(err)
		pin.D("BTC balance", br)

		balance, err := client.GetBalance(s.config.BTCWalletConfig.OutputWalletAccountName)
		lang.CheckErr(err)
		client.Disconnect()

		btcAmount := balance.ToBTC()
		pin.D("   av btc amount", btcAmount)

		pfcAmount := rate.CirculatingSupplyCoins
		pin.D("total pfc amount", pfcAmount)

		rate.BTCperPFC = btcAmount / pfcAmount.Value
	}

	{
		rate.ExchangeRate = s.config.ExchangeSettings.ExchangeRate
		rate.ExchangeMargin = s.config.ExchangeSettings.ExchangeMargin
		rate.MinBTCOperation.Value = s.config.ExchangeSettings.MinBTCOperation
	}

	return toJson(rate)
}

func (s HttpsServer) obrtainPFCAddress(walletAccountName string) string {
	address := &AddressString{
		Type: "PFC",
	}
	{
		client, err := connect.PFCWallet(s.config)
		lang.CheckErr(err)

		addressResult, err := client.GetNewAddress(walletAccountName)
		lang.CheckErr(err)
		client.Disconnect()

		address.AddressString = addressResult.String()
	}
	return toJson(address)
}

func (s HttpsServer) obrtainBTCAddress(walletAccountName string) string {
	address := &AddressString{
		Type: "BTC",
	}
	{
		client, err := connect.BTCWallet(s.config)
		lang.CheckErr(err)

		_, err = client.GetAccountAddress(walletAccountName)
		if err != nil {
			err := client.CreateNewAccount(walletAccountName)
			lang.CheckErr(err)
		}

		addressResult, err := client.GetNewAddress(walletAccountName)
		lang.CheckErr(err)
		client.Disconnect()

		address.AddressString = addressResult.String()
	}
	return toJson(address)
}

func (s HttpsServer) AnalyzeString(hextext string) string {
	text := hextext

	pin.D("text", text)

	btcAddress, _ := btcutil.DecodeAddress(text, &btccfg.MainNetParams)
	pfcAddress, _ := dcrutil.DecodeAddress(text)

	result := &StringAnalysis{}

	if btcAddress != nil {
		result.BTCAddress = &AddressString{
			AddressString: btcAddress.String(),
			Type:          "BTC",
		}
	}

	if pfcAddress != nil {
		result.PFCAddress = &AddressString{
			AddressString: pfcAddress.String(),
			Type:          "PFC",
		}
	}

	return toJson(result)
}

func (s HttpsServer) getBalanceBTC(btc_address string, walletAccountName string) string {
	result := BTCBalance{}
	result.BTCAddress.Type = "BTC"
	result.BTCAddress.AddressString = btc_address
	client, err := connect.BTCWallet(s.config)
	lang.CheckErr(err)

	r, err := client.ListUnspent()
	lang.CheckErr(err)
	client.Disconnect()

	b := receivedBTCByAddress(r, btc_address, walletAccountName)

	result.AmountBTC.Value = b
	return toJson(result)
}

func (s HttpsServer) getBalancePFC(pfc_address string, walletAccountName string) string {
	result := PFCBalance{}
	{
		result.PFCAddress.Type = "PFC"
		result.PFCAddress.AddressString = pfc_address
	}
	{
		client, err := connect.PFCWallet(s.config)
		lang.CheckErr(err)

		r, err := client.ListUnspent()
		lang.CheckErr(err)
		client.Disconnect()

		b := receivedPFCByAddress(r, pfc_address, walletAccountName)
		result.AmountPFC.Value = b
	}
	return toJson(result)
}

func (s HttpsServer) TransferBTC(client_btc_wallet string, btc_amount float64, err error) string {
	result := Result{}

	client, err := connect.BTCWallet(s.config)
	lang.CheckErr(err)
	address, err := btcutil.DecodeAddress(client_btc_wallet, &btccfg.MainNetParams)
	lang.CheckErr(err)

	amount, err := btcutil.NewAmount(btc_amount)
	lang.CheckErr(err)

	sendResult, err := client.SendToAddress(address, amount)
	client.Disconnect()

	if err != nil {
		result.Success = false
		result.Error_message = err.Error()
	} else {
		result.Success = true
		result.Btc_transaction_receipt = sendResult.String()
	}
	result.BtcAmount.Value = btc_amount
	return toJson(result)
}

func (s HttpsServer) TransferPFC(client_pfc_wallet string, pfc_amount float64, err error) string {
	result := Result{}

	client, err := connect.PFCWallet(s.config)
	lang.CheckErr(err)
	address, err := dcrutil.DecodeAddress(client_pfc_wallet)
	lang.CheckErr(err)

	amount, err := dcrutil.NewAmount(pfc_amount)
	lang.CheckErr(err)

	sendResult, err := client.SendToAddress(address, amount)
	client.Disconnect()

	if err != nil {
		result.Success = false
		result.Error_message = err.Error()
	} else {
		result.Success = true
		result.Pfc_transaction_receipt = sendResult.String()
	}
	result.PfcAmount.Value = pfc_amount
	return toJson(result)
}

func receivedPFCByAddress(r []dcrjson.ListUnspentResult, address string, acc string) float64 {
	balance := float64(0)
	minConf := int64(1)
	for _, e := range r {
		if e.Account == acc && //
			e.Address == address && //
			//e.Spendable && //
			e.Confirmations >= minConf {
			balance = balance + e.Amount
		}
	}
	return balance
}

func receivedBTCByAddress(r []btcjson.ListUnspentResult, address string, acc string) float64 {
	balance := float64(0)
	minConf := int64(1)
	for _, e := range r {
		if e.Account == acc && //
			e.Address == address && //
			//e.Spendable && //
			e.Confirmations >= minConf {
			balance = balance + e.Amount
		}
	}
	return balance
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
