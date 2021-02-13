package server

type Rate struct {
	CirculatingSupplyCoins AmountPFC `json: "CirculatingSupplyCoins"`
	AvailablePFC           AmountPFC `json: "AvailablePFC"`
	AvailableDCR           AmountDCR `json: "AvailableDCR"`

	//MinDCROperation AmountDCR `json: "MinDCROperation"`
	//DCRperPFC       float64   `json: "DCRperPFC"`
	//ExchangeRate    float64   `json: "ExchangeRate"`
	//ExchangeMargin  float64   `json: "ExchangeMargin"`
}

type StringAnalysis struct {
	DCRAddress *AddressString `json: "DCRAddress"`
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

type DCRBalance struct {
	Spendable   AmountDCR `json: "Spendable"`
	Unconfirmed AmountDCR `json: "Unconfirmed"`

	DCRAddress          AddressString `json: "DCRAddress"`
	ResolvedAccountName string        `json: "resolvedAccountName"`
}

type AmountPFC struct {
	Value float64 `json: "Value"`
}

type AmountDCR struct {
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

	DCRPFC_Ratio_BeforeTrade float64 `json: "DCRPFC_Ratio_BeforeTrade"`
	DCRPFC_Ratio_AfterTrade  float64 `json: "DCRPFC_Ratio_AfterTrade"`

	DCR_InPool_BeforeTrade AmountDCR `json: "DCR_InPool_BeforeTrade"`
	PFC_InPool_BeforeTrade AmountPFC `json: "PFC_InPool_BeforeTrade"`
	PoolConstant           float64   `json: "PoolConstant"`
	PFC_InPool_AfterTrade  AmountPFC `json: "PFC_InPool_AfterTrade"`
	DCR_InPool_AfterTrade  AmountDCR `json: "DCR_InPool_AfterTrade"`

	DCR_Executed_Amount AmountDCR `json: "DCR_Executed_Amount"`
	PFC_Executed_Amount AmountPFC `json: "PFC_Executed_Amount"`

	DCRPFC_Executed_Price float64 `json: "DCRPFC_Executed_Price"`

	DCR_Transaction TransactionResult `json: "DCR_Transaction"`
	PFC_Transaction TransactionResult `json: "PFC_Transaction"`

	Requested_Price_Dcr_for_1_pfc float64 `json: "Requested_Price_Dcr_for_1_pfc"`

	UnfinishedTransaction bool `json: "UnfinishedTransaction"`
	PriceNotMet           bool `json: "PriceNotMet"`
	MinDCRAmountError     bool `json: "MinDCRAmountError"`
	MinDCRAmountValue     AmountDCR `json: "MinDCRAmountValue"`
}

type TransactionResult struct {
	Success      bool   `json: "Success"`
	ErrorMessage string `json: "ErrorMessage"`

	DCR_FromAccountAddress  string    `json: "DCR_FromAccountAddress"`
	DCR_ResolvedAccountName string    `json: "DCR_ResolvedAccountName"`
	DCR_Amount              AmountDCR `json: "DCR_Amount"`
	DCR_ToAddress           string    `json: "DCR_ToAddress"`
	DCR_TransactionReceipt  string    `json: "DCR_TransactionReceipt"`

	PFC_FromAccountAddress  string    `json: "PFC_FromAccountAddress"`
	PFC_ResolvedAccountName string    `json: "PFC_ResolvedAccountName"`
	PFC_Amount              AmountPFC `json: "PFC_Amount"`
	PFC_ToAddress           string    `json: "PFC_ToAddress"`
	PFC_TransactionReceipt  string    `json: "PFC_TransactionReceipt"`
}
