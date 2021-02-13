package server

import (
	"encoding/json"
	"fmt"
	"github.com/decred/dcrd/dcrutil"
	pfcutil "github.com/picfight/pfcd/dcrutil"

	"github.com/jfixby/pin"
	"github.com/jfixby/pin/lang"

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
const DCRWKEY = "DCRWKEY"
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

	if command == "new_dcr_address" {
		account_name := params["Account_name"][0]
		return s.obrtainDCRAddress(account_name)
	}

	if command == "analyze_string" {
		raw_text := params["Raw_text"]
		//pin.D("Raw_text", raw_text)
		return s.AnalyzeString(raw_text[0])
	}

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
		User_dcr_account := params["User_dcr_account"][0]
		Exchange_pfc_account := params["Exchange_pfc_account"][0]
		Exchange_dcr_account := params["Exchange_dcr_account"][0]

		Dcr_for_1_pfc_order_string := params["Dcr_for_1_pfc_order"][0]
		Dcr_for_1_pfc_order, err := strconv.ParseFloat(Dcr_for_1_pfc_order_string, 64)
		lang.CheckErr(err)

		return s.tradePFC(pfc_amount, op, getquote, User_pfc_account, User_dcr_account, Exchange_pfc_account, Exchange_dcr_account, Dcr_for_1_pfc_order)
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

	if command == "transfer_dcr" {
		DCR_FromAccountAddress := params["Dcr_fromaccountaddress"][0]
		DCR_Amount_string := params["Dcr_amount"][0]
		Amount, err := strconv.ParseFloat(DCR_Amount_string, 64)
		if err != nil {
			return fmt.Sprintf(`{"status":"%v"}`, err)
		}
		DCR_ToAddress := params["Dcr_toaddress"][0]
		DCR_Amount := AmountDCR{Value: Amount}
		return s.TransferDCR(DCR_FromAccountAddress, DCR_ToAddress, DCR_Amount)
	}

	return `{"status":"ok"}`
}

func checkAccessKey(received_key string) bool {
	set := os.Getenv(ACCESS_KEY)
	return received_key == set
}

func (s HttpsServer) AnalyzeString(hextext string) string {
	text := hextext

	pin.D("text", text)

	dcrAddress, _ := dcrutil.DecodeAddress(text)
	pfcAddress, _ := pfcutil.DecodeAddress(text)

	result := &StringAnalysis{}

	if dcrAddress != nil {
		result.DCRAddress = &AddressString{
			AddressString: dcrAddress.String(),
			Type:          "DCR",
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

func (s HttpsServer) tradePFC(amountPFC float64, operation bool, getQuote bool, User_pfc_account, User_dcr_account, Exchange_pfc_account, Exchange_dcr_account string, Dcr_for_1_pfc_order float64) string {

	result := TradeResult{}
	if operation {
		result.Operation = "BUY"
	} else {
		result.Operation = "SELL"
	}
	result.Executed = false
	result.Requested_Price_Dcr_for_1_pfc = Dcr_for_1_pfc_order
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
		result.DCR_InPool_BeforeTrade.Value = SpendableDCR
		result.PFC_InPool_BeforeTrade.Value = SpendablePFC
		result.DCRPFC_Ratio_BeforeTrade = SpendableDCR / SpendablePFC
		PoolConstant := SpendableDCR * SpendablePFC
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

		result.DCR_InPool_AfterTrade.Value = PoolConstant / result.PFC_InPool_AfterTrade.Value

		if result.DCR_InPool_AfterTrade.Value <= 0 {
			result.ErrorMessage = fmt.Sprintf("Pool drain (%v DCR) ", result.DCR_InPool_AfterTrade.Value)
			result.Success = false
			return toJson(result)
		}

		result.DCR_Executed_Amount.Value = -(result.DCR_InPool_AfterTrade.Value - result.DCR_InPool_BeforeTrade.Value)
		if result.Operation == "BUY" {
			result.DCR_Executed_Amount.Value = -result.DCR_Executed_Amount.Value
		}
		result.PFC_Executed_Amount.Value = amountPFC
		result.DCRPFC_Ratio_AfterTrade = result.DCR_InPool_AfterTrade.Value / result.PFC_InPool_AfterTrade.Value
		result.DCRPFC_Executed_Price = result.DCR_Executed_Amount.Value / amountPFC

	}

	minPFCAmount := 0.0
	minDCRAmount := 0.0

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
		amountDCR := result.DCR_Executed_Amount.Value
		if amountDCR <= minDCRAmount {
			result.ErrorMessage = fmt.Sprintf("Requested DCR amount(%v) must be > %v ", amountDCR, minDCRAmount)
			result.Success = false
			result.MinDCRAmountError = true
			result.MinDCRAmountValue.Value = minDCRAmount
			return toJson(result)
		}
	}

	{
		if result.Operation == "BUY" {
			if result.DCRPFC_Executed_Price > Dcr_for_1_pfc_order {
				result.PriceNotMet = true
				result.Success = false
				return toJson(result)
			}
		}
		if result.Operation == "SELL" {
			if result.DCRPFC_Executed_Price < Dcr_for_1_pfc_order {
				result.PriceNotMet = true
				result.Success = false
				return toJson(result)
			}
		}
	}
	{
		result.Executed = true
		if result.Operation == "BUY" { //buy pfc

			balance, err := s.executeGetBalanceDCR(User_dcr_account, 1)
			if err != nil {
				result.ErrorMessage = err.Error()
				result.Success = false
				return toJson(result)
			}
			if balance.Spendable.Value <= result.DCR_Executed_Amount.Value {
				result.NoEnoughFunds = true
				result.Success = false
				return toJson(result)
			}

			pay_dcr_result, err := s.executeTransferDCR(User_dcr_account, Exchange_dcr_account, result.DCR_Executed_Amount)
			if err != nil {
				result.ErrorMessage = err.Error()
				result.Success = false
				return toJson(result)
			}
			result.DCR_Transaction = *pay_dcr_result

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

			DCR_Executed_Amount_MUNIS_Fee := AmountDCR{result.DCR_Executed_Amount.Value}
			pin.AssertTrue("DCR_Executed_Amount_MUNIS_Fee > 0 ", DCR_Executed_Amount_MUNIS_Fee.Value > 0)
			deliver_dcr_result, err := s.executeTransferDCR(Exchange_dcr_account, User_dcr_account, DCR_Executed_Amount_MUNIS_Fee)
			if err != nil {
				result.ErrorMessage = err.Error()
				result.Success = false
				result.UnfinishedTransaction = true
				return toJson(result)
			}
			result.DCR_Transaction = *deliver_dcr_result
		} else {
			result.ErrorMessage = "Unknown operation: " + result.Operation
			result.Success = false
			return toJson(result)
		}
	}

	result.Success = true
	return toJson(result)
}

func toJson(v interface{}) string {
	bytes, err := json.MarshalIndent(v, "", "	")
	lang.CheckErr(err)
	return string(bytes)
}
