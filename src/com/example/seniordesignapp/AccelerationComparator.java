package com.example.seniordesignapp;

import java.util.Comparator;

public enum AccelerationComparator implements Comparator<Acceleration>{
	X_SORT{
		@Override
		public int compare(Acceleration o1, Acceleration o2) {
	        return (int) (o1.getX()-o2.getX());
		}
	},
	Y_SORT{
		@Override
		public int compare(Acceleration o1, Acceleration o2) {
	        return (int) (o1.getY()-o2.getY());
		}
	},
	Z_SORT{
		@Override
		public int compare(Acceleration o1, Acceleration o2) {
	        return (int) (o1.getZ()-o2.getZ());
		}	
	},
	TIMESTAMP_SORT{
		@Override
		public int compare(Acceleration o1, Acceleration o2) {
	        return (int) (o1.getTimestamp()-o2.getTimestamp());
		}
	};
}
