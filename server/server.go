package server

import (
	"fmt"
	"github.com/jfixby/pin"
	"io"
	"log"
	"net/http"
)

type HttpsServer struct {
	config HttpsServerConfig
	//handler func(s *HttpsServer) (w http.ResponseWriter, r *http.Request)
}

func (s HttpsServer) Start() {
	http.HandleFunc("/", s.Handler)

	pin.D(fmt.Sprintf("Starting server at port: %v", s.config.Port))

	// Use ListenAndServeTLS() instead of ListenAndServe() which accepts two extra parameters.
	// We need to specify both the certificate file and the key file (which we've named
	// https-server.crt and https-server.key).
	err := http.ListenAndServeTLS(fmt.Sprintf(":%v", s.config.Port), s.config.CertificateFile, s.config.CertificateKeyFile, nil)
	if err != nil {
		log.Fatal(err)
	}
}

type HttpsServerConfig struct {
	Port               int    `json: "Port"`
	AccessKey          string `json: "accessKey"`
	CertificateFile    string `json: "certfile"`
	CertificateKeyFile string `json: "certfile_key"`
}

func NewServer(cfg HttpsServerConfig) *HttpsServer {
	return &HttpsServer{config: cfg,}
}

func (s *HttpsServer) Handler(w http.ResponseWriter, r *http.Request) {
	w.Header().Add("Content-Type", "application/json")
	io.WriteString(w, `{"status":"ok"}`)
}
