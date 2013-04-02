package com.example.tests;
import org.apache.commons.math3.stat.Frequency;

public class featureTester {
	public static void main(String args[]){
		Frequency f = new Frequency();
		f.addValue(-1);
		f.addValue(0);
		f.addValue(1);
		f.addValue(2);
		f.addValue(3);
		System.out.println(f.getCumFreq(3));
		
	}
}
