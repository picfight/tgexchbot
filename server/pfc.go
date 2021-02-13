package server

import (
	"github.com/jfixby/pin"
	"github.com/jfixby/pin/lang"
	pfcutil "github.com/picfight/pfcd/dcrutil"
	"github.com/picfight/tgexchbot/connect"
	"os"
)


func (s HttpsServer) obrtainPFCAddress(walletAccountName string) string {
	address := &AddressString{
		Type: "PFC",
	}
	{
		client, err := connect.PFCWallet(s.config)
		lang.CheckErr(err)

		pin.D("Checking PFC account", walletAccountName)
		key := os.Getenv(PFCWKEY)
		err = client.WalletPassphrase(key, 10000000)
		lang.CheckErr(err)
		_, err = client.GetAccountAddress(walletAccountName)
		if err != nil {
			pin.D("Error", err)
			pin.D("Creating PFC account", walletAccountName)
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

func (s HttpsServer) getBalancePFC(address string, min_confirmations int) string {
	result, err := s.executeGetBalancePFC(address, min_confirmations)
	lang.CheckErr(err)
	js := toJson(result)
	return js
}

func (s HttpsServer) executeGetBalancePFC(address string, min_confirmations int) (*PFCBalance, error) {
	result := &PFCBalance{}
	{
		result.PFCAddress.Type = "PFC"
		result.PFCAddress.AddressString = address
	}
	{
		client, err := connect.PFCWallet(s.config)
		defer client.Disconnect()
		if err != nil {
			return nil, err
		}

		addr, e := pfcutil.DecodeAddress(address)
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

func (s HttpsServer) TransferPFC(PFC_FromAccountAddress string, PFC_ToAddress string, amountFloat AmountPFC) string {
	result, err := s.executeTransferPFC(PFC_FromAccountAddress, PFC_ToAddress, amountFloat)
	if result == nil {
		lang.CheckErr(err)
	}
	return toJson(result)
}

func (s HttpsServer) executeTransferPFC(PFC_FromAccountAddress string, PFC_ToAddress string, amountFloat AmountPFC) (*TransactionResult, error) {
	result := &TransactionResult{}

	client, err := connect.PFCWallet(s.config)
	defer client.Disconnect()
	if err != nil {
		return nil, err
	}

	{
		addr, e := pfcutil.DecodeAddress(PFC_FromAccountAddress)
		if e != nil {
			return nil, err
		}
		result.PFC_FromAccountAddress = PFC_FromAccountAddress

		validation, e := client.ValidateAddress(addr)
		if e != nil {
			return nil, err
		}
		result.PFC_ResolvedAccountName = validation.Account
	}
	toAddr, e := pfcutil.DecodeAddress(PFC_ToAddress)
	if e != nil {
		return nil, err
	}
	result.PFC_ToAddress = PFC_ToAddress

	amount, err := pfcutil.NewAmount(amountFloat.Value)
	if err != nil {
		return nil, err
	}

	result.PFC_Amount = amountFloat

	{
		key := os.Getenv(PFCWKEY)
		err = client.WalletPassphrase(key, 10000000)
		if err != nil {
			return nil, err
		}
	}

	hash, err := client.SendFrom(result.PFC_ResolvedAccountName, toAddr, amount)

	if hash != nil {
		result.PFC_TransactionReceipt = hash.String()
	}

	if err != nil {
		result.Success = false
		result.ErrorMessage = err.Error()
	} else {
		result.Success = true
	}
	return result, err

}