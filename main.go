package tgexchbot

import (
	"github.com/btcsuite/btcd/chaincfg"
	"github.com/jfixby/btcharness"
	"github.com/jfixby/btcharness/nodecls"
	"github.com/jfixby/btcharness/walletcls"
	"github.com/jfixby/coinharness"
	"github.com/jfixby/pin"
	"github.com/jfixby/pin/commandline"
)

func main() {
	pin.D("Deploy Bitcoin")
	setup := &Setup{
		WorkingDir: pin.NewTempDir(setupWorkingDir(), "simpleregtest").MakeDir(),
	}
	wEXE := &commandline.ExplicitExecutablePathString{
		PathString: "btcwallet",
	}
	consoleWalletFactory := &walletcls.ConsoleWalletFactory{
		WalletExecutablePathProvider: wEXE,
	}

	//mainnetWalletFactory := consoleWalletFactory
	testnetWalletFactory := consoleWalletFactory

	dEXE := &commandline.ExplicitExecutablePathString{
		PathString: "btcd",
	}
	nodeFactory := &nodecls.ConsoleNodeFactory{
		NodeExecutablePathProvider: dEXE,
	}

	portManager := &coinharness.LazyPortManager{
		BasePort: 30000,
	}

	testSeed := btcharness.NewTestSeed

	setup.btcNet = &coinharness.ChainWithMatureOutputsSpawner{
		WorkingDir:        setup.WorkingDir.Path(),
		DebugNodeOutput:   true,
		DebugWalletOutput: true,
		NumMatureOutputs:  0,
		NetPortManager:    portManager,
		WalletFactory:     testnetWalletFactory,
		NodeFactory:       nodeFactory,
		ActiveNet:         &btcharness.Network{&chaincfg.RegressionNetParams},
		CreateTempWallet:  true,
		NewTestSeed:       testSeed,
	}

	h := setup.btcNet.NewInstance("").(*coinharness.Harness)

	coinharness.DeploySimpleChain(setup.btcNet, h)
}

func setupWorkingDir() string {
	//testWorkingDir, err := ioutil.TempDir("", "integrationtest")
	//if err != nil {
	//	fmt.Println("Unable to create working dir: ", err)
	//	os.Exit(-1)
	//}
	//return testWorkingDir
	return ""
}
