package server

import (
	"encoding/json"
	"fmt"
	"github.com/btcsuite/btcd/btcjson"
	btccfg "github.com/btcsuite/btcd/chaincfg"
	dcrutil "github.com/decred/dcrd/dcrutil"
	pfcutil "github.com/picfight/pfcd/dcrutil"
	"os"
	"strconv"
	"strings"
	"sync"

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
const BTCWKEY = "BTCWKEY"
const PFCWKEY = "PFCWKEY"
const DCRWKEY = "DCRWKEY"

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
	//pin.D("access_key", access_key)

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

	if command == "trade_pfc" {
		amountString := params["Pfc_amount"][0]
		pfc_amount, err := strconv.ParseFloat(amountString, 64)
		lang.CheckErr(err)
		op_string := params["Operation"][0]
		op := op_string == "BUY" //true
		getquote_string := params["Getquote"][0]
		getquote := getquote_string == "true"

		return s.tradePFC(pfc_amount, op, getquote)
	}

	if command == "get_balance_pfc" {
		pfc_address := params["Pfc_address"][0]
		min_confirmations_string := params["Min_confirmations"][0]
		min_confirmations, err := strconv.ParseInt(min_confirmations_string, 10, 64)
		lang.CheckErr(err)
		return s.getBalancePFC(pfc_address, int(min_confirmations))
	}

	if command == "get_balance_dcr" {
		pfc_address := params["Dcr_address"][0]
		min_confirmations_string := params["Min_confirmations"][0]
		min_confirmations, err := strconv.ParseInt(min_confirmations_string, 10, 64)
		lang.CheckErr(err)
		return s.getBalanceDCR(pfc_address, int(min_confirmations))
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
		PFC_FromAccountAddress := params["Pfc_fromaccountaddress"][0]
		PFC_Amount_string := params["Pfc_amount"][0]
		PFC_Amount, err := strconv.ParseFloat(PFC_Amount_string, 64)
		if err != nil {
			return fmt.Sprintf(`{"status":"%v"}`, err)
		}
		PFC_ToAddress := params["Pfc_toaddress"][0]
		return s.TransferPFC(PFC_FromAccountAddress, PFC_ToAddress, PFC_Amount)
	}

	if command == "transfer_dcr" {
		DCR_FromAccountAddress := params["Dcr_fromaccountaddress"][0]
		DCR_Amount_string := params["Dcr_amount"][0]
		DCR_Amount, err := strconv.ParseFloat(DCR_Amount_string, 64)
		if err != nil {
			return fmt.Sprintf(`{"status":"%v"}`, err)
		}
		DCR_ToAddress := params["Dcr_toaddress"][0]
		return s.TransferDCR(DCR_FromAccountAddress, DCR_ToAddress, DCR_Amount)
	}

	if command == "new_pfc_address" {
		account_name := params["Account_name"][0]
		return s.obrtainPFCAddress(account_name)
	}

	if command == "new_btc_address" {
		account_name := params["Account_name"][0]
		return s.obrtainBTCAddress(account_name)
	}

	if command == "new_dcr_address" {
		account_name := params["Account_name"][0]
		return s.obrtainDCRAddress(account_name)
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

		//rate.BTCperPFC = btcAmount / pfcAmount.Value
	}

	{
		//rate.ExchangeRate = s.config.ExchangeSettings.ExchangeRate
		//rate.ExchangeMargin = s.config.ExchangeSettings.ExchangeMargin
		//rate.MinBTCOperation.Value = s.config.ExchangeSettings.MinBTCOperation
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

		pin.D("Checking PFC account", walletAccountName)
		key := os.Getenv(PFCWKEY)
		err = client.WalletPassphrase(key, 10000000)
		lang.CheckErr(err)
		_, err = client.GetAccountAddress(walletAccountName)
		if err != nil {
			pin.D("Error", err)
			pin.D("Creating PFC account", walletAccountName)
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

func (s HttpsServer) obrtainBTCAddress(walletAccountName string) string {
	address := &AddressString{
		Type: "BTC",
	}
	{
		client, err := connect.BTCWallet(s.config)
		lang.CheckErr(err)

		pin.D("Checking BTC account", walletAccountName)
		key := os.Getenv(BTCWKEY)
		err = client.WalletPassphrase(key, 10000000)
		lang.CheckErr(err)
		_, err = client.GetAccountAddress(walletAccountName)
		if err != nil {
			pin.D("Error", err)
			pin.D("Creating BTC account", walletAccountName)
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

func (s HttpsServer) obrtainDCRAddress(walletAccountName string) string {
	address := &AddressString{
		Type: "DCR",
	}
	{
		client, err := connect.DCRWallet(s.config)
		lang.CheckErr(err)

		pin.D("Checking DCR account", walletAccountName)
		key := os.Getenv(DCRWKEY)
		err = client.WalletPassphrase(key, 10000000)
		lang.CheckErr(err)
		_, err = client.GetAccountAddress(walletAccountName)
		if err != nil {
			pin.D("Error", err)
			pin.D("Creating DCR account", walletAccountName)
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
	pfcAddress, _ := pfcutil.DecodeAddress(text)
	dcrAddress, _ := dcrutil.DecodeAddress(text)

	result := &StringAnalysis{}

	if btcAddress != nil {
		result.BTCAddress = &AddressString{
			AddressString: btcAddress.String(),
			Type:          "BTC",
		}
	}

	if dcrAddress != nil {
		result.DCRAddress = &AddressString{
			AddressString: dcrAddress.String(),
			Type:          "DCR",
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

func (s HttpsServer) getBalanceBTC(btc_address string, walletAccountName string, min_confirmations int) string {
	//result := BTCBalance{}
	//{
	//	result.BTCAddress.Type = "BTC"
	//	result.BTCAddress.AddressString = btc_address
	//}
	//{
	//	client, err := connect.BTCWallet(s.config)
	//	lang.CheckErr(err)
	//
	//	//client.ListAccounts()
	//
	//	balance, err := client.GetBalanceMinConf(walletAccountName, min_confirmations)
	//	lang.CheckErr(err)
	//	pin.D("balance "+walletAccountName, balance)
	//	result.AmountBTC.Value = balance.ToBTC()
	//}
	//return toJson(result)
	return ""
}

func (s HttpsServer) getBalancePFC(address string, min_confirmations int) string {

	pin.D("get balance account address ", address)

	result := PFCBalance{}
	{
		result.PFCAddress.Type = "PFC"
		result.PFCAddress.AddressString = address
	}
	{
		client, err := connect.PFCWallet(s.config)
		lang.CheckErr(err)

		addr, e := pfcutil.DecodeAddress(address)
		lang.CheckErr(e)

		validation, e := client.ValidateAddress(addr)
		lang.CheckErr(e)

		resolvedAccountName := validation.Account

		balance, err := client.GetBalanceMinConf(resolvedAccountName, min_confirmations)
		lang.CheckErr(err)

		client.Disconnect()

		result.Spendable.Value = balance.Balances[0].Spendable
		result.Unconfirmed.Value = balance.Balances[0].Unconfirmed

		result.ResolvedAccountName = resolvedAccountName
	}
	js := toJson(result)

	return js
}

func (s HttpsServer) getBalanceDCR(address string, min_confirmations int) string {
	pin.D("get balance account address ", address)

	result := DCRBalance{}
	{
		result.DCRAddress.Type = "DCR"
		result.DCRAddress.AddressString = address
	}
	{
		client, err := connect.DCRWallet(s.config)
		lang.CheckErr(err)

		addr, e := dcrutil.DecodeAddress(address)
		lang.CheckErr(e)

		validation, e := client.ValidateAddress(addr)
		lang.CheckErr(e)

		resolvedAccountName := validation.Account

		balance, err := client.GetBalanceMinConf(resolvedAccountName, min_confirmations)
		lang.CheckErr(err)

		client.Disconnect()

		result.Spendable.Value = balance.Balances[0].Spendable
		result.Unconfirmed.Value = balance.Balances[0].Unconfirmed

		result.ResolvedAccountName = resolvedAccountName
	}
	js := toJson(result)

	return js
}

func (s HttpsServer) TransferBTC(client_btc_wallet string, btc_amount float64, err error) string {
	return ""
}

func (s HttpsServer) TransferPFC(PFC_FromAccountAddress string, PFC_ToAddress string, amountFloat float64) string {
	result := Result{}

	client, err := connect.PFCWallet(s.config)
	lang.CheckErr(err)
	{
		addr, e := pfcutil.DecodeAddress(PFC_FromAccountAddress)
		lang.CheckErr(e)
		result.PFC_FromAccountAddress = PFC_FromAccountAddress

		validation, e := client.ValidateAddress(addr)
		lang.CheckErr(e)
		result.PFC_ResolvedAccountName = validation.Account
	}
	toAddr, e := pfcutil.DecodeAddress(PFC_ToAddress)
	lang.CheckErr(e)
	result.PFC_ToAddress = PFC_ToAddress

	amount, err := pfcutil.NewAmount(amountFloat)
	lang.CheckErr(err)

	result.PFC_Amount = AmountPFC{
		Value: amountFloat,
	}

	hash, err := client.SendFrom(result.PFC_ResolvedAccountName, toAddr, amount)
	client.Disconnect()

	if err != nil {
		result.Success = false
		result.ErrorMessage = err.Error()
	} else {
		result.Success = true
		result.PFC_TransactionReceipt = hash.String()
	}

	return toJson(result)
}

func (s HttpsServer) TransferDCR(DCR_FromAccountAddress string, DCR_ToAddress string, amountFloat float64) string {
	result := Result{}

	client, err := connect.DCRWallet(s.config)
	lang.CheckErr(err)
	{
		addr, e := dcrutil.DecodeAddress(DCR_FromAccountAddress)
		lang.CheckErr(e)
		result.DCR_FromAccountAddress = DCR_FromAccountAddress

		validation, e := client.ValidateAddress(addr)
		lang.CheckErr(e)
		result.DCR_ResolvedAccountName = validation.Account
	}
	toAddr, e := dcrutil.DecodeAddress(DCR_ToAddress)
	lang.CheckErr(e)
	result.DCR_ToAddress = DCR_ToAddress

	amount, err := dcrutil.NewAmount(amountFloat)
	lang.CheckErr(err)

	result.DCR_Amount = AmountDCR{
		Value: amountFloat,
	}

	hash, err := client.SendFrom(result.DCR_ResolvedAccountName, toAddr, amount)
	client.Disconnect()

	if err != nil {
		result.Success = false
		result.ErrorMessage = err.Error()
	} else {
		result.Success = true
		result.DCR_TransactionReceipt = hash.String()
	}

	return toJson(result)
}

var mutex = &sync.Mutex{}

func (s HttpsServer) tradePFC(amountPFC float64, operation bool, getQuote bool) string {

	result := TradeResult{}
	if operation {
		result.Operation = "BUY"
	} else {
		result.Operation = "SELL"
	}
	result.GetQuote = getQuote

	mutex.Lock()
	defer mutex.Unlock()
	{
		var SpendableDCR = 0.0
		var SpendablePFC = 0.0
		{
			client, err := connect.DCRWallet(s.config)
			lang.CheckErr(err)

			resolvedAccountName := s.config.DCRWalletConfig.OutputWalletAccountName

			balance, err := client.GetBalanceMinConf(resolvedAccountName, 1)
			lang.CheckErr(err)

			client.Disconnect()

			SpendableDCR = balance.Balances[0].Spendable

		}
		{
			client, err := connect.PFCWallet(s.config)
			lang.CheckErr(err)

			resolvedAccountName := s.config.PFCWalletConfig.OutputWalletAccountName

			balance, err := client.GetBalanceMinConf(resolvedAccountName, 1)
			lang.CheckErr(err)

			client.Disconnect()

			SpendablePFC = balance.Balances[0].Spendable

		}
		result.DCR_InPool_BeforeTrade = SpendableDCR
		result.PFC_InPool_BeforeTrade = SpendablePFC
		result.DCRPFC_Price_BeforeTrade = SpendableDCR / SpendablePFC
		PoolConstant := SpendableDCR * SpendablePFC
		result.PoolConstant = PoolConstant

		if result.Operation == "BUY" {
			result.PFC_InPool_AfterTrade = SpendablePFC - amountPFC
		} else {
			result.PFC_InPool_AfterTrade = SpendablePFC + amountPFC
		}

		if result.PFC_InPool_AfterTrade <= 0 {
			result.ErrorMessage = fmt.Sprintf("Requested amount(%v) exceeds pool size(%v)", amountPFC, SpendablePFC)
			result.Success = false
			return toJson(result)
		}

		result.DCR_InPool_AfterTrade = PoolConstant / result.PFC_InPool_AfterTrade

		result.DCR_Executed_Amount = -(result.DCR_InPool_AfterTrade - result.DCR_InPool_BeforeTrade)
		if result.Operation == "BUY" {
			result.DCR_Executed_Amount = -result.DCR_Executed_Amount
		}
		result.PFC_Executed_Amount = amountPFC
		result.DCRPFC_Price_AfterTrade = result.DCR_InPool_AfterTrade / result.PFC_InPool_AfterTrade
		result.DCRPFC_Executed_Price = result.DCR_Executed_Amount / amountPFC
		result.Success = true
	}

	return toJson(result)
}

func receivedPFCByAddress(r []dcrjson.ListUnspentResult, address string, acc string, minConf int64) float64 {
	balance := float64(0)
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

func receivedBTCByAddress(r []btcjson.ListUnspentResult, address string, acc string, minConf int64) float64 {
	balance := float64(0)
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
