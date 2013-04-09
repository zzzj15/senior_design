package com.example.seniordesignapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * @author zsljulius
 * This class will handle the construction of arff file if the file already exists. 
 * but if the file doesn't exist, it will generate the headers and write the 
 * file to internal storage. 
 */


public class FeaturesConstructor{
	private final String DEBUG_TAG = FeaturesConstructor.class.getSimpleName();
	private ArrayList<Attribute>      atts;
    private Instances       data;
    private double[]        vals;

    
	private List<Feature> features = new ArrayList<Feature>();
	private List<Acceleration> accelerations = new ArrayList<Acceleration>();
	private SQLiteDatabase mDb;
	private Context mContext;
	private int SAMPLE_SIZE = 1000;
	private final int BIN_SIZE = 10;  
	private Cursor mCursor;
	private FileOutputStream outputStream;
	private String runningFileName = "activity_classification_running.arff";
	private String walkingFileName = "activity_classification_walking.arff";
	private String fileName = "activity_classification.arff";
	private StringBuffer buf=new StringBuffer();
	
	public FeaturesConstructor(Context context){
		mContext = context;
		mDb = new DatabaseHelper(mContext).getWritableDatabase();
	}
	private Instances constructArff(List<Feature> features,String mode){
		atts = new ArrayList<Attribute>();// set up attributes
		char[] labels = {'x','y','z'};
		for (char e: labels){	     // - numeric
		atts.add(new Attribute("avg_"+e));
		atts.add(new Attribute("std_"+e));
		atts.add(new Attribute("avgAbsDiff_"+e));
		atts.add(new Attribute("avgRlstAccel_"+e));
		atts.add(new Attribute("timePeaks_"+e));
		for (int i=1;i<=BIN_SIZE;i++){
			atts.add(new Attribute("binDist_"+e+i));
			}
		}
		atts.add(new Attribute("class",(ArrayList<String>) null)); // - string
		
		data = new Instances("activity", atts, 0);
		
		for (Feature feature:features){
			vals = new double[data.numAttributes()];
			double[] avg = feature.getAverage();
			double[] std = feature.getStd();
			double[] avgAbsDiff = feature.getAvgAbsDiff();
			double avgRlstAccel = feature.getAvgRlstAccel();
			double[] timePeaks = feature.getTimePeaks();
			double[] binDist = feature.getBinDist();
//			for (int i=0;i<2;i++){ //There are in total x,y,z 3 datapoints
			for (int i=0;i<3;i++){ //There are in total x,y,z 3 datapoints
				vals[i*15] = avg[i];
				vals[i*15+1] = std[i];
				vals[i*15+2] = avgAbsDiff[i];
				vals[i*15+3] = avgRlstAccel;
				vals[i*15+4] = timePeaks[i];
				for (int j=0;j<BIN_SIZE*3;j++){
					if(j>=0 && j<=9)
						vals[5+j] = binDist[j];
					else if(j>=10 && j<=19)
						vals[10+j] = binDist[j];
					else
						vals[15+j] = binDist[j];
					//Log.d(DEBUG_TAG,"The binDist["+j+"] = " + binDist[j]);
				}
			}
			//The last attribute is the class running/walking.
			vals[data.numAttributes()-1] = data.attribute(data.numAttributes()-1).addStringValue(mode);
			data.add(new DenseInstance(1.0, vals));
		}
	    return data;
	}
	
	public void constructFeatures(String mode) throws IOException{
		if(mode.equals("running"))
			outputStream = mContext.openFileOutput(runningFileName, Context.MODE_PRIVATE);
		if(mode.equals("walking"))
			outputStream = mContext.openFileOutput(walkingFileName, Context.MODE_PRIVATE);
		
		/* Get the just-recorded accelerations from db and construct a feature from every 200 datapoints*/
		
		mCursor = mDb.rawQuery("SELECT * FROM "+DatabaseHelper.ACCELS_TABLE_NAME 
				+" ORDER BY timestamp DESC",null);
		Log.d(DEBUG_TAG,"number of points returned from database "+mCursor.getCount());
		mCursor.moveToFirst();
		long timeSt=0;
		long endTime = mCursor.getLong(4);
		//Hashtable<Long, Float> ht = new Hashtable<Long, Float>();	
		
		while(!mCursor.isAfterLast()){
			
			timeSt = mCursor.getLong(4);
			//if(!ht.containsKey(timeSt)){
			//ht.put(timeSt, mCursor.getFloat(1));
			accelerations.add(new Acceleration(mCursor.getFloat(1),mCursor.getFloat(2),
					mCursor.getFloat(3),timeSt));
			Log.d(DEBUG_TAG,"time is "+timeSt+"x is "+mCursor.getFloat(1)+" "+mCursor.getFloat(2)+" "+mCursor.getFloat(3));
			//}
			mCursor.moveToNext();
		}
		Log.d(DEBUG_TAG,"time interval is "+(endTime-timeSt));
		mCursor.close();
		/*String debug="Sample Data";
		for (Acceleration e:accelerations){
			debug = debug+String.valueOf(e.getX());
		}
		Log.d(DEBUG_TAG,debug);*/
		for (int i =0;i<accelerations.size()/SAMPLE_SIZE;i++){
			List<Acceleration> samples = accelerations.subList(i*SAMPLE_SIZE, SAMPLE_SIZE*(i+1)-1);
			features.add(new Feature(samples));
		}
		Instances data = constructArff(features,mode);
		outputStream.write(data.toString().getBytes());
		outputStream.close();
		
		
		File rfile = mContext.getFileStreamPath(runningFileName);
		File wfile = mContext.getFileStreamPath(walkingFileName);
		if(rfile.exists()&&wfile.exists()){ //merge two files remove extra header
			outputStream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
			BufferedReader br = new BufferedReader(new FileReader(rfile));
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				outputStream.write(sCurrentLine.getBytes());
				outputStream.write("\n".getBytes());
			}
			br.close();
			br = new BufferedReader(new FileReader(wfile));
			boolean toWrite = false;
			while ((sCurrentLine = br.readLine()) != null) {
				if(toWrite){
					outputStream.write(sCurrentLine.getBytes());
					outputStream.write("\n".getBytes());
				}
				if(sCurrentLine.equals("@data"))
					toWrite=true;
			}
			br.close();
			outputStream.close();
		}
		
		/* We don't need the data for calibration*/
		mDb.execSQL("DROP TABLE " + DatabaseHelper.ACCELS_TABLE_NAME);
		mDb.execSQL(DatabaseHelper.ACCELS_STRING_CREATE);
		mDb.close();
	}
	
}
