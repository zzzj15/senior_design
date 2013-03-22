package com.example.seniordesignapp;
/**
 * @author zsljulius
 * Acceleration Class contains the x, y, z accelerations and a timestamp. It is an 
 * abstraction of a database row.
 */
public class Acceleration{
	private double x;
	private double y;
	private double z;
	private long timestamp;
	
	public Acceleration(double x,double y, double z, long timestamp){
		this.x = x;
		this.y = y;
		this.z = z;
		this.timestamp = timestamp;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getZ() {
		return z;
	}
	public void setZ(double z) {
		this.z = z;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public String toString(){
		return "X: "+x+" Y: "+y+" Z: "+z + ". At time: " + timestamp;
	}
}

