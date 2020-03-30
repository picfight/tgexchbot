package tgexchbot

import (
	"encoding/json"
	"github.com/jfixby/pin"
	"github.com/jfixby/pin/lang"
	"io/ioutil"
	"path/filepath"
	"testing"
)

func TestCfgFile(t *testing.T) {
	filePath, err := filepath.Abs("test_config.json")
	lang.CheckErr(err)

	file, err := ioutil.ReadFile(filePath)
	lang.CheckErr(err)
	pin.D("file", file)

	data := ConfigJson{}
	err = json.Unmarshal([]byte(file), &data)

	lang.CheckErr(err)

	pin.D("cfg", data)

}
