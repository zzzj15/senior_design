package com.example.seniordesignapp;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AccelsDbHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "tracker";
	private static final int DB_VERSION = 1;
	public static final String ACCELS_TABLE_NAME = "accelerations";
	public static final String DIETS_TABLE_NAME = "diets";
	public static final String FEATURES_TABLE_NAME = "features";
	public static final String COL_X = "xAxis";
	public static final String COL_Y = "yAxis";
	public static final String COL_Z = "zAxis";
	public static final String COL_TIMESTAMP = "timestamp"; //Time Stamp is in milliseconds
	
	public static final String ACCELS_STRING_CREATE = "CREATE TABLE " + ACCELS_TABLE_NAME +
			" (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_X + " REAL, " 
			+ COL_Y+ " REAL, " + COL_Z+" REAL, "+ COL_TIMESTAMP + " REAL  );";
	public static final String DIETS_STRING_CREATE = "CREATE TABLE " + DIETS_TABLE_NAME +
			" (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_X + " REAL, " 
			+ COL_Y+ " REAL, " + COL_Z+" REAL, "+ COL_TIMESTAMP + " REAL  );";
	public static final String FEATURES_STRING_CREATE = "CREATE TABLE " + ACCELS_TABLE_NAME +
			" (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_X + " REAL, " 
			+ COL_Y+ " REAL, " + COL_Z+" REAL, "+ COL_TIMESTAMP + " REAL  );";
	public AccelsDbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(ACCELS_STRING_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS" + ACCELS_STRING_CREATE);
		onCreate(db);
	}

}