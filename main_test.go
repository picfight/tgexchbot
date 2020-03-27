package tgexchbot

import (
	"flag"
	"github.com/jfixby/pin"
	"os"
	"testing"
)

// TestMain is executed by go-test, and is
// responsible for setting up and disposing test environment.
func TestMain(m *testing.M) {
	flag.Parse()
	pin.D("Hello!")

	exitCode := m.Run()
	pin.VerifyNoAssetsLeaked()
	os.Exit(exitCode)
}
