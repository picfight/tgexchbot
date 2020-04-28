package server

type Rate struct {
	CirculatingSupplyCoins float64   `json: "CirculatingSupplyCoins"`
	AvailablePFC           AmountPFC `json: "AvailablePFC"`
	AvailableBTC           AmountBTC `json: "AvailableBTC"`
	BTCperPFC              float64   `json: "BTCperPFC"`
	ExchangeRate           float64   `json: "ExchangeRate"`
	ExchangeMargin         float64   `json: "ExchangeMargin"`
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
	AmountPFC  AmountPFC     `json: "AmountPFC"`
	PFCAddress AddressString `json: "PFCAddress"`
}

type BTCBalance struct {
	AmountBTC  AmountBTC     `json: "AmountBTC"`
	BTCAddress AddressString `json: "BTCAddress"`
}

type AmountPFC struct {
	Value float64 `json: "Value"`
}

type AmountBTC struct {
	Value float64 `json: "Value"`
}
