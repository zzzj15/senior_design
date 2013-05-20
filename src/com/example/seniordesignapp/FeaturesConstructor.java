package com.example.seniordesignapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import android.os.Environment;
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
	private int SAMPLE_SIZE = 100; //1000 works but not many sets of data will be generated
	private final int BIN_SIZE = 10;  
	private Cursor mCursor;
	private FileOutputStream outputStream;
	private String fileName = "activity_classification.arff";
	private String classifierFileName = "classifier";
	
	public FeaturesConstructor(Context context){
		mContext = context;
		mDb = new DatabaseHelper(mContext).getWritableDatabase();
	}
	
	private Instances constructInstances(List<Feature> features,String mode,boolean isCalibration,int Position){
		atts = new ArrayList<Attribute>();// set up attributes
		char[] labels = {'v','h'};
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
//		atts.add(new Attribute("avgTimePeaks"));
		
		// Declare the class attribute along with its values
		if(isCalibration)
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
			for (int i=0;i<2;i++){ //There are in total x,y,z 3 datapoints
				vals[i*15] = avg[i];
				vals[i*15+1] = std[i];
				vals[i*15+2] = avgAbsDiff[i];
				vals[i*15+3] = avgRlstAccel;
				vals[i*15+4] = timePeaks[i];
				
//				vals[i*2] = std[i];
//				vals[i*2+1] = timePeaks[i];
//				for (int j=0;j<BIN_SIZE*3;j++){
//					if(j>=0 && j<=9)
//						vals[2+j] = binDist[j];
//					else if(j>=10 && j<=19)
//						vals[4+j] = binDist[j];
//					else
//						vals[6+j] = binDist[j];
//				}
				
				for (int j=0;j<BIN_SIZE*2;j++){
					if(j>=0 && j<=9)
						vals[5+j] = binDist[j];
					else if(j>=10 && j<=19)
						vals[10+j] = binDist[j];
				}
			}
//			vals[data.numAttributes()-2]=(timePeaks[0]+timePeaks[1])/2; //hardcoded for now
			if(isCalibration){ //The last attribute is the class running/walking.
				String m = getName(Position,mode);
				vals[data.numAttributes()-1] = data.attribute(data.numAttributes()-1).addStringValue(m);
			}
			data.add(new DenseInstance(1.0, vals));
		}
//		data.attribute(data.numAttributes()-2).setWeight(2.0);
//		data.attribute(4).setWeight(10); // more weight for period
//		data.attribute(19).setWeight(10);
//		data.attribute(34).setWeight(10);
	    return data;
	}
	private void createFolderonSD(String x){
		File folder = new File(Environment.getExternalStorageDirectory() + "/"+x );//"/seniordesigndata"
		boolean success = true;
		if (!folder.exists()) {
		    success = folder.mkdir();
		}
		if (success) {
		    Log.d(DEBUG_TAG,"folder "+x +"created");
		}
	}
	public void copy(File src, File dst) throws IOException {
	    try {
	        InputStream in = new FileInputStream(src);
	        OutputStream out = new FileOutputStream(dst);

	        // Transfer bytes from in to out
	        byte[] buf = new byte[1024];
	        int len;
	        while ((len = in.read(buf)) > 0) {
	            out.write(buf, 0, len);
	        }
	        in.close();
	        out.close();
	    } catch (IOException io) {
	        Log.d(DEBUG_TAG,"error copying file!"+io);
	    }
	}
	private void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
	private String getName(int position,String mode){
		//String temp = mode.substring(0, 1)+position;
		if(mode.contains("sitting"))
			return mode;
		else{
			if(position==0)
				return mode+"_hand";
			else if(position==1)
				return mode+"_pocket";
			else if(position==2)
				return mode+"_hand_text";
			else
				return mode;
		}
	
	}
	private int retrieveSensorData(String mode,int Position) throws IOException{ //retrieve sensor data from database
		int ret=0;																	//returns 1 if query result is nonempty
		if(mode == ""){ //retrieve all data from database, when data is not mixed
			mCursor = mDb.rawQuery("SELECT * FROM "+DatabaseHelper.ACCELS_TABLE_NAME 
					+" ORDER BY timestamp ASC",null);
		}
		else{
			mCursor = mDb.rawQuery("SELECT * FROM "+DatabaseHelper.ACCELS_TABLE_NAME 
					+" WHERE class like '%"+mode+"%' AND position = "+Position+ " ORDER BY timestamp ASC",null);
		}

		if(mCursor.getCount()>0){
			Log.d(DEBUG_TAG,"number of points returned from database "+mCursor.getCount());
			mCursor.moveToFirst();
			long timeSt=0;
			long endTime = mCursor.getLong(4);
			while(!mCursor.isAfterLast()){
				timeSt = mCursor.getLong(4);
				accelerations.add(new Acceleration(mCursor.getFloat(1),mCursor.getFloat(2),
						mCursor.getFloat(3),timeSt));
				mCursor.moveToNext();
			}
			Log.d(DEBUG_TAG,"time interval is "+(endTime-timeSt));
			ret = 1;
		}
		mCursor.close();
		return ret;
	}
	public Instances constructFeature(List<Acceleration> x){//returns constructed features without class
		features.add(new Feature(x));
		Instances data = constructInstances(features,"",false,0); //mode is not required when class is not necessary
		return data;
	}
	private String getPre(boolean test){
		String pre; //decide the prefix for the output file
		if(test)
			pre = "test_";
		else
			pre = "activity_classification_";
		return pre;
	}
	private String getCurrentTime(){
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMdd_HHmmss");
		String currentDateandTime = dateFormat.format(new Date());
		return currentDateandTime;
	}
	private void evaluateResult(boolean algo,boolean test,String fileName,String classifierFileName) throws Exception{
		/* generate classifier*/
		String currentDateandTime = getCurrentTime();
		
		if(test==false){
			FileOutputStream oStream = mContext.openFileOutput("training_log_"+currentDateandTime, Context.MODE_PRIVATE);
			Log.d(DEBUG_TAG,"Training!");
//			String[] options = new String[4];
//			options[0] = "-t";
//			options[1] = mContext.getFileStreamPath(fileName).getAbsolutePath();
//			options[2] = "-d";
//			options[3] = mContext.getFileStreamPath(classifierFileName).getAbsolutePath();
			if(algo==true){//j48
				String[] options = new String[5];
				options[0] = "-t";
				options[1] = mContext.getFileStreamPath(fileName).getAbsolutePath();
				options[2] = "-d";
				options[3] = mContext.getFileStreamPath(classifierFileName).getAbsolutePath();
				options[4] = "-U";
				String result = Evaluation.evaluateModel(new J48(), options);
				Log.d(DEBUG_TAG,result);
				oStream.write(result.getBytes());
			}
			else{
				String[] options = new String[4];
				options[0] = "-t";
				options[1] = mContext.getFileStreamPath(fileName).getAbsolutePath();
				options[2] = "-d";
				options[3] = mContext.getFileStreamPath(classifierFileName).getAbsolutePath();
				String result = Evaluation.evaluateModel(new NaiveBayes(), options);
				Log.d(DEBUG_TAG,result);
				oStream.write(result.getBytes());
			}
			oStream.close();
		}
		else{
			FileOutputStream oStream = mContext.openFileOutput("testing_log_"+currentDateandTime, Context.MODE_PRIVATE);
			Log.d(DEBUG_TAG,"Testing!");
			if(algo==true){//J48
				String[] options = new String[4];
				options[0] = "-l";
				options[1] = mContext.getFileStreamPath(classifierFileName).getAbsolutePath();
				options[2] = "-T";
				options[3] = mContext.getFileStreamPath(fileName).getAbsolutePath();
				String result = Evaluation.evaluateModel(new J48(), options);
				Log.d(DEBUG_TAG,result);
				oStream.write(result.getBytes());
			}
			else{
				String[] options = new String[5];
				options[0] = "-l";
				options[1] = mContext.getFileStreamPath(classifierFileName).getAbsolutePath();
				options[2] = "-T";
				options[3] = mContext.getFileStreamPath(fileName).getAbsolutePath();
				options[4] = "-o";
				String result = Evaluation.evaluateModel(new NaiveBayes(), options);
				Log.d(DEBUG_TAG,result);
				oStream.write(result.getBytes());
			}
			oStream.close();
			
		}
		//copy test result to SD card
		File sdcard = Environment.getExternalStorageDirectory();
		File src,dst;
		if(test){
			 src = new File(mContext.getFileStreamPath("testing_log_"+currentDateandTime).getAbsolutePath());
			 dst = new File(sdcard.getAbsolutePath()+"/seniordesigndata/Logs/test_log_"+currentDateandTime+".txt"); 
		}
		else{
			 src = new File(mContext.getFileStreamPath("training_log_"+currentDateandTime).getAbsolutePath());
			 dst = new File(sdcard.getAbsolutePath()+"/seniordesigndata/Logs/training_log_"+currentDateandTime+".txt");
		}
			
		copy(src,dst);
	}
	private void constructFinalFile(boolean algo,boolean test ) throws Exception{
		
		String pre = getPre(test); //decide the prefix for the output file
		
		//File[] fileArr = new File[12];
		String[] filePre = {"r","w","s"};
		ArrayList<File> fileArr = new ArrayList<File>();
		ArrayList<String> fName = new ArrayList<String>();
		//String[] fName = new String[12];
		int[] posNum = {2,3,1}; //2 positions for running, 3 positions for walking, 1 position for still
		for(int i=0;i<3;++i){
			for(int j=0;j<posNum[i];++j){
				String t = pre+filePre[i]+j+".arff";
				fName.add(t);
				fileArr.add(mContext.getFileStreamPath(t));
//				fName[4*i+j] = pre+filePre[i]+j+".arff";
//				fileArr[4*i+j] = mContext.getFileStreamPath(fName[4*i+j]); //"activity_classification_r1.arff"
			}
		}
		//check if all files exist
		boolean allExist = true;
		int numFiles = fileArr.size();
		boolean[] fileExistArr = new boolean[numFiles];
		for(int i=0;i<numFiles;i++){
//			allExist = allExist && fileArr[i].exists();
			allExist = allExist && fileArr.get(i).exists();
			if(fileArr.get(i).exists()==false){
				Log.d(DEBUG_TAG,fileArr.get(i).getName()+"DOES NOT EXIST");
				fileExistArr[i]=false;
			}
			else
				fileExistArr[i]=true;
		}
		if(allExist||test){	//construct final file if all files exist for calibration or in test mode
			outputStream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
			BufferedReader br;
			int startFileNum = 0;
			if(!allExist){
				 for(int i=0;i<numFiles;i++){
					 if(fileExistArr[i]==true){
						startFileNum = i;
						break;
					 }
				 }
			}
//			br = new BufferedReader(new FileReader(fileArr[startFileNum]));
			br = new BufferedReader(new FileReader(fileArr.get(startFileNum)));
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				if(sCurrentLine.equals("@attribute class string")){
					//outputStream.write("@attribute class {running,walking,sitting}".getBytes());
					outputStream.write("@attribute class {running_hand,running_pocket,walking_hand,walking_pocket,walking_hand_text,sitting}".getBytes());
//					outputStream.write("@attribute class {r0,r1,r2,r3,w0,w1,w2,w3,s0,s1,s2,s3}".getBytes());
					outputStream.write("\n".getBytes());
				}
				else{
					outputStream.write(sCurrentLine.getBytes());
					outputStream.write("\n".getBytes());
				}
			}
			br.close();
			//continue reading the rest files
			for(int i=startFileNum;i<numFiles;i++){
				if(fileExistArr[i]==true){
					br = new BufferedReader(new FileReader(fileArr.get(i)));
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
				}
			}
			outputStream.close();
			//copy test data to SDcard
			File sdcard = Environment.getExternalStorageDirectory();
			
			File src= new File(mContext.getFileStreamPath(fileName).getAbsolutePath());
			String currentDateandTime = getCurrentTime();
			File dst;
			if(test){
				 dst= new File(sdcard.getAbsolutePath()+"/seniordesigndata/Arff files/test_"+currentDateandTime+".arff"); 
			}
			else
				 dst= new File(sdcard.getAbsolutePath()+"/seniordesigndata/Arff files/train_"+currentDateandTime+".arff");  
			copy(src,dst);
			File classifier =  mContext.getFileStreamPath(classifierFileName); 
			if(!classifier.exists()){//if classifier does not exist copy from asset folder
				FileOutputStream oStream = mContext.openFileOutput(classifierFileName, Context.MODE_PRIVATE);
				InputStream myInput =mContext.getAssets().open(classifierFileName);
				copyFile(myInput,oStream);
				myInput.close();
		        oStream.close();
			}
			evaluateResult(algo,test,fileName,classifierFileName);
		}
		
	}
	private void createFoldersonSD(){
		createFolderonSD("seniordesigndata");
		createFolderonSD("seniordesigndata/Logs");
		createFolderonSD("seniordesigndata/Arff files");
	}
	public void constructFeatures(String mode,boolean test,boolean algo,boolean isCalibration,int position) throws Exception{
		createFoldersonSD();
		String pre = getPre(test); //decide the prefix for the output file
		outputStream = mContext.openFileOutput(pre+mode.substring(0,1)+position+".arff", Context.MODE_PRIVATE);
		
		/* Get the just-recorded accelerations from db and construct a feature from every 200 datapoints*/
		retrieveSensorData("",0); //retrieve all data from database
		for (int i =0;i<accelerations.size()/SAMPLE_SIZE;i++){
			List<Acceleration> samples = accelerations.subList(i*SAMPLE_SIZE, SAMPLE_SIZE*(i+1)-1);
			features.add(new Feature(samples));
		}
		Instances data = constructInstances(features,mode,isCalibration,position);
		outputStream.write(data.toString().getBytes());
		outputStream.close();
		
		constructFinalFile( algo, test);
		/* We don't need the data for calibration*/
		mDb.execSQL("DROP TABLE " + DatabaseHelper.ACCELS_TABLE_NAME);
		mDb.execSQL(DatabaseHelper.ACCELS_STRING_CREATE);
		mDb.close();
	}
	public void constructTestFeature(boolean algo) throws Exception{
		createFoldersonSD();
		String[] modes = {"running","walking","sitting"};
		File dir = mContext.getFilesDir();
		
		
		int[] posNum = {2,3,1}; //2 positions for running, 3 positions for walking, 1 position for still
		
		for(int i=0;i<3;++i){
			for(int j=0;j<posNum[i];++j){ //j is position
				int isNotEmpty = retrieveSensorData(modes[i],j);//look for all activities in all positions if any data was saved from the database
				if(isNotEmpty==1){
					outputStream = mContext.openFileOutput("test_"+modes[i].substring(0,1)+j+".arff", Context.MODE_PRIVATE);
					for (int k =0;k<accelerations.size()/SAMPLE_SIZE;k++){
						List<Acceleration> samples = accelerations.subList(k*SAMPLE_SIZE, SAMPLE_SIZE*(k+1)-1);
						features.add(new Feature(samples));
					}
					Instances data = constructInstances(features,modes[i],true,j);
					outputStream.write(data.toString().getBytes());
					outputStream.close();
				}
				else{ // delete files not used in this calibration
					File file = new File(dir, "test_"+modes[i].substring(0,1)+j+".arff");
					boolean deleted = file.delete();
					if(deleted)
						Log.d(DEBUG_TAG,"deleted"+file.getAbsolutePath().toString());
				}
			}
		}
		constructFinalFile(algo, true); //true - J48, test is true
		/* We don't need the data for calibration*/
		mDb.execSQL("DROP TABLE " + DatabaseHelper.ACCELS_TABLE_NAME);
		mDb.execSQL(DatabaseHelper.ACCELS_STRING_CREATE);
		mDb.close();
	}
}
