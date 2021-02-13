package server

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	btccfg "github.com/btcsuite/btcd/chaincfg"
	"github.com/btcsuite/btcutil"

	"github.com/jfixby/pin"
	"github.com/jfixby/pin/lang"
	pfcutil "github.com/picfight/pfcd/dcrutil"
	"github.com/picfight/tgexchbot/cfg"
	"github.com/picfight/tgexchbot/connect"
	"io"
	"log"
	"net/http"
	"os"
	"path"
	"strconv"
	"strings"
	"sync"
)

type HttpsServer struct {
	config *cfg.ConfigJson
	//handler func(s *HttpsServer) (w http.ResponseWriter, r *http.Request)
}

const ACCESS_KEY = "TGEXCHBOTKEY"
const BTCWKEY = "BTCWKEY"
const PFCWKEY = "PFCWKEY"

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

// GetIP gets a requests IP address by reading off the forwarded-for
// header (for proxies) and falls back to use the remote address.
func GetIP(r *http.Request) string {
	forwarded := r.Header.Get("X-FORWARDED-FOR")
	if forwarded != "" {
		return forwarded
	}
	return r.RemoteAddr
}

func (s *HttpsServer) Handler(w http.ResponseWriter, r *http.Request) {
	uri := r.RequestURI
	_, command := path.Split(uri)
	//pin.D("dir", dir)
	pin.D("command", command)

	client_ip := GetIP(r)
	pin.D("client ip", client_ip)
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

var mutex = &sync.Mutex{}

func (s *HttpsServer) processRequest(command string, access_key string, params http.Header) string {
	valid := checkAccessKey(access_key)
	if !valid {
		return `{"error":"Access denied, invalid key"}`
	}

	if command == "new_pfc_address" {
		account_name := params["Account_name"][0]
		return s.obrtainPFCAddress(account_name)
	}

	if command == "new_btc_address" {
		account_name := params["Account_name"][0]
		return s.obrtainBTCAddress(account_name)
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

	//if command == "plot_chart" {
	//	Chart_data_base64 := params["Chart_data_base64"][0]
	//	//pin.D("Raw_text", raw_text)
	//	return s.PlotChart(Chart_data_base64)
	//}

	for k, v := range params {
		pin.D(k, v)
	}

	mutex.Lock()
	//------------------------------------------------------------------------------
	defer mutex.Unlock()

	if command == "trade_pfc" {
		amountString := params["Pfc_amount"][0]
		pfc_amount, err := strconv.ParseFloat(amountString, 64)
		lang.CheckErr(err)
		op_string := params["Operation"][0]
		op := op_string == "BUY" //true
		getquote_string := params["Getquote"][0]
		getquote := getquote_string == "true"

		User_pfc_account := params["User_pfc_account"][0]
		User_btc_account := params["User_btc_account"][0]
		Exchange_pfc_account := params["Exchange_pfc_account"][0]
		Exchange_btc_account := params["Exchange_btc_account"][0]

		Btc_for_1_pfc_order_string := params["Btc_for_1_pfc_order"][0]
		Btc_for_1_pfc_order, err := strconv.ParseFloat(Btc_for_1_pfc_order_string, 64)
		lang.CheckErr(err)

		return s.tradePFC(pfc_amount, op, getquote, User_pfc_account, User_btc_account, Exchange_pfc_account, Exchange_btc_account, Btc_for_1_pfc_order)
	}

	if command == "get_balance_pfc" {
		pfc_address := params["Pfc_address"][0]
		min_confirmations_string := params["Min_confirmations"][0]
		min_confirmations, err := strconv.ParseInt(min_confirmations_string, 10, 64)
		lang.CheckErr(err)
		return s.getBalancePFC(pfc_address, int(min_confirmations))
	}

	if command == "get_balance_btc" {
		pfc_address := params["Btc_address"][0]
		min_confirmations_string := params["Min_confirmations"][0]
		min_confirmations, err := strconv.ParseInt(min_confirmations_string, 10, 64)
		lang.CheckErr(err)
		return s.getBalanceBTC(pfc_address, int(min_confirmations))
	}

	if command == "transfer_pfc" {
		PFC_FromAccountAddress := params["Pfc_fromaccountaddress"][0]
		PFC_Amount_string := params["Pfc_amount"][0]
		Amount, err := strconv.ParseFloat(PFC_Amount_string, 64)
		if err != nil {
			return fmt.Sprintf(`{"status":"%v"}`, err)
		}
		PFC_ToAddress := params["Pfc_toaddress"][0]
		PFC_Amount := AmountPFC{Value: Amount}
		return s.TransferPFC(PFC_FromAccountAddress, PFC_ToAddress, PFC_Amount)
	}

	if command == "transfer_btc" {
		BTC_FromAccountAddress := params["Btc_fromaccountaddress"][0]
		BTC_Amount_string := params["Btc_amount"][0]
		Amount, err := strconv.ParseFloat(BTC_Amount_string, 64)
		if err != nil {
			return fmt.Sprintf(`{"status":"%v"}`, err)
		}
		BTC_ToAddress := params["Btc_toaddress"][0]
		BTC_Amount := AmountBTC{Value: Amount}
		return s.TransferBTC(BTC_FromAccountAddress, BTC_ToAddress, BTC_Amount)
	}

	return `{"status":"ok"}`
}

func checkAccessKey(received_key string) bool {
	set := os.Getenv(ACCESS_KEY)
	return received_key == set
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
		//key := os.Getenv(BTCWKEY)
		//err = client.WalletPassphrase(key, 10000000)
		//lang.CheckErr(err)
		_, err = client.GetAccountAddress(walletAccountName)
		if err != nil {
			pin.D("Error", err)
			pin.D("Creating BTC account", walletAccountName)
			err := client.CreateNewAccount(walletAccountName)
			lang.CheckErr(err)
		}

		addressResult, err := client.GetNewAddress(walletAccountName)
		lang.CheckErr(err)
		client.Shutdown()

		address.AddressString = addressResult.String()
	}
	return toJson(address)
}

func (s HttpsServer) AnalyzeString(hextext string) string {
	text := hextext

	pin.D("text", text)

	btcAddress, _ := btcutil.DecodeAddress(text, &btccfg.MainNetParams)
	pfcAddress, _ := pfcutil.DecodeAddress(text)

	result := &StringAnalysis{}

	if btcAddress != nil {
		result.BTCAddress = &AddressString{
			AddressString: btcAddress.String(),
			Type:          "BTC",
		}
	}

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

func (s HttpsServer) getBalanceBTC(address string, min_confirmations int) string {
	result, err := s.executeGetBalanceBTC(address, min_confirmations)
	lang.CheckErr(err)
	js := toJson(result)
	return js
}

func (s HttpsServer) executeGetBalanceBTC(address string, min_confirmations int) (*BTCBalance, error) {
	result := &BTCBalance{}
	{
		result.BTCAddress.Type = "BTC"
		result.BTCAddress.AddressString = address
	}
	{
		client, err := connect.BTCWallet(s.config)
		defer client.Shutdown()
		if err != nil {
			return nil, err
		}

		addr, e := btcutil.DecodeAddress(address, &btccfg.MainNetParams)
		if e != nil {
			return nil, err
		}

		validation, e := client.ValidateAddress(addr)
		pin.D("validation", validation)
		if e != nil {
			return nil, err
		}

		if !validation.IsMine {
			e := fmt.Errorf("account for %v not found", addr)
			return nil, e
		}

		resolvedAccountName := validation.Account

		balance, err := client.GetBalanceMinConf(resolvedAccountName, min_confirmations)
		if err != nil {
			return nil, err
		}

		result.Spendable.Value = balance.ToBTC()

		ubalance, err := client.GetUnconfirmedBalance(resolvedAccountName)
		if err != nil {
			return nil, err
		}

		result.Unconfirmed.Value = ubalance.ToBTC()

		result.ResolvedAccountName = resolvedAccountName
	}
	return result, nil
}

func (s HttpsServer) getBalancePFC(address string, min_confirmations int) string {
	result, err := s.executeGetBalancePFC(address, min_confirmations)
	lang.CheckErr(err)
	js := toJson(result)
	return js
}

func (s HttpsServer) executeGetBalancePFC(address string, min_confirmations int) (*PFCBalance, error) {
	result := &PFCBalance{}
	{
		result.PFCAddress.Type = "PFC"
		result.PFCAddress.AddressString = address
	}
	{
		client, err := connect.PFCWallet(s.config)
		defer client.Disconnect()
		if err != nil {
			return nil, err
		}

		addr, e := pfcutil.DecodeAddress(address)
		if e != nil {
			return nil, err
		}

		validation, e := client.ValidateAddress(addr)
		if e != nil {
			return nil, err
		}

		resolvedAccountName := validation.Account

		balance, err := client.GetBalanceMinConf(resolvedAccountName, min_confirmations)
		if err != nil {
			return nil, err
		}

		result.Spendable.Value = balance.Balances[0].Spendable
		result.Unconfirmed.Value = balance.Balances[0].Unconfirmed

		result.ResolvedAccountName = resolvedAccountName
	}
	return result, nil
}

func (s HttpsServer) TransferBTC(BTC_FromAccountAddress string, BTC_ToAddress string, amountFloat AmountBTC) string {
	result, err := s.executeTransferBTC(BTC_FromAccountAddress, BTC_ToAddress, amountFloat)
	if result == nil {
		lang.CheckErr(err)
	}
	return toJson(result)
}
func (s HttpsServer) TransferPFC(PFC_FromAccountAddress string, PFC_ToAddress string, amountFloat AmountPFC) string {
	result, err := s.executeTransferPFC(PFC_FromAccountAddress, PFC_ToAddress, amountFloat)
	if result == nil {
		lang.CheckErr(err)
	}
	return toJson(result)
}

func (s HttpsServer) tradePFC(amountPFC float64, operation bool, getQuote bool, User_pfc_account, User_btc_account, Exchange_pfc_account, Exchange_btc_account string, Btc_for_1_pfc_order float64) string {

	result := TradeResult{}
	if operation {
		result.Operation = "BUY"
	} else {
		result.Operation = "SELL"
	}
	result.Executed = false
	result.Requested_Price_Btc_for_1_pfc = Btc_for_1_pfc_order
	{
		var SpendableBTC = 0.0
		var SpendablePFC = 0.0
		{
			client, err := connect.BTCWallet(s.config)
			lang.CheckErr(err)

			resolvedAccountName := s.config.BTCWalletConfig.OutputWalletAccountName

			balance, err := client.GetBalanceMinConf(resolvedAccountName, 1)
			lang.CheckErr(err)

			client.Shutdown()

			SpendableBTC = balance.ToBTC()

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
		result.BTC_InPool_BeforeTrade.Value = SpendableBTC
		result.PFC_InPool_BeforeTrade.Value = SpendablePFC
		result.BTCPFC_Ratio_BeforeTrade = SpendableBTC / SpendablePFC
		PoolConstant := SpendableBTC * SpendablePFC
		result.PoolConstant = PoolConstant
		if result.Operation == "BUY" {
			result.PFC_InPool_AfterTrade.Value = SpendablePFC - amountPFC
		} else {
			result.PFC_InPool_AfterTrade.Value = SpendablePFC + amountPFC
		}

		if result.PFC_InPool_AfterTrade.Value <= 0 {
			result.ErrorMessage = fmt.Sprintf("Requested amount(%v) exceeds pool size(%v)", amountPFC, SpendablePFC)
			result.Success = false
			return toJson(result)
		}

		result.BTC_InPool_AfterTrade.Value = PoolConstant / result.PFC_InPool_AfterTrade.Value

		if result.BTC_InPool_AfterTrade.Value <= 0 {
			result.ErrorMessage = fmt.Sprintf("Pool drain (%v BTC) ", result.BTC_InPool_AfterTrade.Value)
			result.Success = false
			return toJson(result)
		}

		result.BTC_Executed_Amount.Value = -(result.BTC_InPool_AfterTrade.Value - result.BTC_InPool_BeforeTrade.Value)
		if result.Operation == "BUY" {
			result.BTC_Executed_Amount.Value = -result.BTC_Executed_Amount.Value
		}
		result.PFC_Executed_Amount.Value = amountPFC
		result.BTCPFC_Ratio_AfterTrade = result.BTC_InPool_AfterTrade.Value / result.PFC_InPool_AfterTrade.Value
		result.BTCPFC_Executed_Price = result.BTC_Executed_Amount.Value / amountPFC

	}

	minPFCAmount := 0.0
	minBTCAmount := 0.001

	{
		client, err := connect.BTCWallet(s.config)
		lang.CheckErr(err)

		fee, err := client.EstimateFee(1)
		lang.CheckErr(err)

		feea, err := btcutil.NewAmount(fee)
		lang.CheckErr(err)

		result.BTC_Fee.Value = feea.ToBTC()

		minBTCAmount = feea.ToBTC() * 3.0

		client.Shutdown()
	}

	if getQuote {
		result.Success = true
		return toJson(result)
	}
	// order execution:
	{

		if amountPFC <= minPFCAmount {
			result.ErrorMessage = fmt.Sprintf("Requested PFC amount(%v) must be > %v ", amountPFC, minPFCAmount)
			result.Success = false
			return toJson(result)
		}
		amountBTC := result.BTC_Executed_Amount.Value
		if amountBTC <= minBTCAmount {
			result.ErrorMessage = fmt.Sprintf("Requested BTC amount(%v) must be > %v ", amountBTC, minBTCAmount)
			result.Success = false
			result.MinBTCAmountError = true
			result.MinBTCAmountValue.Value = minBTCAmount
			return toJson(result)
		}
	}

	{
		if result.Operation == "BUY" {
			if result.BTCPFC_Executed_Price > Btc_for_1_pfc_order {
				result.PriceNotMet = true
				result.Success = false
				return toJson(result)
			}
		}
		if result.Operation == "SELL" {
			if result.BTCPFC_Executed_Price < Btc_for_1_pfc_order {
				result.PriceNotMet = true
				result.Success = false
				return toJson(result)
			}
		}
	}
	{
		result.Executed = true
		if result.Operation == "BUY" { //buy pfc

			balance, err := s.executeGetBalanceBTC(User_btc_account, 1)
			if err != nil {
				result.ErrorMessage = err.Error()
				result.Success = false
				return toJson(result)
			}
			if balance.Spendable.Value <= result.BTC_Executed_Amount.Value {
				result.NoEnoughFunds = true
				result.Success = false
				return toJson(result)
			}

			pay_btc_result, err := s.executeTransferBTC(User_btc_account, Exchange_btc_account, result.BTC_Executed_Amount)
			if err != nil {
				result.ErrorMessage = err.Error()
				result.Success = false
				return toJson(result)
			}
			result.BTC_Transaction = *pay_btc_result

			deliver_pfc_result, err := s.executeTransferPFC(Exchange_pfc_account, User_pfc_account, result.PFC_Executed_Amount)
			if err != nil {
				result.ErrorMessage = err.Error()
				result.Success = false
				result.UnfinishedTransaction = true
				return toJson(result)
			}
			result.PFC_Transaction = *deliver_pfc_result
		} else if result.Operation == "SELL" { //sell pfc
			balance, err := s.executeGetBalancePFC(User_pfc_account, 1)
			if err != nil {
				result.ErrorMessage = err.Error()
				result.Success = false
				return toJson(result)
			}
			if balance.Spendable.Value <= result.PFC_Executed_Amount.Value {
				result.NoEnoughFunds = true
				result.Success = false
				return toJson(result)
			}

			pay_pfc_result, err := s.executeTransferPFC(User_pfc_account, Exchange_pfc_account, result.PFC_Executed_Amount)
			if err != nil {
				result.ErrorMessage = err.Error()
				result.Success = false
				return toJson(result)
			}
			result.PFC_Transaction = *pay_pfc_result

			BTC_Executed_Amount_MUNIS_Fee := AmountBTC{result.BTC_Executed_Amount.Value - result.BTC_Fee.Value}
			pin.AssertTrue("BTC_Executed_Amount_MUNIS_Fee > 0 ", BTC_Executed_Amount_MUNIS_Fee.Value > 0)
			deliver_btc_result, err := s.executeTransferBTC(Exchange_btc_account, User_btc_account, BTC_Executed_Amount_MUNIS_Fee)
			if err != nil {
				result.ErrorMessage = err.Error()
				result.Success = false
				result.UnfinishedTransaction = true
				return toJson(result)
			}
			result.BTC_Transaction = *deliver_btc_result
		} else {
			result.ErrorMessage = "Unknown operation: " + result.Operation
			result.Success = false
			return toJson(result)
		}
	}

	result.Success = true
	return toJson(result)
}

func (s HttpsServer) PlotChart(Chart_data_base64 string) string {

	result := PlottedChart{}
	data := ChartData{}

	bytes, err := base64.StdEncoding.DecodeString(Chart_data_base64)
	if err != nil {
		result.Error = err.Error()
		result.Success = false
		return toJson(result)
	}

	err = json.Unmarshal(bytes, &data)
	if err != nil {
		result.Error = err.Error()
		result.Success = false
		return toJson(result)
	}

	imgBytes, err := Plot(data)

	if err != nil {
		result.Error = err.Error()
		result.Success = false
		return toJson(result)
	}

	result.ImageBase64 = base64.StdEncoding.EncodeToString(imgBytes)
	result.Success = true

	return toJson(result)

}

func (s HttpsServer) executeTransferPFC(PFC_FromAccountAddress string, PFC_ToAddress string, amountFloat AmountPFC) (*TransactionResult, error) {
	result := &TransactionResult{}

	client, err := connect.PFCWallet(s.config)
	defer client.Disconnect()
	if err != nil {
		return nil, err
	}

	{
		addr, e := pfcutil.DecodeAddress(PFC_FromAccountAddress)
		if e != nil {
			return nil, err
		}
		result.PFC_FromAccountAddress = PFC_FromAccountAddress

		validation, e := client.ValidateAddress(addr)
		if e != nil {
			return nil, err
		}
		result.PFC_ResolvedAccountName = validation.Account
	}
	toAddr, e := pfcutil.DecodeAddress(PFC_ToAddress)
	if e != nil {
		return nil, err
	}
	result.PFC_ToAddress = PFC_ToAddress

	amount, err := pfcutil.NewAmount(amountFloat.Value)
	if err != nil {
		return nil, err
	}

	result.PFC_Amount = amountFloat

	{
		key := os.Getenv(PFCWKEY)
		err = client.WalletPassphrase(key, 10000000)
		if err != nil {
			return nil, err
		}
	}

	hash, err := client.SendFrom(result.PFC_ResolvedAccountName, toAddr, amount)

	if hash != nil {
		result.PFC_TransactionReceipt = hash.String()
	}

	if err != nil {
		result.Success = false
		result.ErrorMessage = err.Error()
	} else {
		result.Success = true
	}
	return result, err

}

func (s HttpsServer) executeTransferBTC(BTC_FromAccountAddress string, BTC_ToAddress string, amountFloat AmountBTC) (*TransactionResult, error) {
	result := &TransactionResult{}

	client, err := connect.BTCWallet(s.config)
	defer client.Shutdown()
	if err != nil {
		return nil, err
	}

	{
		addr, e := btcutil.DecodeAddress(BTC_FromAccountAddress, &btccfg.MainNetParams)
		if e != nil {
			return nil, err
		}
		result.BTC_FromAccountAddress = BTC_FromAccountAddress

		validation, e := client.ValidateAddress(addr)
		if e != nil {
			return nil, err
		}
		result.BTC_ResolvedAccountName = validation.Account
	}
	toAddr, e := btcutil.DecodeAddress(BTC_ToAddress, &btccfg.MainNetParams)
	if e != nil {
		return nil, err
	}
	result.BTC_ToAddress = BTC_ToAddress

	amount, err := btcutil.NewAmount(amountFloat.Value)
	if err != nil {
		return nil, err
	}

	result.BTC_Amount = amountFloat

	{
		//key := os.Getenv(BTCWKEY)
		//err = client.WalletPassphrase(key, 10000000)
		//if err != nil {
		//	return nil, err
		//}
		fee, err := client.EstimateFee(1)
		if err != nil {
			return nil, err
		}
		feea, err := btcutil.NewAmount(fee)
		if err != nil {
			return nil, err
		}
		err = client.SetTxFee(feea)
		if err != nil {
			return nil, err
		}
	}

	hash, err := client.SendFrom(result.BTC_ResolvedAccountName, toAddr, amount)

	if hash != nil {
		result.BTC_TransactionReceipt = hash.String()
	}

	if err != nil {
		result.Success = false
		result.ErrorMessage = err.Error()
	} else {
		result.Success = true
	}
	return result, err
}

func toJson(v interface{}) string {
	bytes, err := json.MarshalIndent(v, "", "	")
	lang.CheckErr(err)
	return string(bytes)
}
