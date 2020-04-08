package server

type Rate struct {
	//	Host            string `json: "host"`
	CirculatingSupplyCoins float64 `json: "CirculatingSupplyCoins"`
	AvailablePFC           float64 `json: "AvailablePFC"`
	BTCperPFC                float64 `json: "BTCperPFC"`
}

type AddressString struct {
	AddressString string `json: "address_string"`
	Type string `json: "type"`
}

//type RPCConfig struct {
//	Host            string `json: "host"`
//	User            string `json: "user"`
//	Pass            string `json: "password"`
//	CertificateFile string `json: "certfile"`
//}
