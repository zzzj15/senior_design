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
	/* Food GPS Parameters */
	private Context mContext;
	private static String TAG = "DatabaseHelper";
	private static String DB_NAME = "SeniorDesign";
	public final String latitude="latitude";
    public final String longitude="longitude";
    public final String COLUMN1="ID_foodGPS";
    public final String foodItem="food_name";
    public final String foodCategory="food_category";
    public final String calories="calories";
    public final String gpsTime="GPS_time";
    public final String FOODGPSTABLE="FoodGPS";
    public final String CREATETB1="create table "+ FOODGPSTABLE+ "(" 
    								+COLUMN1+ " integer primary key autoincrement,"
    								+gpsTime+  " text,"
    								+latitude+ " text,"
    								+longitude +" text,"
    								+foodItem + " text,"
    								+foodCategory+" text,"
    								+calories + " integer"
    								+");";
    /* Food Parameters */
	public final String table2_ID="ID_FOOD";
    public final String GL="GL";
    public final String GI="GI";
    public final String serve_size = "Serve_Size";
    public final String FOODTABLE="food";
    public final String CREATETB2="create table "+ FOODTABLE+ "(" 
    								+table2_ID+ " integer primary key autoincrement,"
    								+foodItem+  " text,"
    								+GI+ " integer not null,"
    								+serve_size+ " text,"
    								+GL +" integer"
    								+");";
	public DatabaseHelper(Context context) {
		super(context, DB_NAME,null, 1);
		mContext=context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		db.execSQL(CREATETB1);
		db.execSQL(CREATETB2);
		try {
			seedDatabase(db);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
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
