package com.example.seniordesignapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper{
	private static final String DEBUG_TAG = DatabaseHelper.class.getSimpleName();
	
	private Context mContext;
	/* General DB Information */
	private static final String DB_NAME = "SeniorDesign";
	private static final int DB_VERSION = 1;
	
	/* Food GPS Parameters */
	public final String COL_LATITUDE="latitude";
    public final String COL_LONGITUDE="longitude";
    public final String COL_FOODITEM="food_name";
    public final String COL_FOODCATEGORY="food_category";
    public final String COL_CALORIES="calories";
    public final String COL_GPSTIME="GPS_time";
    public final String FOODGSP_TABLE_NAME="FoodGPS";
    public final String FOODGPS_STRING_CREATE="create table "+ FOODGSP_TABLE_NAME
    								+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " 
    								+ COL_GPSTIME +  " text,"
    								+ COL_LATITUDE + " text,"
    								+ COL_LONGITUDE +" text,"
    								+ COL_FOODITEM + " text,"
    								+ COL_FOODCATEGORY + " text,"
    								+ COL_CALORIES + " integer"
    								+");";
    /* Food Parameters */
    public final String COL_GL="GL";
    public final String COL_GI="GI";
    public final String COL_SERVING_SIZE = "Serve_Size";
    public final String FOOD_TABLE_NAME="food";
    public final String FOOD_STRING_CREATE = "create table " + FOOD_TABLE_NAME 
    								+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " 
    								+ COL_FOODITEM +  " text,"
    								+ COL_GI + " integer not null,"
    								+ COL_SERVING_SIZE + " text,"
    								+ COL_GL +" REAL"
    								+");";
    /* Activity Parameters */
	public static final String ACCELS_TABLE_NAME = "accelerations";
	public static final String COL_X = "xAxis";
	public static final String COL_Y = "yAxis";
	public static final String COL_Z = "zAxis";
	public static final String COL_TIMESTAMP = "timestamp"; //Time Stamp is in milliseconds
	
	public static final String ACCELS_STRING_CREATE = "CREATE TABLE " + ACCELS_TABLE_NAME 
								+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " 
								+ COL_X + " REAL, " 
								+ COL_Y + " REAL, " 
								+ COL_Z +" REAL, "
								+ COL_TIMESTAMP + " REAL  );";
	
	public DatabaseHelper(Context context) {
		super(context, DB_NAME,null, 1);
		mContext=context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(ACCELS_STRING_CREATE);
		db.execSQL(FOODGPS_STRING_CREATE);
		db.execSQL(FOOD_STRING_CREATE);

		try {
			seedDatabase(db);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + ACCELS_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + FOODGSP_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + FOOD_TABLE_NAME);
		onCreate(db);
		
	}
	private void seedDatabase(SQLiteDatabase mDb) throws IOException{
    	//Open your local db as the input stream
    	//InputStream myInput =mContext.getAssets().open("foodTable.txt");
		InputStream myInput =mContext.getAssets().open("2008.txt");
    	BufferedReader reader = new BufferedReader(new InputStreamReader(myInput));
        String line = reader.readLine();
    	try{
    		mDb.beginTransaction();
    		while (line!=null){
        		mDb.execSQL(line);
        		line=reader.readLine();
        	}
    		mDb.setTransactionSuccessful();
    	}
    	finally{
            mDb.endTransaction();	
    	}
	}
	public Cursor fetchAllFood(SQLiteDatabase mDb) {
		  Cursor mCursor = mDb.rawQuery("SELECT food_name FROM food;",null);
		 
		  if (mCursor != null) {
		   mCursor.moveToFirst();
		  }
		  return mCursor;
	}
	public Cursor fetchFoodByName(String input, SQLiteDatabase mDb) {
		 
		  Cursor mCursor = mDb.rawQuery("SELECT food_name FROM food WHERE lower(food_name) LIKE lower('%"+input+"%');",null);
		 
		  if (mCursor != null) {
		   mCursor.moveToFirst();
		  }
		  return mCursor;
	}
	

}
