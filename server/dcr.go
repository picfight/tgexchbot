package server

import (
	"github.com/jfixby/pin"
	"github.com/jfixby/pin/lang"
	dcrutil "github.com/decred/dcrd/dcrutil"
	"github.com/picfight/tgexchbot/connect"
	"os"
)


func (s HttpsServer) obrtainDCRAddress(walletAccountName string) string {
	address := &AddressString{
		Type: "DCR",
	}
	{
		client, err := connect.DCRWallet(s.config)
		lang.CheckErr(err)

		pin.D("Checking DCR account", walletAccountName)
		key := os.Getenv(DCRWKEY)
		err = client.WalletPassphrase(key, 10000000)
		lang.CheckErr(err)
		_, err = client.GetAccountAddress(walletAccountName)
		if err != nil {
			pin.D("Error", err)
			pin.D("Creating DCR account", walletAccountName)
			err := client.CreateNewAccount(walletAccountName)
			lang.CheckErr(err)
		}

		addressResult, err := client.GetNewAddress(walletAccountName)
		lang.CheckErr(err)
		client.Disconnect()

		address.AddressString = addressResult.String()
	}
	return toJson(address)
}

func (s HttpsServer) getBalanceDCR(address string, min_confirmations int) string {
	result, err := s.executeGetBalanceDCR(address, min_confirmations)
	lang.CheckErr(err)
	js := toJson(result)
	return js
}

func (s HttpsServer) executeGetBalanceDCR(address string, min_confirmations int) (*DCRBalance, error) {
	result := &DCRBalance{}
	{
		result.DCRAddress.Type = "DCR"
		result.DCRAddress.AddressString = address
	}
	{
		client, err := connect.DCRWallet(s.config)
		defer client.Disconnect()
		if err != nil {
			return nil, err
		}

		addr, e := dcrutil.DecodeAddress(address)
		if e != nil {
			return nil, err
		}

		validation, e := client.ValidateAddress(addr)
		if e != nil {
			return nil, err
		}

		resolvedAccountName := validation.Account

		balance, err := client.GetBalanceMinConf(resolvedAccountName, min_confirmations)
		if err != nil {
			return nil, err
		}

		result.Spendable.Value = balance.Balances[0].Spendable
		result.Unconfirmed.Value = balance.Balances[0].Unconfirmed

		result.ResolvedAccountName = resolvedAccountName
	}
	return result, nil
}

func (s HttpsServer) TransferDCR(DCR_FromAccountAddress string, DCR_ToAddress string, amountFloat AmountDCR) string {
	result, err := s.executeTransferDCR(DCR_FromAccountAddress, DCR_ToAddress, amountFloat)
	if result == nil {
		lang.CheckErr(err)
	}
	return toJson(result)
}

func (s HttpsServer) executeTransferDCR(DCR_FromAccountAddress string, DCR_ToAddress string, amountFloat AmountDCR) (*TransactionResult, error) {
	result := &TransactionResult{}

	client, err := connect.DCRWallet(s.config)
	defer client.Disconnect()
	if err != nil {
		return nil, err
	}

	{
		addr, e := dcrutil.DecodeAddress(DCR_FromAccountAddress)
		if e != nil {
			return nil, err
		}
		result.DCR_FromAccountAddress = DCR_FromAccountAddress

		validation, e := client.ValidateAddress(addr)
		if e != nil {
			return nil, err
		}
		result.DCR_ResolvedAccountName = validation.Account
	}
	toAddr, e := dcrutil.DecodeAddress(DCR_ToAddress)
	if e != nil {
		return nil, err
	}
	result.DCR_ToAddress = DCR_ToAddress

	amount, err := dcrutil.NewAmount(amountFloat.Value)
	if err != nil {
		return nil, err
	}

	result.DCR_Amount = amountFloat

	{
		key := os.Getenv(DCRWKEY)
		err = client.WalletPassphrase(key, 10000000)
		if err != nil {
			return nil, err
		}
	}

	hash, err := client.SendFrom(result.DCR_ResolvedAccountName, toAddr, amount)

	if hash != nil {
		result.DCR_TransactionReceipt = hash.String()
	}

	if err != nil {
		result.Success = false
		result.ErrorMessage = err.Error()
	} else {
		result.Success = true
	}
	return result, err

}