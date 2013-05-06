package com.example.seniordesignapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import android.util.Log;

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
	private final String DEBUG_TAG = Feature.class.getSimpleName();
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
	private double[] binDist = new double[NUM_BIN*3];
	
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
		
		//Transform the accelerations data into vertical and horizontal
		/*Step 1
			Calculate Average*/
		average[0] = statsX.getMean();
		average[1] = statsY.getMean();
		average[2] = statsZ.getMean();
		/*Step 2
			unit vector*/
		double mag = Math.sqrt(average[0]*average[0]+average[1]*average[1]+average[2]*average[2]);
		double[] unitG = {average[0]/mag,average[1]/mag,average[2]/mag};
		/*Step 3
		 * remove from the frame by subtracting G*/
		for (Acceleration e:accelerations){
			e.setX(e.getX()-average[0]);
			e.setY(e.getY()-average[1]);
			e.setZ(e.getZ()-average[2]);
		}
		/*Step 4
		 * calculate the signed length of the component of each vector
		 */
		DescriptiveStatistics statsV = new DescriptiveStatistics();
		DescriptiveStatistics statsH = new DescriptiveStatistics();
		for (Acceleration e:accelerations){
			double[] a = {e.getX(),e.getY(),e.getZ()};
			double v = -dotProduct(a,unitG);
			double h = Math.sqrt(Math.pow(a[0] - v*unitG[0], 2)
					+Math.pow(a[1] - v*unitG[1], 2)
					+Math.pow(a[2] - v*unitG[2], 2));
			e.setV(v);
			e.setH(h);
			statsV.addValue(v);
			statsH.addValue(h);
//			Log.d(DEBUG_TAG,"v is"+v+" h is "+h);
		}
		
		//Calculate Standard Deviation
		std[0] = statsV.getStandardDeviation();
		std[1] = statsH.getStandardDeviation();
		
		//Calculate Average Absolute Difference, special computation required 
		//for the underlying values, so have to get them from the stats datasets
		double[] v = statsV.getValues();
		double[] h = statsH.getValues();
		
		avgAbsDiff[0] = getAvgAbsDiff(v);
		avgAbsDiff[1] = getAvgAbsDiff(h);
		//Calculate Average Resultant Acceleration
		avgRlstAccel = getAvgRlstAccel(v,h);
		
		//Find Time Peaks
		timePeaks = getTimePeaks(accelerations,statsV,statsH);
		
		//Find Binned Distribution
		Frequency fv = new Frequency();
		Frequency fh = new Frequency();
		for (int i=0;i<v.length;i++){
			fv.addValue(v[i]);
			fh.addValue(h[i]);
		}
		getBinDist(statsV,fv,0);
		getBinDist(statsH,fh,10);
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
	private double getAvgRlstAccel(double[] x,double[] y){
		double sum = 0;
		for (int i =0;i<x.length;i++){
			sum = Math.sqrt(x[i]*x[i]+y[i]*y[i]);
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
	private double[] getTimePeaks(List<Acceleration> accelerations,DescriptiveStatistics statsX,DescriptiveStatistics statsY){
		double timePeaks[] = new double[3];
//		timePeaks[0] = peakDet(accelerations,'x',0.05);
//		timePeaks[1] =peakDet(accelerations,'y',0.05);
//		timePeaks[2] =peakDet(accelerations,'z',0.05);
		double xthres=0,ythres=0;
		if(statsX.getMax()-statsX.getMin()<0.7)
			xthres = 1;
		else
			xthres = statsX.getMax()-statsX.getMin();
		if(statsY.getMax()-statsY.getMin()<0.7)
			ythres = 1;
		else
			ythres = statsY.getMax()-statsY.getMin();
		
		timePeaks[0] = peakDet(accelerations,'v',0.5*xthres);//50% of the full range as threshold
		timePeaks[1] =peakDet(accelerations,'h',0.5*ythres);
		return timePeaks;
		
	}
	private void getBinDist(DescriptiveStatistics stats,Frequency f, int index){
		double max = stats.getMax();
		double min = stats.getMin();
		double binSize = (max-min)/NUM_BIN;
		long numElements = stats.getN();
		for (int i=index+1;i<=NUM_BIN+index;i++){
			double minsize = (min+(i-1)*binSize);
			//Log.d(DEBUG_TAG,"Min+("+i+"-1)*binSize = "+minsize);
			//Log.d(DEBUG_TAG,"Frequency "+i+" = "+f.getCumFreq(min+(i-1)*binSize));
			if (i==index+1){
				binDist[i-1] =  f.getCumFreq(min+(i-index)*binSize);
			}
			else{
				binDist[i-1] =  f.getCumFreq(min+(i-index)*binSize)-f.getCumFreq(min+(i-index-1)*binSize);
			}
			//Log.d(DEBUG_TAG,"Freqency "+i+" = "+binDist[i-1]);
		}
	}
	private double dotProduct(double[] x,double[] y){
		double sum = 0.0;
		for(int i=0;i<x.length;i++)
			sum += x[i]*y[i];
		return sum;
	}
	private double peakDet(List<Acceleration> accelerations,char pos, double threshold){
		double mn = Double.MAX_VALUE;
		double mx;
		List<Acceleration> maxTab = new ArrayList<Acceleration>();
//		Acceleration MaxAccel = new Acceleration(Double.MIN_VALUE,Double.MIN_VALUE,);
		
		Acceleration maxAccel = accelerations.get(0);
		boolean lookForMax = true;
		for (int i=1;i<accelerations.size();i++){
			Acceleration v = accelerations.get(i);
			double vx = 0;
			switch (pos){
				case 'v':
					vx = v.getV();
					mx = maxAccel.getV();
					break;
				case 'h':
					vx = v.getH();
					mx = maxAccel.getH();
					break;
				default:
					mx = 0;
					break;
			}
//			Log.d(DEBUG_TAG,"mx is "+mx);
			if (vx > mx){
				mx = vx;
				maxAccel = v;
			}
			if (lookForMax){
				if (vx < mx-threshold){ //point < max - threshold
					maxTab.add(maxAccel);
//					Log.d(DEBUG_TAG,"max accel is x" + maxAccel.getX()+"max accel y is"+maxAccel.getY()+" max accel z is"+maxAccel.getZ());
					mn = vx;
					lookForMax = false;
				}
			}
			else{
				if (vx > mn+threshold){ // point > min + threshold
					mx = vx;
					lookForMax = true;
				}
			}
		}
		if (maxTab.size()<=1){
//			Log.d(DEBUG_TAG,"size is "+maxTab.size()+"!!!!!!!!!!!!!!!!!!!!");
			return 0;
		}
		else{
//			Log.d(DEBUG_TAG,"size is "+maxTab.size()+"!!!!!!!!!!!!!!!!!!!!");
			
			long diffSum=0;
			int count=0;
			Acceleration prev=maxTab.get(0);
			for(Acceleration cur : maxTab){
				diffSum +=cur.getTimestamp()-prev.getTimestamp();
				count++;
//				Log.d(DEBUG_TAG,"time stamp is "+cur.getTimestamp()+" count "+count+" prev "+prev.getTimestamp());
//				Log.d(DEBUG_TAG,pos + "cur time is "+cur.getTimestamp()+"prev x is "+prev.getTimestamp());
				prev = cur;
			}
//			
//			Iterator<Acceleration> iterator = maxTab.iterator();
//			Acceleration cur,prev;
//			prev = iterator.next();
//			long diffSum = 0;
//			
//			int count=0;
//			
//			while (iterator.hasNext()) {
//				cur = iterator.next();
//				diffSum += cur.getTimestamp()-prev.getTimestamp();
//				
//				Log.d(DEBUG_TAG,"time stamp is "+cur.getTimestamp()+" count "+count+" prev "+prev.getTimestamp());
//				Log.d(DEBUG_TAG,"cur x is "+cur.getX()+"prev x is "+prev.getX());
//				count++;
//				prev = cur;
//			}
			return (double) diffSum/maxTab.size(); 
		}
	}
	@Override
	public String toString(){
		return "Dummy";
	}
}

