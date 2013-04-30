package com.example.seniordesignapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.classifiers.bayes.NaiveBayes;
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
	private int SAMPLE_SIZE = 200; //1000 works but not many sets of data will be generated
	private final int BIN_SIZE = 10;  
	private Cursor mCursor;
	private FileOutputStream outputStream;
	private String runningFileName = "activity_classification_running.arff";
	private String walkingFileName = "activity_classification_walking.arff";
	private String sittingFileName = "activity_classification_sitting.arff";
	private String fileName = "activity_classification.arff";
	private String classfierFileName = "classifier";
	
	public FeaturesConstructor(Context context){
		mContext = context;
		mDb = new DatabaseHelper(mContext).getWritableDatabase();
	}
	
	private Instances constructInstances(List<Feature> features,String mode,boolean isCalibration){
		atts = new ArrayList<Attribute>();// set up attributes
		char[] labels = {'x','y','z'};
		for (char e: labels){	     // - numeric
//			atts.add(new Attribute("avg_"+e));
			atts.add(new Attribute("std_"+e));
//			atts.add(new Attribute("avgAbsDiff_"+e));
//			atts.add(new Attribute("avgRlstAccel_"+e));
			atts.add(new Attribute("timePeaks_"+e));
			for (int i=1;i<=BIN_SIZE;i++){
				atts.add(new Attribute("binDist_"+e+i));
			}
		}
		atts.add(new Attribute("avgTimePeaks"));
		
		// Declare the class attribute along with its values
		if(isCalibration)
			atts.add(new Attribute("class",(ArrayList<String>) null)); // - string
		data = new Instances("activity", atts, 0);
		
		for (Feature feature:features){
			vals = new double[data.numAttributes()];
//			double[] avg = feature.getAverage();
			double[] std = feature.getStd();
//			double[] avgAbsDiff = feature.getAvgAbsDiff();
//			double avgRlstAccel = feature.getAvgRlstAccel();
			double[] timePeaks = feature.getTimePeaks();
			double[] binDist = feature.getBinDist();
			for (int i=0;i<3;i++){ //There are in total x,y,z 3 datapoints
//				vals[i*15] = avg[i];
//				vals[i*15+1] = std[i];
//				vals[i*15+2] = avgAbsDiff[i];
//				vals[i*15+3] = avgRlstAccel;
//				vals[i*15+4] = timePeaks[i];
				vals[i*12] = std[i];
				vals[i*12+1] = timePeaks[i];
				for (int j=0;j<BIN_SIZE*3;j++){
					if(j>=0 && j<=9)
						vals[2+j] = binDist[j];
					else if(j>=10 && j<=19)
						vals[4+j] = binDist[j];
					else
						vals[6+j] = binDist[j];
				}
//				for (int j=0;j<BIN_SIZE*3;j++){
//					if(j>=0 && j<=9)
//						vals[5+j] = binDist[j];
//					else if(j>=10 && j<=19)
//						vals[10+j] = binDist[j];
//					else
//						vals[15+j] = binDist[j];
//				}
			}
			vals[data.numAttributes()-2]=(timePeaks[0]+timePeaks[1]+timePeaks[2])/3; //hardcoded for now
			if(isCalibration) //The last attribute is the class running/walking.
				vals[data.numAttributes()-1] = data.attribute(data.numAttributes()-1).addStringValue(mode);
			data.add(new DenseInstance(1.0, vals));
		}
		data.attribute(data.numAttributes()-2).setWeight(2.0);
//		data.attribute(4).setWeight(10); // more weight for period
//		data.attribute(19).setWeight(10);
//		data.attribute(34).setWeight(10);
	    return data;
	}
	
	private void retrieveSensorData(int mode) throws IOException{ //retrieve sensor data from database
		if(mode == 0){ //retrieve all data from database, when data is not mixed
			mCursor = mDb.rawQuery("SELECT * FROM "+DatabaseHelper.ACCELS_TABLE_NAME 
					+" ORDER BY timestamp ASC",null);
		}
		else if(mode ==1){//retrieve running data
			Log.d(DEBUG_TAG,"mode is running");
			mCursor = mDb.rawQuery("SELECT * FROM "+DatabaseHelper.ACCELS_TABLE_NAME 
					+" WHERE class like '%running%' ORDER BY timestamp ASC",null);
		}
		else if(mode ==2){//retrieve walking data
			Log.d(DEBUG_TAG,"mode is walking");
			mCursor = mDb.rawQuery("SELECT * FROM "+DatabaseHelper.ACCELS_TABLE_NAME 
					+" WHERE class like '%walking%' ORDER BY timestamp ASC",null);
		}
		else if(mode ==3){//retrieve sitting data
			Log.d(DEBUG_TAG,"mode is sitting");
			mCursor = mDb.rawQuery("SELECT * FROM "+DatabaseHelper.ACCELS_TABLE_NAME 
					+" WHERE class like '%sitting%' ORDER BY timestamp ASC",null);
		}
		Log.d(DEBUG_TAG,"number of points returned from database "+mCursor.getCount());
		mCursor.moveToFirst();
		long timeSt=0;
		long endTime = mCursor.getLong(4);
//		FileOutputStream os = mContext.openFileOutput("sensordata", Context.MODE_PRIVATE);
		while(!mCursor.isAfterLast()){
			timeSt = mCursor.getLong(4);
			accelerations.add(new Acceleration(mCursor.getFloat(1),mCursor.getFloat(2),
					mCursor.getFloat(3),timeSt));
//				String tt = ""+timeSt+" "+mCursor.getFloat(1)+" "+mCursor.getFloat(2)+" "+mCursor.getFloat(3);
//				os.write(tt.getBytes());
//				os.write("\n".getBytes());
			mCursor.moveToNext();
		}
//		os.close();
		Log.d(DEBUG_TAG,"time interval is "+(endTime-timeSt));
		mCursor.close();
	}
	public Instances constructFeature(List<Acceleration> x){//returns constructed features without class
		features.add(new Feature(x));
		Instances data = constructInstances(features,"",false); //mode is not required when class is not necessary
		return data;
	}
	private void evaluateModel(boolean algo,boolean test) throws Exception{

		File rfile = mContext.getFileStreamPath(runningFileName);
		File wfile = mContext.getFileStreamPath(walkingFileName);
		File sfile = mContext.getFileStreamPath(sittingFileName);
		if(rfile.exists()&&wfile.exists()&&sfile.exists()){ //merge two files remove extra header and generate classifier
			outputStream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
			BufferedReader br = new BufferedReader(new FileReader(rfile));
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				if(sCurrentLine.equals("@attribute class string")){
					outputStream.write("@attribute class {running,walking,sitting}".getBytes());
					outputStream.write("\n".getBytes());
				}
				else{
					outputStream.write(sCurrentLine.getBytes());
					outputStream.write("\n".getBytes());
				}
			}
			br.close();
			//continue reading wfile
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
			//continue reading sfile
			br = new BufferedReader(new FileReader(sfile));
			toWrite = false;
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
			/* generate classifier*/
			if(test==false){
				Log.d(DEBUG_TAG,"Training!");
				String[] options = new String[4];
				options[0] = "-t";
				options[1] = mContext.getFileStreamPath(fileName).getAbsolutePath();
				options[2] = "-d";
				options[3] = mContext.getFileStreamPath(classfierFileName).getAbsolutePath();
				Log.d(DEBUG_TAG,options[1]);
				if(algo==true)
					Log.d(DEBUG_TAG,Evaluation.evaluateModel(new J48(), options));
				else
					Log.d(DEBUG_TAG,Evaluation.evaluateModel(new NaiveBayes(), options));
			}
			else{
				Log.d(DEBUG_TAG,"Testing!");
				
				if(algo==true){
					String[] options = new String[4];
					options[0] = "-l";
					options[1] = mContext.getFileStreamPath(classfierFileName).getAbsolutePath();
					options[2] = "-T";
					options[3] = mContext.getFileStreamPath(fileName).getAbsolutePath();
					Log.d(DEBUG_TAG,Evaluation.evaluateModel(new J48(), options));
				}
				else{
					String[] options = new String[5];
					options[0] = "-l";
					options[1] = mContext.getFileStreamPath(classfierFileName).getAbsolutePath();
					options[2] = "-T";
					options[3] = mContext.getFileStreamPath(fileName).getAbsolutePath();
					options[4] = "-o";
					Log.d(DEBUG_TAG,Evaluation.evaluateModel(new NaiveBayes(), options));
				}
			}
		}
	}
	public void constructFeatures(String mode,boolean test,boolean algo,boolean isCalibration) throws Exception{
		if(mode.equals("running"))
			outputStream = mContext.openFileOutput(runningFileName, Context.MODE_PRIVATE);
		if(mode.equals("walking"))
			outputStream = mContext.openFileOutput(walkingFileName, Context.MODE_PRIVATE);
		if(mode.equals("sitting"))
			outputStream = mContext.openFileOutput(sittingFileName, Context.MODE_PRIVATE);
		
		/* Get the just-recorded accelerations from db and construct a feature from every 200 datapoints*/
		retrieveSensorData(0); //retrieve all data from database
		for (int i =0;i<accelerations.size()/SAMPLE_SIZE;i++){
			List<Acceleration> samples = accelerations.subList(i*SAMPLE_SIZE, SAMPLE_SIZE*(i+1)-1);
			features.add(new Feature(samples));
		}
		Instances data = constructInstances(features,mode,isCalibration);
		outputStream.write(data.toString().getBytes());
		outputStream.close();
		
		evaluateModel( algo, test);
		/* We don't need the data for calibration*/
		mDb.execSQL("DROP TABLE " + DatabaseHelper.ACCELS_TABLE_NAME);
		mDb.execSQL(DatabaseHelper.ACCELS_STRING_CREATE);
		mDb.close();
	}
	public void constructTestFeature(boolean algo) throws Exception{
		retrieveSensorData(1); //retrieve running data
		outputStream = mContext.openFileOutput(runningFileName, Context.MODE_PRIVATE);
		for (int i =0;i<accelerations.size()/SAMPLE_SIZE;i++){
			List<Acceleration> samples = accelerations.subList(i*SAMPLE_SIZE, SAMPLE_SIZE*(i+1)-1);
			features.add(new Feature(samples));
		}
		Instances data = constructInstances(features,"running",true);
		outputStream.write(data.toString().getBytes());
		outputStream.close();
		
		retrieveSensorData(2); //retrieve walking data
		outputStream = mContext.openFileOutput(walkingFileName, Context.MODE_PRIVATE);
		for (int i =0;i<accelerations.size()/SAMPLE_SIZE;i++){
			List<Acceleration> samples = accelerations.subList(i*SAMPLE_SIZE, SAMPLE_SIZE*(i+1)-1);
			features.add(new Feature(samples));
		}
		data = constructInstances(features,"walking",true);
		outputStream.write(data.toString().getBytes());
		outputStream.close();
		
		retrieveSensorData(3); //retrieve sitting data
		outputStream = mContext.openFileOutput(sittingFileName, Context.MODE_PRIVATE);
		for (int i =0;i<accelerations.size()/SAMPLE_SIZE;i++){
			List<Acceleration> samples = accelerations.subList(i*SAMPLE_SIZE, SAMPLE_SIZE*(i+1)-1);
			features.add(new Feature(samples));
		}
		data = constructInstances(features,"sitting",true);
		outputStream.write(data.toString().getBytes());
		outputStream.close();
		
		evaluateModel(algo, true); //true - J48
		/* We don't need the data for calibration*/
		mDb.execSQL("DROP TABLE " + DatabaseHelper.ACCELS_TABLE_NAME);
		mDb.execSQL(DatabaseHelper.ACCELS_STRING_CREATE);
		mDb.close();
	}
	
}
