package com.example.seniordesignapp;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class AccelsService extends Service implements SensorEventListener{
	private final String DEBUG_TAG = AccelsService.class.getSimpleName();
	
	private boolean mTransactionStatus = false;//Indicate if the service should commit the database changes
	private List<SensorEvent> sensorEvents = new ArrayList<SensorEvent>();
    private final IBinder mBinder = new LocalBinder();// Binder given to clients
    public class LocalBinder extends Binder{
    AccelsService getService() {// Return this instance of AccelsService so clients can call public methods
            return AccelsService.this;
        }
    };
    
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	@Override
	public void onCreate(){
		super.onCreate();
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(DEBUG_TAG, "onStartCommand!" );
		return START_STICKY;
	}
	@Override
	public IBinder onBind(Intent intent) {
		startCalibration();
		return mBinder;
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
		mSensorManager.unregisterListener(AccelsService.this);
		Log.d(DEBUG_TAG, "AccelService got Destroyed! mTransactionStatus = "+mTransactionStatus );
		/* If the TransactionStatus is false, that is the app unexpectedly exists, data won't get inserted to the Database;
		 */
		mDb.beginTransaction();
		try{			
			if (mTransactionStatus){
				for (SensorEvent e: sensorEvents){
					mDb.execSQL("INSERT INTO "+ DatabaseHelper.ACCELS_TABLE_NAME +" VALUES ( NULL, "+ e.values[0]
							+", "+ e.values[1] + ", " + e.values[2] + ", " + System.currentTimeMillis() + " );");	
				}
				mDb.setTransactionSuccessful();
			}
		}
		finally{
			mDb.endTransaction();
			mDbHelper.close();
			new FeaturesTask().execute();
		}
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		Log.d(DEBUG_TAG, "onSensorChanged" );
		sensorEvents.add(event);
	}
	private class FeaturesTask extends AsyncTask<Void,Void,Void>{
		@Override
		protected Void doInBackground(Void... arg0) {
			new FeaturesConstructor(getApplicationContext()).constructFeatures();
			return null;
		}
	}
	private void startCalibration(){
		mDbHelper = new DatabaseHelper(this);
		mDb = mDbHelper.getWritableDatabase();
		
		/*Register the Sensor Listener */
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensor = (Sensor) mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_UI);
		Toast.makeText(this, "AccelService starting...", Toast.LENGTH_SHORT).show();		
	}
	
	public void setTransactionStatus(boolean isSuccessful){
		mTransactionStatus = isSuccessful;
	}
	public boolean getTransactionStatus(){
		return mTransactionStatus;
	}
}
