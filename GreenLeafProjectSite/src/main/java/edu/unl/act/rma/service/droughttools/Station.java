package edu.unl.act.rma.service.droughttools;

import java.util.Vector;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Station")
public class Station {
	private String name;
	private String id;
	private int startYear;
	private int endYear;
	private float awc;
	private Location location;
	private float[][] precipitationData;
	private float[][] temperatureData;
	private float[][] highTemperatureData;
	private float[] temperatureAverage;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public float getAwc() {
		return awc;
	}
	public void setAwc(float awc) {
		this.awc = awc;
	}
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	public float[][] getPrecipitationData() {
		return precipitationData;
	}
	public void setPrecipitationData(float[][] precipitationData) {
		this.precipitationData = precipitationData;
	}
	public float[][] getTemperatureData() {
		return temperatureData;
	}
	public void setTemperatureData(float[][] temperatureData) {
		this.temperatureData = temperatureData;
	}
	public float[][] getHighTemperatureData() {
		return highTemperatureData;
	}
	public void setHighTemperatureData(float[][]  highTemperatureData) {
		this.highTemperatureData = highTemperatureData;
	}
	public float[] getTemperatureAverage() {
		return temperatureAverage;
	}
	public void setTemperatureAverage(float[] temperatureAverage) {
		this.temperatureAverage = temperatureAverage;
	}
	public int getStartYear() {
		return startYear;
	}
	public void setStartYear(int startYear) {
		this.startYear = startYear;
	}
	public int getEndYear() {
		return endYear;
	}
	public void setEndYear(int endYear) {
		this.endYear = endYear;
	}
	public float[] getPrecipDataArray(){
		if (precipitationData.length < 1) {
			return null;
		}
		Vector<Float> precipArray = new Vector<Float>();
		for(int i = 0; i < precipitationData.length; i++){
			for(float num : precipitationData[i]){
				precipArray.add(num);
			}
		}
		float[] returnArray = new float[precipArray.size()];
		int count = 0;
		for(float num: precipArray){
			returnArray[count] = num;
			count++;
		}
		return returnArray;
	}
	public float[] getTempDataArray(){
		if (temperatureData.length < 1) {
			return null;
		}
		Vector<Float> tempArray = new Vector<Float>();
		for(int i = 0; i < temperatureData.length; i++){
			for(float num : temperatureData[i]){
				tempArray.add(num);
			}
		}
		float[] returnArray = new float[tempArray.size()];
		int count = 0;
		for(float num: tempArray){
			returnArray[count] = num;
			count++;
		}
		return returnArray;
	}
	
}
