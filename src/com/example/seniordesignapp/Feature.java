package com.example.seniordesignapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/* This is an abstraction of a feature that is to be stored in our features database.
 * The statistics are calculated based on 200 readings contained within each 10-second segment. 
 * It should compute the following features:
 * 1. Average[3]
 * 2. Standard Deviation[3]
 * 3. Average  Absolute  Difference[3]
 * 4. Average  Resultant  Acceleration[1]
 * 5. Time  Between  Peaks[3]
 * 6. Binned Distribution[30]
 * REFERENCE: Activity Recognition using cell phone accelerometers Jennifer by
 * R. Kwapisz, Gary M. Weiss, Samuel A. Moore 
 */

/**
 * @author zsljulius
 *
 */
public class Feature {
	private final int NUM_BIN=10;
	
	private double[] average = new double[3];
	private double[] std = new double[3];
	private double[] avgAbsDiff = new double[3];
	private double avgRlstAccel;
	private double[] timePeaks = new double[3];
	public double[] getAverage() {
		return average;
	}
	public double[] getStd() {
		return std;
	}
	public double[] getAvgAbsDiff() {
		return avgAbsDiff;
	}
	public double getAvgRlstAccel() {
		return avgRlstAccel;
	}
	public double[] getTimePeaks() {
		return timePeaks;
	}
	public double[] getBinDist() {
		return binDist;
	}
	private double[] binDist = new double[NUM_BIN];
	
	/**
	 * Construct a feature to be stored in the database.
	 * @param accelerations A list of accelerations retrived 
	 */
	public Feature(List<Acceleration> accelerations){
		DescriptiveStatistics statsX = new DescriptiveStatistics();
		DescriptiveStatistics statsY = new DescriptiveStatistics();
		DescriptiveStatistics statsZ = new DescriptiveStatistics();
		for (Acceleration e:accelerations){
			statsX.addValue(e.getX());
			statsY.addValue(e.getY());
			statsZ.addValue(e.getZ());
		}
		//Calculate Average
		average[0] = statsX.getMean();
		average[1] = statsY.getMean();
		average[2] = statsZ.getMean();
		//Calculate Standard Deviation
		std[0] = statsX.getStandardDeviation();
		std[1] = statsY.getStandardDeviation();
		std[2] = statsZ.getStandardDeviation();
		
		//Calculate Average Absolute Difference, special computation required 
		//for the underlying values, so have to get them from the stats datasets
		double[] x = statsX.getValues();
		double[] y = statsY.getValues();
		double[] z = statsZ.getValues();
		
		avgAbsDiff[0] = getAvgAbsDiff(x);
		avgAbsDiff[1] = getAvgAbsDiff(y);
		avgAbsDiff[2] = getAvgAbsDiff(z);
		//Calculate Average Resultant Acceleration
		avgRlstAccel = getAvgRlstAccel(x,y,z);
		
		//Find Time Peaks
		timePeaks = getTimePeaks(accelerations);
		
		//Find Binned Distribution
		Frequency fx = new Frequency();
		Frequency fy = new Frequency();
		Frequency fz = new Frequency();
		for (int i=0;i<x.length;i++){
			fx.addValue(x[i]);
			fy.addValue(y[i]);
			fz.addValue(z[i]);
		}
		getBinDist(statsX,fx);
		getBinDist(statsY,fy);
		getBinDist(statsZ,fz);
	}
	/**
	 * @param dataset The x,y,z dataset
	 * @return the average absolute difference as defined in the paper
	 */
	private double getAvgAbsDiff(double[] dataset){
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (double e:dataset){
			stats.addValue(e-average[0]);
		}
		return stats.getMean();
	}
	/**
	 * @param x x accelerations
	 * @param y y accelerations
	 * @param z z accelerations
	 * @return Avg(sqrt(x[i]^2+y[i]^2+z[i]^2)) over all i from 1 to 200;
	 */
	private double getAvgRlstAccel(double[] x,double[] y,double[] z){
		double sum = 0;
		for (int i =0;i<x.length;i++){
			sum = Math.sqrt(x[i]*x[i]+y[i]*y[i]+z[i]*z[i]);
		}
		return sum/x.length;
		
	}
	/**
	 * To  estimate  this  value,  for  each example  we  first  identify  
	 * all  of  the  peaks  in  the  wave  using  a heuristic method and 
	 * then identify the highest peak for each axis. We then set a threshold 
	 * based on a percentage of this value and find the other peaks that met 
	 * or exceed this threshold; if no peaks meet this criterion then the threshold 
	 * is lowered until we find at least three peaks. We then measure the time between 
	 * successive peaks and calculate the average. For samples where at least three 
	 * peaks could not be found, the time between peaks is marked as unknown.
	 *  
	 * @return
	 */
	private double[] getTimePeaks(List<Acceleration> accelerations){
		double timePeaks[] = new double[3];
		timePeaks[0] = peakDet(accelerations,'x',0.05);
		timePeaks[1] =peakDet(accelerations,'y',0.05);
		timePeaks[2] =peakDet(accelerations,'z',0.05);
		return timePeaks;
		
	}
	private void getBinDist(DescriptiveStatistics stats,Frequency f){
		double binSize = (stats.getMax()-stats.getMin())/NUM_BIN;
		for (int i=1;i<=NUM_BIN;i++){
			binDist[i-1] =  f.getCumFreq(i*binSize);
		}
	}
	private double peakDet(List<Acceleration> accelerations,char pos, double threshold){
		double mn = Double.MAX_VALUE;
		double mx;
		List<Acceleration> maxTab = new ArrayList<Acceleration>();
		Acceleration maxAccel = accelerations.get(0);
		boolean lookForMax = true;
		for (int i=1;i<accelerations.size();i++){
			Acceleration v = accelerations.get(i);
			double vx = 0;
			switch (pos){
				case 'x':
					vx = v.getX();
					mx = maxAccel.getX();
					break;
				case 'y':
					vx = v.getY();
					mx = maxAccel.getY();
					break;
				case 'z':
					vx = v.getZ();
					mx = maxAccel.getZ();
					break;
				default:
					mx = 0;
					break;
			}
			if (vx > mx){
				mx = vx;
				maxAccel = v;
			}
			if (lookForMax){
				if (vx < mx-threshold){
					maxTab.add(maxAccel);
					mn = vx;
					lookForMax = false;
				}
			}
			else{
				if (vx > mn+threshold){
					mx = vx;
					lookForMax = true;
				}
			}
		}
		if (maxTab.size()<=1)
			return 0;
		else{
			Iterator<Acceleration> iterator = maxTab.iterator();
			Acceleration cur,prev;
			prev = iterator.next();
			long diffSum = 0;
			while (iterator.hasNext()) {
				cur = iterator.next();
				diffSum += cur.getTimestamp()-prev.getTimestamp();
				prev = cur;
			}
			return diffSum/maxTab.size();
		}
	}
	@Override
	public String toString(){
		return "Dummy";
	}
}

