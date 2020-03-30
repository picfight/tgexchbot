package main

import (
	"fmt"
	"github.com/jfixby/coinharness"
	"github.com/jfixby/pin"
	"path/filepath"
)

type Setup struct {
	// WorkingDir defines test setup working dir
	WorkingDir *pin.TempDirHandler
	btcNet     *coinharness.ChainWithMatureOutputsSpawner
}

func NewInstance(harnessName string, testSetup *coinharness.ChainWithMatureOutputsSpawner) pin.Spawnable {
	harnessFolderName := "harness-" + harnessName
	pin.AssertNotNil("ConsoleNodeFactory", testSetup.NodeFactory)
	pin.AssertNotNil("ActiveNet", testSetup.ActiveNet)
	pin.AssertNotNil("WalletFactory", testSetup.WalletFactory)

	harnessFolder := filepath.Join(testSetup.WorkingDir, harnessFolderName)
	walletFolder := filepath.Join(harnessFolder, "wallet")
	nodeFolder := filepath.Join(harnessFolder, "node")

	p2p := testSetup.NetPortManager.ObtainPort()
	nodeRPC := testSetup.NetPortManager.ObtainPort()
	walletRPC := testSetup.NetPortManager.ObtainPort()

	localhost := "127.0.0.1"

	nodeConfig := &coinharness.TestNodeConfig{
		P2PHost: localhost,
		P2PPort: p2p,

		NodeRPCHost: localhost,
		NodeRPCPort: nodeRPC,

		NodeUser:     "node.user",
		NodePassword: "node.pass",

		ActiveNet: testSetup.ActiveNet,

		WorkingDir: nodeFolder,
	}

	walletConfig := &coinharness.TestWalletConfig{
		Seed:        testSetup.NewTestSeed(0),
		NodeRPCHost: localhost,
		NodeRPCPort: nodeRPC,

		WalletRPCHost: localhost,
		WalletRPCPort: walletRPC,

		NodeUser:     "node.user",
		NodePassword: "node.pass",

		WalletUser:     "wallet.user",
		WalletPassword: "wallet.pass",

		ActiveNet:  testSetup.ActiveNet,
		WorkingDir: walletFolder,
	}

	harness := &coinharness.Harness{
		Name:       harnessName,
		Node:       testSetup.NodeFactory.NewNode(nodeConfig),
		Wallet:     testSetup.WalletFactory.NewWallet(walletConfig),
		WorkingDir: harnessFolder,
	}

	pin.AssertTrue("Networks match", harness.Node.Network() == harness.Wallet.Network())

	nodeNet := harness.Node.Network()
	walletNet := harness.Wallet.Network()

	pin.AssertTrue(
		fmt.Sprintf(
			"Wallet net<%v> is the same as Node net<%v>", walletNet, nodeNet),
		walletNet == nodeNet)

	return harness
}
