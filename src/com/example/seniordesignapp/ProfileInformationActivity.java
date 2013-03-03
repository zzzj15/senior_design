package com.example.seniordesignapp;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class ProfileInformationActivity extends Activity implements OnClickListener{
	SharedPreferences UserData;
	TextView Username;
	TextView Password;
	TextView Height;
	TextView Weight;
	TextView Gender;
	Button UserInfo;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_information);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        UserInfo = (Button) findViewById(R.id.Load);
        Username = (TextView) findViewById(R.id.textView1);
        Password = (TextView) findViewById(R.id.textView2);
        Gender = (TextView) findViewById(R.id.textView3);
        Height =  (TextView) findViewById(R.id.textView4);
        Weight =  (TextView) findViewById(R.id.textView5);
        UserInfo.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_profile_information, menu);
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
		
		UserData = getSharedPreferences("MySharedString", MODE_PRIVATE );
		
		String LoadUsername = UserData.getString("Username", "Couldn't Load Data");
		Username.setText(LoadUsername);
		String LoadPassword = UserData.getString("Password", "Couldn't Load Data");
		Password.setText(LoadPassword);
		String LoadGender = UserData.getString("Gender", " Coudn't Load Data");
		Gender.setText(LoadGender);
		String LoadHeight = UserData.getString("Height", " Couldn't Load Data");
		Height.setText(LoadHeight);
		String LoadWeight = UserData.getString("Weight", " Couldn't Load Data");
		Weight	.setText(LoadWeight);
		
		
	}
}
