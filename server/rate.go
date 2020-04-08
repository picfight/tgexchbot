package server

type Rate struct {
	//	Host            string `json: "host"`
	CirculatingSupplyCoins float64 `json: "CirculatingSupplyCoins"`
	AvailablePFC           float64 `json: "AvailablePFC"`
	PFC2BTC                float64 `json: "PFC2BTC"`
}

//type RPCConfig struct {
//	Host            string `json: "host"`
//	User            string `json: "user"`
//	Pass            string `json: "password"`
//	CertificateFile string `json: "certfile"`
//}
