
package org.picfight.exchbot.lambda;

public class ChartData {
// Title string `json: "Title"`
// DataPoints []DataPoint `json: "DataPoints"`
// ImgWidth float64 `json: "ImgWidth"`
// ImgHeight float64 `json: "ImgHeight"`
// X_Label string `json: "X_Label"`
// Y_Label string `json: "Y_Label"`

	public String Title;
	public String X_Label;
	public String Y_Label;
	public Double ImgWidth;
	public Double ImgHeight;
	public DataPoint[] DataPoints;

}
