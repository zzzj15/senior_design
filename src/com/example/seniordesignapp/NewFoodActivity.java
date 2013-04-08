package com.example.seniordesignapp;

import android.R.string;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.database.sqlite.*;

public class NewFoodActivity extends Activity implements OnClickListener {
	
	private Button Confirm;
	private Button Back;
	private EditText ManualFoodEntry;
	private EditText GIValue;
	private EditText GLValue;
	private EditText ServeSize;
	private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private static String TAG = "NewFoodActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_food);
        getActionBar().setDisplayHomeAsUpEnabled(true);
       
        Confirm= (Button) findViewById(R.id.NewEntryConfirm);
        Back= (Button) findViewById(R.id.NewEntryBack);
        GIValue=(EditText)findViewById(R.id.EditGIValue);
        ManualFoodEntry=(EditText)findViewById(R.id.EditManualFoodEntry);
        GLValue=(EditText)findViewById(R.id.EditGLValue);
        ServeSize=(EditText)findViewById(R.id.EditServeSizeValue);
        Confirm.setOnClickListener(this);
        Back.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_new_food, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.NewEntryConfirm:
			 mDbHelper = new DatabaseHelper(this);
			 mDb = mDbHelper.getWritableDatabase();
			
		
			
			//SQLiteDatabase.openDatabase("SeniorDesign.db", null,SQLiteDatabase.CREATE_IF_NECESSARY);
			String foodentry = ManualFoodEntry.getText().toString();
			//Toast t = Toast.makeText(getApplicationContext(), foodentry, 10);
			//t.show();
			String GIvalue = GIValue.getText().toString();
			String GLvalue = GLValue.getText().toString();
			String Servesize = ServeSize.getText().toString();
			
			ContentValues newValues = new ContentValues();
			newValues.put("food_name",foodentry);
			newValues.put("GI",GIvalue);
			newValues.put("Serve_Size",Servesize);
			newValues.put("GL",GLvalue);
			Log.d(TAG, "INSERTING into Food "+ foodentry+"gi"+GIvalue + "gl "+ GLvalue + "quantity " + Servesize);
			mDb.insert("food",null,newValues);
			Toast tt = Toast.makeText(getApplicationContext(), GIvalue, 10);
			tt.show();
			mDb.close();
			
		break;
		case R.id.NewEntryBack:
			Intent i = new Intent(this, MainActivity.class);
			startActivity(i);
		}
	

}
}