package com.example.seniordesignapp;

//import com.example.profilepage.R;
//import com.example.profilepage.SecondActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class ProfileActivity extends Fragment implements OnClickListener {
	private Button test;
	private Button signup;
	private Button cancel;
	private EditText username;
	private EditText password;
	private EditText retypepassword;
	private EditText height;
	private EditText weight;
	private ToggleButton toggleMaleButton;
	private ToggleButton toggleFemaleButton;
	private boolean isMale = false;
	private boolean isFemale = false;
	final Context context = getActivity(); //context
	private char prev = 'u';
	private TextView testingUsernameResults;
	public static String filename = "MySharedString";
	SharedPreferences profileData;
	private ScrollView mRelativeLayout;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }
		 mRelativeLayout = (ScrollView)inflater.inflate(R.layout.activity_profile, container, false);
		
		init();
		profileData = getActivity().getSharedPreferences(filename, 0);
		
		return mRelativeLayout;
    }
    
    private void init() {
		signup = (Button) mRelativeLayout.findViewById(R.id.Signup);
		cancel = (Button) mRelativeLayout.findViewById(R.id.Cancel);
		username = (EditText) mRelativeLayout.findViewById(R.id.Username);
		password = (EditText) mRelativeLayout.findViewById(R.id.Password);

		// test=(Button) findViewById (R.id.button1);
		// testingUsernameResults = (TextView) findViewById(R.id.textView1);
		retypepassword = (EditText) mRelativeLayout.findViewById(R.id.RetypePassword);

		height = (EditText) mRelativeLayout.findViewById(R.id.Height_ET);
		height.setInputType(InputType.TYPE_CLASS_NUMBER);
		weight = (EditText) mRelativeLayout.findViewById(R.id.Weight_ET);
		weight.setInputType(InputType.TYPE_CLASS_NUMBER);
		toggleMaleButton = (ToggleButton) mRelativeLayout.findViewById(R.id.Male);
		toggleFemaleButton = (ToggleButton) mRelativeLayout.findViewById(R.id.Female);
		toggleMaleButton.setOnClickListener(maleClicked);
		toggleFemaleButton.setOnClickListener(femaleClicked);
		signup.setOnClickListener(this);
		cancel.setOnClickListener(this);

		test = (Button) mRelativeLayout.findViewById(R.id.button1);
		test.setOnClickListener(this);
		testingUsernameResults = (TextView) mRelativeLayout.findViewById(R.id.textView1);
	}
    
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.Signup:
			if (password.getText().toString()
					.equals(retypepassword.getText().toString())) {

				String UserData = username.getText().toString();
				String PasswordData = password.getText().toString();
				String HeightData = height.getText().toString();
				String WeightData = weight.getText().toString();
				String MaleData = "Male";
				String FemaleData = "Female";

				SharedPreferences.Editor editor = profileData.edit();
				editor.putString("Username", UserData);
				editor.putString("Password", PasswordData);
				editor.putString("Height", HeightData);
				editor.putString("Weight", WeightData);

				if (toggleMaleButton.isChecked()) {
					editor.putString("Gender", MaleData);

				} else if (toggleFemaleButton.isChecked()) {
					editor.putString("Gender", FemaleData);
				}

				editor.commit();
				break;
			} else {
				Toast passwordcheck = Toast.makeText(getActivity(),
						"Password do not match. Renter Password", 50);
				passwordcheck.show();
			}

			break;
		case R.id.button1:

			Intent i = new Intent(getActivity(), ProfileInformationActivity.class);
			startActivity(i);
			profileData = getActivity().getSharedPreferences(filename, 0);
			String sharedUsernameData = profileData.getString("Username",
					" Couldn't Load Data");
			String sharedPasswordData = profileData.getString("Password",
					" Couldn't Load Data");
			String sharedHeightData = profileData.getString("Height",
					"Couldn't Load Data");
			String sharedweightData = profileData.getString("Weight",
					"Couldn't Load Data");
			String sharedtargetData = profileData.getString("Target",
					"Couldn't Load Data");
			String sharedMaleData = profileData.getString("Male",
					"Couldn't Load Data");
			String sharedFemaleData = profileData.getString("Female",
					"Couldn't Load Data");
			// testingUsernameResults.setText(sharedMaleData);
			break;

		case R.id.Cancel:
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Would you like to clear all your information?")
					.setCancelable(false)
					.setPositiveButton("No",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();

								}
							})
					.setNegativeButton("Yes",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									username.setText(null);
									password.setText(null);
									retypepassword.setText(null);
									height.setText(null);
									weight.setText(null);
									if (toggleMaleButton.isChecked()) // toggleFemaleButton.isChecked())
									{
										toggleMaleButton.toggle();
										// toggleFemaleButton.toggle();
									}

									if (toggleFemaleButton.isChecked()) {
										toggleFemaleButton.toggle();
									}

								}
							});

			AlertDialog alert = builder.create();
			alert.show();

		}

	}
	private OnClickListener maleClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (prev == 'u') {
				prev = 'm';
			} else if (prev == 'm') {
				toggleMaleButton.toggle();
				prev = 'm';
			} else if (prev == 'f') {
				toggleFemaleButton.toggle();
				prev = 'm';
			}
		}

	};

	private OnClickListener femaleClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (prev == 'u') {
				prev = 'f';
			} else if (prev == 'm') {
				toggleMaleButton.toggle();
				prev = 'f';
			} else if (prev == 'f') {
				toggleFemaleButton.toggle();
				prev = 'f';
			}
		}

	};
}
