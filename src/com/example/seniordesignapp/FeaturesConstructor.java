package com.example.seniordesignapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author zsljulius
 * This class will handle the construction of one feature at a time and append it to 
 * the arrf file if the file already exists. but if the file doesn't exist, it will
 * generate the headers and write the file to internal storage. 
 */

public class FeaturesConstructor{
	private List<Feature> features = new ArrayList<Feature>();
	private List<Acceleration> accelerations = new ArrayList<Acceleration>();
	private SQLiteDatabase mDb;
	private Context mContext;
	private int SAMPLE_SIZE = 200;
	
	private FileOutputStream outputStream;
	private String fileName = "activity_classification.arrf";
	private StringBuffer buf=new StringBuffer();
	
	public FeaturesConstructor(Context context){
		mContext = context;
		mDb = new AccelsDbHelper(mContext).getWritableDatabase();
	}
	
	private void constructHeader(){
		char[] labels = {'x','y','z'};
		buf.append("@RELATION activities\n");
		//10 is the NUM_BIN
		for (char e: labels){
			buf.append("@ATTRIBUTE avg_"+e+" NUMERIC\n");
			buf.append("@ATTRIBUTE std_"+e+" NUMERIC\n");
			buf.append("@ATTRIBUTE avgAbsDiff_"+e+" NUMERIC\n");
			buf.append("@ATTRIBUTE avgRlstAccel_"+e+" NUMERIC\n");
			buf.append("@ATTRIBUTE timePeaks_"+e+" NUMERIC\n");
			for (int i=1;i<=10;i++){
				buf.append("@ATTRIBUTE binDist_"+e+i+" NUMERIC\n");	
			}
		}
	}
	
	public void writeToFile(){
		try{
			File file = mContext.getFileStreamPath(fileName);
			if (file.exists()){
				outputStream = mContext.openFileOutput(fileName, Context.MODE_APPEND);	
			}
			else{
				constructHeader();	
				outputStream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
			}
			for (Feature feature:features){
				double[] avg = feature.getAverage();
				double[] std = feature.getStd();
				double[] avgAbsDiff = feature.getAvgAbsDiff();
				double avgRlstAccel = feature.getAvgRlstAccel();
				double[] timePeaks = feature.getTimePeaks();
				double[] binDist = feature.getBinDist();
				for (int i=0;i<2;i++){ //There are in total x,y,z 3 datapoints
					buf.append(avg[i]+",");
					buf.append(std[i]+",");
					buf.append(avgAbsDiff[i]+",");
					buf.append(avgRlstAccel+",");
					buf.append(timePeaks[i]+",");
					for (int j=0;j<10;j++){
						buf.append(binDist[j]+",");
						if (i==1 && j==9){ //At last line, we want to remove comma and add linebreak
							buf.deleteCharAt(buf.length()-1);
							buf.append("\n");
						}
					}
					
				}	
			}
			outputStream.write(buf.toString().getBytes());
			outputStream.close();
			/* We don't need the calibration Data */
			mDb.execSQL("DROP TABLE " + AccelsDbHelper.ACCELS_TABLE_NAME);
			mDb.execSQL(AccelsDbHelper.ACCELS_STRING_CREATE);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	public void constructFeatures(){
		/* Get the most recent acceleration data and construct a feature from that*/
		Cursor mCursor = mDb.rawQuery("SELECT * FROM "+AccelsDbHelper.ACCELS_TABLE_NAME 
				+" ORDER BY timestamp DESC",null);
		mCursor.moveToFirst();
		int count = 0;
		while(!mCursor.isAfterLast()){
			accelerations.add(new Acceleration(mCursor.getFloat(1),mCursor.getFloat(2),
					mCursor.getFloat(3),mCursor.getLong(4)));
			mCursor.moveToNext();
			count++;
		}
		mCursor.close();
		
		for (int i =0;i<accelerations.size()/SAMPLE_SIZE;i++){
			List<Acceleration> samples = accelerations.subList(i*SAMPLE_SIZE, SAMPLE_SIZE*(i+1)-1);
			features.add(new Feature(samples));
		}
		writeToFile();
	}
	
}
