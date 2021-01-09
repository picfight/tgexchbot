package server

import (
	"github.com/pplcc/plotext/custplotter"
	"gonum.org/v1/plot"
	"gonum.org/v1/plot/vg"
	"io/ioutil"
	"math"
	"math/rand"
	"time"
)

func Plot(data ChartData) ([]byte, error) {
	n := 60
	fakeTOHLCVs := CreateTOHLCVExampleData(n)
	p, err := plot.New()
	if err != nil {
		return []byte{}, err
	}

	p.Title.Text = data.Title
	p.X.Label.Text = data.X_Label
	p.Y.Label.Text = data.Y_Label
	p.X.Tick.Marker = plot.TimeTicks{Format: "2006-01-02\n15:04:05"}

	bars, err := custplotter.NewCandlesticks(fakeTOHLCVs)
	if err != nil {
		return []byte{}, err
	}

	p.Add(bars)

	file := "candlesticks.png"
	err = p.Save(vg.Length(data.ImgWidth), vg.Length(data.ImgHeight), file)
	if err != nil {
		return []byte{}, err
	}

	bytes, err := ioutil.ReadFile(file)

	return bytes, err
}

// CreateTOHLCVExampleData generates and returns some artificial TOHLCV data for testing and demo purpose
func CreateTOHLCVExampleData(n int) custplotter.TOHLCVs {
	rnd := rand.New(rand.NewSource(1))
	m := 4 * n
	fract := make([]float64, m)
	for i := 0; i < m; i++ {
		fract[i] = 100
	}
	stat1 := 0.0
	stat2 := 0.0
	for k := m; k > 0; k = k / 2 {
		j := 0
		for i := 0; i < m; i++ {
			if j == 0 {
				j = k
				stat2 = stat1
				stat1 = 10.0 * (float64(k)/float64(m) + 0.02) * (2.0*rnd.Float64() - 1.0)
			}
			fract[i] += float64(k-j)/float64(k)*stat1 + float64(j)/float64(k)*stat2
			j--
		}
	}

	data := make(custplotter.TOHLCVs, n)

	loc, _ := time.LoadLocation("America/New_York")
	for i := range data {
		data[i].T = float64(time.Date(2000, 01, 02, 03, 04, 05, 0, loc).Add(time.Duration(i) * time.Minute).Unix())
		data[i].O = fract[4*i]
		data[i].H = math.Max(math.Max(fract[4*i], fract[4*i+1]), math.Max(fract[4*i+2], fract[4*i+3]))
		data[i].L = math.Min(math.Min(fract[4*i], fract[4*i+1]), math.Min(fract[4*i+2], fract[4*i+3]))
		data[i].C = fract[4*i+3]

		data[i].V = (data[i].H - data[i].L + math.Abs(data[i].C-data[i].O)) * 100 // just use this as a fake volume
	}
	return data
}
