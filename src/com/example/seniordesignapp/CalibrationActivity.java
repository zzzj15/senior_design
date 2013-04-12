package com.example.seniordesignapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

public class CalibrationActivity extends Activity implements SensorEventListener{
    private final String DEBUG_TAG = CalibrationActivity.class.getSimpleName();
    private static final int HISTORY_SIZE = 30;            // number of points to plot in history
    
    private XYPlot mXyzHistPlot = null;
    private SimpleXYSeries mXSeries;
    private SimpleXYSeries mYSeries;
    private SimpleXYSeries mZSeries;
    
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private List<SensorEvent> sensorEvents;
	
	private final int countdownPeriod = 180; //3 Minutes
	
	private TextView mCountdownTv;
	private Button mStartButton;
	private CountDownTimer mCountdown;
	
	private boolean mIsCountdown;
	private String mMode;
	private long startTime,endTime,count=0;
	private ArrayList<Long> timeStamps;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration);
		mIsCountdown=false;
		mCountdownTv = (TextView) findViewById(R.id.countdown_timer);
		mStartButton = (Button) findViewById(R.id.start_button);
		
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
		mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_NORMAL);
		mMode = getIntent().getExtras().getString("mode");
	}
	@Override
	protected void onStart(){
		super.onStart();
		mStartButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCountdown==null){//Start to countdown 3 minutes and stop the service
					mCountdown = new myCountdown(1000*countdownPeriod, 1000);
					mCountdown.start();
					mIsCountdown = true;
					timeStamps = new ArrayList<Long>();
				}
				mStartButton.setEnabled(false);//Disable the button after it's clicked
			}
		});
	}
	@Override
    protected void onStop() {
        super.onStop();
        // unregister with the orientation sensor before exiting:
        mSensorManager.unregisterListener(this);
        finish();
        mDb.beginTransaction();
		try{
			int i=0;
			for (SensorEvent e: sensorEvents){
//				mDb.execSQL("INSERT INTO "+ DatabaseHelper.ACCELS_TABLE_NAME +" VALUES ( NULL, "+ e.values[0]
//						+", "+ e.values[1] + ", " + e.values[2] + ", " + System.currentTimeMillis() + " );");	
				mDb.execSQL("INSERT INTO "+ DatabaseHelper.ACCELS_TABLE_NAME +" VALUES ( NULL, "+ e.values[0]
						+", "+ e.values[1] + ", " + e.values[2] + ", " + timeStamps.get(i)+ " );");
				i++;
			}
			mDb.setTransactionSuccessful();
			timeStamps = new ArrayList<Long>();
		}
		finally{
			mDb.endTransaction();
			mDbHelper.close();
			new FeaturesTask().execute(mMode);
		}
    }
	@Override
	public synchronized void onSensorChanged(SensorEvent sensorEvent) {        // update instantaneous data:
		
		if(mIsCountdown){
		if(count==0)
			startTime=System.currentTimeMillis();
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
        long curTime = System.currentTimeMillis();
        timeStamps.add(curTime);
        Log.d(DEBUG_TAG,"adding sensor event "+count+ " at "+curTime);
        sensorEvents.add(sensorEvent);
        
        // redraw the Plots:
        
        	mXyzHistPlot.redraw();
        	count++;
		}
	}
	@Override
	public void onAccuracyChanged(Sensor sensorEvent, int arg1) {
		// TODO Auto-generated method stub
		
	}
	private class myCountdown extends CountDownTimer{ //This counter will control the the 3-minute countdown 
		public myCountdown(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		public void onTick(long millisUntilFinished) { //OnTick, we will update the display of the digital counter
			int minutes = (int) millisUntilFinished / (60*1000);
			int seconds = (int) (millisUntilFinished - minutes*60*1000)/1000;
			if (seconds >= 10)
				mCountdownTv.setText(minutes+":" + seconds);	
			else
				mCountdownTv.setText(minutes+":0" + seconds);
	     }
	     public void onFinish() {
	    	//When the countdown is finished, we will set the transactionStatus to be true and thus data will be stored  
			mCountdownTv.setText("Done!");
			endTime=System.currentTimeMillis();
			Log.d(DEBUG_TAG,"SENSOR BEGIN TIME "+startTime);
			Log.d(DEBUG_TAG,"SENSOR FINISH TIME "+endTime+" COUNT "+count);
			mIsCountdown=false;
			mCountdown = null;
			mStartButton.setEnabled(true); 
	     }	
	}
	private class FeaturesTask extends AsyncTask<String,Void,Void>{
		@Override
		protected Void doInBackground(String... arg0) {
			try {
				new FeaturesConstructor(getApplicationContext()).constructFeatures(arg0[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}




