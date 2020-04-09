package server

type Rate struct {
	//	Host            string `json: "host"`
	CirculatingSupplyCoins float64 `json: "CirculatingSupplyCoins"`
	AvailablePFC           float64 `json: "AvailablePFC"`
	BTCperPFC              float64 `json: "BTCperPFC"`
}

type StringAnalysis struct {
	BTCAddress *AddressString `json: "btc_address"`
	PFCAddress *AddressString `json: "pfc_address"`
}

type AddressString struct {
	AAddressString string `json: "address_string"`
	TType          string `json: "type"`
}
