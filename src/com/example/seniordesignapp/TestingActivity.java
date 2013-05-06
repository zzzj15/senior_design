package com.example.seniordesignapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

public class TestingActivity extends Activity implements SensorEventListener,RadioGroup.OnCheckedChangeListener,View.OnClickListener{
	private final String DEBUG_TAG = CalibrationActivity.class.getSimpleName();
    private static final int HISTORY_SIZE = 30;            // number of points to plot in history
    
    private RadioGroup mRadioStatusGroup,mRadioPositionGroup;
    
    private XYPlot mXyzHistPlot = null;
    private SimpleXYSeries mXSeries;
    private SimpleXYSeries mYSeries;
    private SimpleXYSeries mZSeries;
    
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private List<SensorEvent> sensorEvents;
	
	
	private TextView mCounter;
	private Button mStartButton;
	
	private boolean mIsCountdown;
	private String mMode;
	private long count=0;
	private ArrayList<Long> timeStamps;
	private ArrayList<Float> xSensorData,ySensorData,zSensorData;
	private ArrayList<String> mClasses;
	private ArrayList<Integer> mPositions;
	private boolean mTest,mAlgo;
	private int mCount,mPosition;
	private AsyncTask<Integer, Integer, Integer> mUpdateTimer;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_testing);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mRadioStatusGroup = (RadioGroup) findViewById(R.id.statusGroup);
		mRadioPositionGroup = (RadioGroup) findViewById(R.id.positionGroup);
		mRadioStatusGroup.setOnCheckedChangeListener(this);
		mStartButton = (Button) findViewById(R.id.start_button);
		mCounter = (TextView) findViewById(R.id.timer);
		mCounter.setText("0:00");
		
		// This list will be temporarily storing the accelerations data  
		sensorEvents = new ArrayList<SensorEvent>();
		
		//Database related operations 
		mDbHelper = new DatabaseHelper(this);
		mDb = mDbHelper.getWritableDatabase();
		
        // setup x/y/z accelerations plot:
		mXyzHistPlot = (XYPlot) findViewById(R.id.xyz_hist_plot);
		mXSeries = new SimpleXYSeries("X");
		mYSeries = new SimpleXYSeries("Y");
		mZSeries = new SimpleXYSeries("Z");
		
		mXSeries.useImplicitXVals();
		mYSeries.useImplicitXVals();
		mZSeries.useImplicitXVals();
        
        mXyzHistPlot.setRangeBoundaries(-10, 10, BoundaryMode.FIXED);
        mXyzHistPlot.setDomainBoundaries(0, 30, BoundaryMode.FIXED);
        mXyzHistPlot.addSeries(mXSeries, new LineAndPointFormatter(getApplicationContext(), R.xml.xseries));
        mXyzHistPlot.addSeries(mYSeries, new LineAndPointFormatter(getApplicationContext(), R.xml.yseries));
        mXyzHistPlot.addSeries(mZSeries, new LineAndPointFormatter(getApplicationContext(), R.xml.zseries));
        mXyzHistPlot.setDomainStepValue(5);
        mXyzHistPlot.setTicksPerRangeLabel(3);
        mXyzHistPlot.setDomainLabel("Sample Index");
        mXyzHistPlot.getDomainLabelWidget().pack();
        mXyzHistPlot.setRangeLabel("Angle (Degs)");
        mXyzHistPlot.getRangeLabelWidget().pack();
        
		/*Register the Sensor Listener */
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensor = (Sensor) mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_FASTEST);
		
		mMode = "sitting";
		mCount = 0;
		mIsCountdown = false;
		mUpdateTimer = new UpdateTimerLabel();
		
		mStartButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(mIsCountdown == false){
					timeStamps = new ArrayList<Long>();
					xSensorData = new ArrayList<Float>();
					ySensorData = new ArrayList<Float>();
					zSensorData = new ArrayList<Float>();
					mClasses = new ArrayList<String>();
					mPositions = new ArrayList<Integer>();
					
					mIsCountdown = true;
					mUpdateTimer = new UpdateTimerLabel();
					mUpdateTimer.execute();
					mStartButton.setText("Stop");
					mCount = 0;
					
				}
				else{
					finishTransaction();
					mIsCountdown = false;
					mStartButton.setText("Start");
				}
				
			}
		});
		mPosition = 0;
		mRadioPositionGroup = (RadioGroup) findViewById(R.id.positionGroup);
		mRadioPositionGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
            	if(checkedId == R.id.right_hand){
        			mPosition = 0;
//        			Log.d(DEBUG_TAG,"position "+mPosition);
                }
               if(checkedId == R.id.left_hand){
            	   mPosition = 1;
//            		Log.d(DEBUG_TAG,"position "+mPosition);
               }
               if(checkedId == R.id.pocket_face_down){
            	   mPosition = 2;
//            	   	Log.d(DEBUG_TAG,"position "+mPosition);
               }
               if(checkedId == R.id.pocket_face_up){
            	   mPosition = 3;
//            	   	Log.d(DEBUG_TAG,"position "+mPosition);
               }
			}
          });
	}
	@Override
    protected void onStop() {
        super.onStop();
        mIsCountdown = false;
        mUpdateTimer.cancel(true);
        // unregister with the orientation sensor before exiting:
        mSensorManager.unregisterListener(this);
        mDbHelper.close();

    }
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if(checkedId == R.id.Run){
			mMode = "running";
//			Log.d(DEBUG_TAG,mMode);
        }
       if(checkedId == R.id.Walk){
    	   mMode = "walking";
//    	   Log.d(DEBUG_TAG,mMode);
       }
       if(checkedId == R.id.Still){
    	   mMode = "sitting";
//    	   Log.d(DEBUG_TAG,mMode);
       }
     
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.start_button:

		default:
				
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		// TODO Auto-generated method stub
		if(mIsCountdown){
			// get rid the oldest sample in history:
	        if (mXSeries.size() > HISTORY_SIZE) {
	        	mXSeries.removeFirst();
	        	mYSeries.removeFirst();
	        	mZSeries.removeFirst();
	        }
	
	        // add the latest history sample:
	        mXSeries.addLast(null, sensorEvent.values[0]);
	        mYSeries.addLast(null, sensorEvent.values[1]);
	        mZSeries.addLast(null, sensorEvent.values[2]);
	        long curTime = System.nanoTime();
	        timeStamps.add(curTime);
	        xSensorData.add(sensorEvent.values[0]);
	        ySensorData.add(sensorEvent.values[1]);
	        zSensorData.add(sensorEvent.values[2]);
	        mClasses.add(mMode);
	        mPositions.add(mPosition);
	        
//	        Log.d(DEBUG_TAG,"adding sensor event "+count+ " at "+curTime);
	        //sensorEvents.add(sensorEvent);
	        
	        // redraw the Plots:
	        
	        	mXyzHistPlot.redraw();
	        	count++;
		}
	}
	private void updateTimerTextView() {
				mCount++;
				int div = mCount/60;
				int rem = mCount - div*60;
				if( rem < 10)
					mCounter.setText(div+":0"+rem);
				else {
					mCounter.setText(div +":"+rem);
				}
	}
	private void finishTransaction(){
			mDbHelper = new DatabaseHelper(this);
			mDb = mDbHelper.getWritableDatabase();
	        mDb.beginTransaction();
			try{
				for (int i=0;i<timeStamps.size();i++){
					mDb.execSQL("INSERT INTO "+ DatabaseHelper.ACCELS_TABLE_NAME +" VALUES ( NULL, "+ xSensorData.get(i)
							+", "+ ySensorData.get(i) + ", " + zSensorData.get(i) + ", " + timeStamps.get(i)+ ", \"" + mClasses.get(i)+"\", "+mPositions.get(i)+" );");
				}
				mDb.setTransactionSuccessful();
				timeStamps = new ArrayList<Long>();
			}
			finally{
				mDb.endTransaction();
				//mDbHelper.close();
				new FeaturesTask().execute(mMode);
				Log.d(DEBUG_TAG,mMode);
			}
	}

	private class UpdateTimerLabel extends AsyncTask<Integer,Integer,Integer>{
		@Override
		protected Integer doInBackground(Integer... arg0) {
			while (mIsCountdown) {
	            try {
	                Thread.sleep(1000);
	                publishProgress(arg0);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	            if(isCancelled())
	                  break;
	        }
	        return null;
		}
		
		protected void onProgressUpdate(Integer... progress) {
	        updateTimerTextView(); // Call to method in UI
	    }
	}
	private class FeaturesTask extends AsyncTask<String,Void,Void>{
		@Override
		protected Void doInBackground(String... arg0) {
			try {
				new FeaturesConstructor(getApplicationContext()).constructTestFeature(false); //j48-true,naivebayes-false,with class name
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
}
