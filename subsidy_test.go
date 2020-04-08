package main

import (
	"github.com/picfight/picfightcoin"
	"testing"
)

func TestSubsidy(t *testing.T) {
	s := picfightcoin.PicFightCoinSubsidy()
	{
		block_subsidy := s.EstimateSupply(0)
		checkSubsidy(t, block_subsidy, 0, 0)
	}
	{
		block_subsidy := s.EstimateSupply(1)
		prem := s.PreminedCoins().AtomsValue
		checkSubsidy(t, block_subsidy, 1, prem)
	}
	{
		block_subsidy := s.EstimateSupply(10)
		checkSubsidy(t, block_subsidy, 10, 2035181943229)
	}
	{
		block_subsidy := s.EstimateSupply(42673)
		checkSubsidy(t, block_subsidy, 42673, 2417050090575)
	}
}

func checkSubsidy(t *testing.T, blockSubsidy int64, blockIndex int64, expectedSubsidy int64) {
	if expectedSubsidy != blockSubsidy {
		t.Errorf("blockSubsidy=%v blockIndex=%v expectedSubsidy=%v", blockSubsidy, blockIndex, expectedSubsidy)
		t.FailNow()
	}

}
