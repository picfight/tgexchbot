package server

type Rate struct {
	//	Host            string `json: "host"`
	CirculatingSupplyCoins float64 `json: "CirculatingSupplyCoins"`
	AvailablePFC           float64 `json: "AvailablePFC"`
	BTCperPFC              float64 `json: "BTCperPFC"`
}

type StringAnalysis struct {
	BTCAddress *AddressString `json: "BTCAddress"`
	PFCAddress *AddressString `json: "PFCAddress"`
	Error string `json: "Error"`
}

type AddressString struct {
	AddressString string `json: "AddressString"`
	Type          string `json: "Type"`
}
