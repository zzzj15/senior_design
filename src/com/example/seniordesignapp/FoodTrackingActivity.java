package com.example.seniordesignapp;

import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class FoodTrackingActivity extends Fragment implements AdapterView.OnItemSelectedListener,TextWatcher{
	private static String TAG = "FoodTrackingActivity";
	private Spinner spin_amount,freq_choice;
	//private AutoCompleteTextView itemAutoComplete_2,itemAutoComplete_3;
	private String[] foodItem;
	private ImageButton mbtSpeak,mbtSearch;
	private Button mbtConfirm;
	private EditText mEdtText;
	private ListView mlv;
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;

	//GPS parameters
	
	private LocationManager locationManager;
	private LocationListener locationListener;
    private Double lon,lat;
    private Long gpstime;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private Cursor mCursor;
    private SimpleCursorAdapter dataAdapter;
    private String[] columns;
    private int[] to;
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
//		switch(arg0.getId()){
//		case R.id.spinner1:
//		default:
//		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
	private void generateData(){
		/*Initialize food database*/
    	mDbHelper = new DatabaseHelper(getActivity());
    	mDb = mDbHelper.getWritableDatabase();
		foodItem=getFoodItemList();
		columns = new String[]{
				mDbHelper.foodItem
		};
		to = new int[]{
				R.id.foodlist
		};
		
	}
	String[] getFoodItemList(){
		Cursor crs = mDb.rawQuery("SELECT food_name FROM food", null);
		String[] array = new String[crs.getCount()];
		int i = 0;
		crs.moveToFirst();
		while(crs.moveToNext()){
		    String uname = crs.getString(crs.getColumnIndex("food_name"));
		    array[i] = uname;
		    i++;
		}
		return array;
	}
	private void initializeAmountSpinners(){
		ArrayAdapter<String> aa = null;
		aa = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_spinner_item, new String[] {" ","1","2","3","4","5","6","7","8","9","10"});
		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin_amount.setAdapter(aa);
	}
	private void initializeFrequentSpinner(){
		ArrayList<String> sData = new ArrayList<String>();
		sData.add("Or Select from Frequent Choices Below");
		//String sql = "select food_name from FoodGPS where food_name NOT NULL";
		String sql = "SELECT food_name, COUNT(*)"
					+ " FROM FoodGPS WHERE food_name NOT NULL"
					+" GROUP BY food_name"
					+" ORDER BY COUNT(*) DESC"
					+" LIMIT 5";
		Cursor crs = mDb.rawQuery(sql, null);
		
		if(crs.getCount()>0){ //now it is taking the first match WIP fix later
			crs.moveToFirst();
			sData.add(crs.getString(crs.getColumnIndex("food_name")));
				while (!crs.isLast()) {
					crs.moveToNext();
				    String Name = crs.getString(crs.getColumnIndex("food_name"));
				    sData.add(Name);
				    Log.d(TAG,Name);
				}
		}
		freq_choice.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, sData));
		
	}

	public boolean checkVoiceRecognition() {
		  // Check if voice recognition is present
		  PackageManager pm = getActivity().getPackageManager();
		  List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
		    RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		  if (activities.size() == 0) {
		   mbtSpeak.setEnabled(false);
		   //mbtSpeak.setText("Voice recognizer not present");
		   Toast.makeText(this.getActivity(), "Voice recognizer not present",Toast.LENGTH_SHORT).show();
		   return false;
		  }
	  return true;
	}
	
	public void speak(View view) {
		  Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		  // Specify the calling package to identify your application
		  intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
		    .getPackage().getName());

		  // Display an hint to the user about what he should say.
		  intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Now, Example : 1 Hamburger");

		  // Given an hint to the recognizer about what the user is going to say
		  //There are two form of language model available
		  //1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
		  //2.LANGUAGE_MODEL_FREE_FORM  : If not sure about the words or phrases and its domain.
		  intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

		  // Specify how many results you want to receive. The results will be
		  // sorted where the first result is the one with higher confidence.
		  intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
		  //Start the Voice recognizer activity for the result.
		  startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
		 }
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	   //If Voice recognition is successful then it returns RESULT_OK
	   if(requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
		   
		 //  showToastMessage("OK");   
	    ArrayList<String> textMatchList = data
	    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

	    if (!textMatchList.isEmpty()) {
	     // If first Match contains the 'search' word
	     // Then start web search.
	     if (textMatchList.get(0).contains("search")) {

	        String searchQuery = textMatchList.get(0);
	                                           searchQuery = searchQuery.replace("search","");
	        Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
	        search.putExtra(SearchManager.QUERY, searchQuery);
	        startActivity(search);
	     }
	     else {
	         // populate the Matches
	    	 showToastMessage("Updated Text: "+ textMatchList.get(0));
	    	 mEdtText.setText(textMatchList.get(0));
	    	 checkData(textMatchList.get(0));
	     }

	    }
	   //Result code for various error.
	   }
	   else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
	    showToastMessage("Audio Error");
	   }
	   else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
	    showToastMessage("Client Error");
	   }
	   else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
	    showToastMessage("Network Error");
	   }
	   else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
	    showToastMessage("No Match");
	   }
	   else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
	    showToastMessage("Server Error");
	   }
	  super.onActivityResult(requestCode, resultCode, data);
	 }
	void showToastMessage(String message){
		  Toast.makeText(this.getActivity(), message, Toast.LENGTH_SHORT).show();
	}
	//helper routine to decide if a string is numeric
	public boolean isInteger( String input){
	   try{
	      Integer.parseInt( input );
	      return true;
	   }
	   catch( Exception e){
	      return false;
	   }
	}
	private void checkData(String in){
		spin_amount.setSelection(0);
		int setCount = 0;
		String delims = "[ ]+";
		String[] tokens = in.split(delims);
		String temp = "";
		for (int i = 0; i < tokens.length; i++){
			if(isInteger(tokens[i])){
				if(temp.isEmpty())
					setQuantity(Integer.parseInt(tokens[i]));
				else{
					setQuantity(Integer.parseInt(tokens[i]));
					checkFoodDatabase(temp);
					setCount++;
					temp="";
				}
			}
			else{
				if(temp.isEmpty())
					temp=temp+tokens[i];
				else
					temp=temp+" "+tokens[i];
//				checkFoodDatabase(tokens[i]);
			}
			if((i==tokens.length-1) &&!(temp.isEmpty()))
				checkFoodDatabase(temp);
		}
		    
	}
	private void setQuantity(int in){
		if(in<=10)
			spin_amount.setSelection(in);
		else
			showToastMessage("Maximum is 10.");
	}
	private void checkFoodDatabase(String out){
		//trim the "s" if there are any
		//this will not be a problem for words end with 's' 
		//because we are using % in the query
		Log.d(TAG,"checking database"+out);
		ArrayList<String> sData = new ArrayList<String>();
		if(out.substring(out.length() - 1).equals("s"))
			out=out.substring(0, out.length()-1);
		String sql = "SELECT food_name FROM food WHERE lower(food_name) LIKE lower('%"+out+"%');";
		Cursor crs = mDb.rawQuery(sql, null);
		//if there is a match
		if(crs.getCount()>0){ //now it is taking the first match WIP fix later
			crs.moveToFirst();
			sData.add(crs.getString(crs.getColumnIndex("food_name")));
				while (!crs.isLast()) {
					crs.moveToNext();
				    String Name = crs.getString(crs.getColumnIndex("food_name"));
				    sData.add(Name);
				    Log.d(TAG,Name);
				}
		}
		else{
			sData.add("No matches");
			showToastMessage(out+" does not exist in food database! Add it");
		}
		mlv.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_single_choice, sData));
	}
	
	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated
		}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
	void disableKeyboard(){
		//Hide Virtual Keyboard after checking data
    	InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 

    	inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                   InputMethodManager.HIDE_NOT_ALWAYS);
	}
	void searchAll(){
		//Hide Virtual Keyboard after checking data
    	disableKeyboard();
    	String x = (String) mEdtText.getText().toString();
    	checkData(x);
	}

	void requestGPSupdate(){
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		//		class MyLocThread extends Thread 
//		{ 
//		public MyLocThread () 
//		{ 
//			setDaemon(true); 
//			setName("LocationThread"); 
//		} 
//
//		public void run() 
//		{ 
//		//Looper.prepare(); 
//		//mUserLocationHandler = new Handler();
//			showToastMessage("HERE");
//		locationManager.requestLocationUpdates( 
//		  LocationManager.GPS_PROVIDER, 
//		  0L, 
//		  0L, 
//		  locationListener,
//		  Looper.getMainLooper()
//		); 
//		Looper.loop();
//		}
//	}
//		getActivity().runOnUiThread(new MyLocThread());
	}
	public  void updateFoodGPSDatabase(String fName,Double lon,Double lat){
		
		ContentValues value=new ContentValues();
		value.put("food_name", fName);
		value.put("latitude", lat.toString());
		value.put("longitude", lon.toString());
		Log.d(TAG, "INSERTING into FoodGPS latitude "+lat+"longitude"+lon + "food name "+ fName);
		mDb.insert("FoodGPS",null,value);
        
    }

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
			ScrollView mlinearLayout = (ScrollView)inflater.inflate(R.layout.activity_food_tracking, container, false);
			/*Generate Food Data*/
	    	generateData();
	    	/*Initialize Location manager*/
	    	// Acquire a reference to the system Location Manager
        	locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        	
        	/*search bar*/
        	mEdtText = (EditText) mlinearLayout.findViewById(R.id.search_src_text);
 	        mEdtText.setOnKeyListener(new View.OnKeyListener() {
 	            public boolean onKey(View v, int keyCode, KeyEvent event) {
 	                if (keyCode==KeyEvent.KEYCODE_ENTER) { 
 	                	searchAll();
 	                }
 	            return false;
 	            }
 	        });
 	        mbtSearch = (ImageButton) mlinearLayout.findViewById(R.id.search_btn);
	        mbtSearch.setOnClickListener(new View.OnClickListener() {
		        	public void onClick(View v) {
		            if (v.getId() == R.id.search_btn) {
		                //listen for results
		            	searchAll();
		            }
		        	}
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						// TODO Auto-generated method stub
						return false;
					}
		        });
	        mbtSpeak = (ImageButton) mlinearLayout.findViewById(R.id.voice_btn);
	        if(checkVoiceRecognition()==true){
		        mbtSpeak.setOnClickListener(new View.OnClickListener() {
		        	public void onClick(View v) {
		            if (v.getId() == R.id.voice_btn) {
		                //listen for results
		                speak(v);
		            }
		        	}
		        });
	        }

	        
        	/*initialize spinner */
	        spin_amount = (Spinner) mlinearLayout.findViewById(R.id.spinner);
	        initializeAmountSpinners();
	        /* initialize listview*/
	        ArrayList<String> sData = new ArrayList<String>();
	        sData.add("No matches");
	        mlv = (ListView) mlinearLayout.findViewById(R.id.foodlist);
	        mlv.setAdapter(new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, sData));
	        mlv.setItemsCanFocus(false);
		    mlv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	        
	        /*Confirm button and new food button*/
	        final Button newFoodBtn = (Button) mlinearLayout.findViewById(R.id.newitembutton);
	        newFoodBtn.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	                // Perform action on click
	            	
	            	Intent intent = new Intent(FoodTrackingActivity.this.getActivity(),NewFoodActivity.class);
	    	        startActivity(intent); 
	            }
	        });
	        /*Frequent Choice Spinner*/
	        freq_choice = (Spinner) mlinearLayout.findViewById(R.id.frequentSpinner);
	        initializeFrequentSpinner();
	        freq_choice.setOnItemSelectedListener(new OnItemSelectedListener() {
	        	@Override
	        	public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	        	    // your code here
	        		if(position>0){
	        		ArrayList<String> sData = new ArrayList<String>();
	        		String temp = freq_choice.getItemAtPosition(position).toString();
				    sData.add(temp);
				    mlv.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_single_choice, sData));
	        		}
	        	}
	        	@Override
	        	public void onNothingSelected(AdapterView<?> parentView) {
	        	    // your code here
	        	}
	        });
	        mbtConfirm = (Button) mlinearLayout.findViewById(R.id.confirmbutton);
	        //mbtConfirm.setEnabled(false);
	        mbtConfirm.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	            	// Define a listener that responds to location updates
	            	locationListener = new LocationListener() {

	            	    public void onStatusChanged(String provider, int status, Bundle extras) {}

	            	    public void onProviderEnabled(String provider) {}

	            	    public void onProviderDisabled(String provider) {}

	    				@Override
	    				public void onLocationChanged(Location loc) {
	    					// get longitude,latitude and time data
	    	                 //ContentValues values = new ContentValues();
	    	                  lon = loc.getLongitude();
	    	                  lat = loc.getLatitude();
	    	                  gpstime = loc.getTime();
	    	                  showToastMessage("longitude "+lon+"latitude "+lat+"time "+gpstime);
	    	                // updateFoodGPSDatabase(lon,lat);
	    				}
	            	  };
	            	  /* request location and store to database*/
	            	 // Register the listener with the Location Manager to receive location updates
	            	  //requestGPSupdate();
	            	  //updateFoodGPSDatabase(lon,lat);
	        
	            	  //for testing
	            	  lon = 5.2;
	            	  lat = 5.2;
	            	  /* Get the position of user's selection*/
	            	  int pos = mlv.getCheckedItemPosition();
	            	  int spinner_pos = freq_choice.getSelectedItemPosition();
	            	  if(pos>=0)
	            		  updateFoodGPSDatabase(mlv.getItemAtPosition(pos).toString(),lon,lat);
	            	  else if(spinner_pos>0){
	            		  updateFoodGPSDatabase(freq_choice.getItemAtPosition(spinner_pos).toString(),lon,lat);
	            	  }
	            	  
	            	  //remove listener
	            	//locationManager.removeUpdates (locationListener);
	            	 
	            }
	        });
	        return mlinearLayout;
	}

}
