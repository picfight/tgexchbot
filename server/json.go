package server

type Rate struct {
	CirculatingSupplyCoins AmountPFC `json: "CirculatingSupplyCoins"`
	AvailablePFC           AmountPFC `json: "AvailablePFC"`
	AvailableBTC           AmountBTC `json: "AvailableBTC"`

	//MinBTCOperation AmountBTC `json: "MinBTCOperation"`
	//BTCperPFC       float64   `json: "BTCperPFC"`
	//ExchangeRate    float64   `json: "ExchangeRate"`
	//ExchangeMargin  float64   `json: "ExchangeMargin"`
}

type StringAnalysis struct {
	BTCAddress *AddressString `json: "BTCAddress"`
	PFCAddress *AddressString `json: "PFCAddress"`
	Error      string         `json: "Error"`
}

type AddressString struct {
	AddressString string `json: "AddressString"`
	Type          string `json: "Type"`
}

type PFCBalance struct {
	Spendable   AmountPFC `json: "Spendable"`
	Unconfirmed AmountPFC `json: "Unconfirmed"`

	PFCAddress          AddressString `json: "PFCAddress"`
	ResolvedAccountName string        `json: "resolvedAccountName"`
}

type BTCBalance struct {
	Spendable   AmountBTC `json: "Spendable"`
	Unconfirmed AmountBTC `json: "Unconfirmed"`

	BTCAddress          AddressString `json: "BTCAddress"`
	ResolvedAccountName string        `json: "resolvedAccountName"`
}

type AmountPFC struct {
	Value float64 `json: "Value"`
}

type AmountBTC struct {
	Value float64 `json: "Value"`
}

type PlottedChart struct {
	ImageBase64 string `json: "ImageBase64"`
	Error       string `json: "Error"`
	Success     bool   `json: "Success"`
}

type DataPoint struct {
	X float64 `json: "X"`
	Y float64 `json: "Y"`
}

type ChartData struct {
	Title      string      `json: "Title"`
	DataPoints []DataPoint `json: "DataPoints"`
	ImgWidth   float64     `json: "ImgWidth"`
	ImgHeight  float64     `json: "ImgHeight"`
	X_Label    string      `json: "X_Label"`
	Y_Label    string      `json: "Y_Label"`
}

type TradeResult struct {
	Success       bool   `json: "Success"`
	NoEnoughFunds bool   `json: "NoEnoughFunds"`
	ErrorMessage  string `json: "ErrorMessage"`
	Operation     string `json: "Operation"`
	Executed      bool   `json: "Executed"`

	BTCPFC_Ratio_BeforeTrade float64 `json: "BTCPFC_Ratio_BeforeTrade"`
	BTCPFC_Ratio_AfterTrade  float64 `json: "BTCPFC_Ratio_AfterTrade"`

	BTC_InPool_BeforeTrade AmountBTC `json: "BTC_InPool_BeforeTrade"`
	PFC_InPool_BeforeTrade AmountPFC `json: "PFC_InPool_BeforeTrade"`
	PoolConstant           float64   `json: "PoolConstant"`
	PFC_InPool_AfterTrade  AmountPFC `json: "PFC_InPool_AfterTrade"`
	BTC_InPool_AfterTrade  AmountBTC `json: "BTC_InPool_AfterTrade"`

	BTC_Executed_Amount AmountBTC `json: "BTC_Executed_Amount"`
	PFC_Executed_Amount AmountPFC `json: "PFC_Executed_Amount"`

	BTCPFC_Executed_Price float64 `json: "BTCPFC_Executed_Price"`

	BTC_Transaction TransactionResult `json: "BTC_Transaction"`
	PFC_Transaction TransactionResult `json: "PFC_Transaction"`

	Requested_Price_Btc_for_1_pfc float64 `json: "Requested_Price_Btc_for_1_pfc"`

	UnfinishedTransaction bool `json: "UnfinishedTransaction"`
	PriceNotMet           bool `json: "PriceNotMet"`
}

type TransactionResult struct {
	Success      bool   `json: "Success"`
	ErrorMessage string `json: "ErrorMessage"`

	BTC_FromAccountAddress  string    `json: "BTC_FromAccountAddress"`
	BTC_ResolvedAccountName string    `json: "BTC_ResolvedAccountName"`
	BTC_Amount              AmountBTC `json: "BTC_Amount"`
	BTC_ToAddress           string    `json: "BTC_ToAddress"`
	BTC_TransactionReceipt  string    `json: "BTC_TransactionReceipt"`

	PFC_FromAccountAddress  string    `json: "PFC_FromAccountAddress"`
	PFC_ResolvedAccountName string    `json: "PFC_ResolvedAccountName"`
	PFC_Amount              AmountPFC `json: "PFC_Amount"`
	PFC_ToAddress           string    `json: "PFC_ToAddress"`
	PFC_TransactionReceipt  string    `json: "PFC_TransactionReceipt"`
}
