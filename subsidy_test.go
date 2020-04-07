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

func checkSubsidy(t *testing.T, block_subsidy int64, block_index int64, expected_subsidy int64) {
	if expected_subsidy != block_subsidy {
		t.Errorf("block_subsidy=%v block_index=%v expected_subsidy=%v", block_subsidy, block_index, expected_subsidy)
		t.FailNow()
	}

}
