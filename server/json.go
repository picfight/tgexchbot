package server

type Rate struct {
	CirculatingSupplyCoins AmountPFC `json: "CirculatingSupplyCoins"`
	AvailablePFC           AmountPFC `json: "AvailablePFC"`
	AvailableBTC           AmountBTC `json: "AvailableBTC"`
	AvailableDCR           AmountBTC `json: "AvailableDCR"`

	//MinBTCOperation AmountBTC `json: "MinBTCOperation"`
	//BTCperPFC       float64   `json: "BTCperPFC"`
	//ExchangeRate    float64   `json: "ExchangeRate"`
	//ExchangeMargin  float64   `json: "ExchangeMargin"`
}

type StringAnalysis struct {
	BTCAddress *AddressString `json: "BTCAddress"`
	DCRAddress *AddressString `json: "DCRAddress"`
	PFCAddress *AddressString `json: "PFCAddress"`
	Error      string         `json: "Error"`
}

type AddressString struct {
	AddressString string `json: "AddressString"`
	Type          string `json: "Type"`
}

type PFCBalance struct {
	AmountPFC  AmountPFC     `json: "AmountPFC"`
	PFCAddress AddressString `json: "PFCAddress"`
}

type DCRBalance struct {
	AmountDCR  AmountDCR     `json: "AmountDCR"`
	DCRAddress AddressString `json: "DCRAddress"`
}

type BTCBalance struct {
	AmountBTC  AmountBTC     `json: "AmountBTC"`
	BTCAddress AddressString `json: "BTCAddress"`
}

type AmountPFC struct {
	Value float64 `json: "Value"`
}

type AmountDCR struct {
	Value float64 `json: "Value"`
}

type AmountBTC struct {
	Value float64 `json: "Value"`
}

type Result struct {
	Success                 bool      `json: "Success"`
	Error_message           string    `json: "Error_message"`
	Btc_transaction_receipt string    `json: "Btc_transaction_receipt"`
	Pfc_transaction_receipt string    `json: "Pfc_transaction_receipt"`
	Dcr_transaction_receipt string    `json: "Dcr_transaction_receipt"`
	BtcAmount               AmountBTC `json: "BtcAmount"`
	PfcAmount               AmountPFC `json: "PfcAmount"`
	DcrAmount               AmountDCR `json: "DcrAmount"`
}
