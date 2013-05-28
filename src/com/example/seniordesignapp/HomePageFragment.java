package com.example.seniordesignapp;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.PopupWindow;
import com.google.android.maps.*;


//import com.handmark.pulltorefresh.library.PullToRefreshBase;
//import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
//import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;

public class HomePageFragment extends Fragment {
	
	ImageView GLcheck;
	ExpandableListView lv;
	ToggleButton tButton;
    private DatabaseHelper mDbHelper;
    private TextView TotalGL;
    private SQLiteDatabase mDb;
    private Cursor mCursor;
    private static String TAG = "HomePageFramentDynamicLog";
    ProgressBar GLprogressBar;
    final double GL_LIMIT = 100; 
    final double GL_SCALE = 70; //Scale to 70% as it is 100%
    TextView leftoverGL;
    TextView recommendations;
    private ArrayList<String> tips = new ArrayList<String>();
    
	//Testing String timeStamp;
	private String[] groups;
	private Button refreshButton;
	class MyExpandableListAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnChildClickListener, OnClickListener {

		private String[] groups= setGroupData();
		private String[][] children = setChildGroupData();
		private ArrayList<String> childItem; 
		
		public String[] setGroupData() {// WIP - Hard Code for Now..
			
			ArrayList<String> groupItem = new ArrayList<String>();
			childItem = new ArrayList<String>();
			//ArrayList<String> childItem = new ArrayList<String>();
			String timeStamp;
			String fname;
			String GL;
			String item;
			mDbHelper = new DatabaseHelper(getActivity());
 			mDb = mDbHelper.getWritableDatabase();
 			String sql = "SELECT * FROM foodGPS  ORDER BY GPS_time DESC LIMIT 3";
			mCursor = mDb.rawQuery(sql,null);
			
			if(mCursor.getCount()>0){ //now it is taking the first match WIP fix later
				Log.d(TAG,"Adding items to Dynamic Log");
				mCursor.moveToFirst();
				timeStamp = mCursor.getString(mCursor.getColumnIndex("GPS_time"));
				timeStamp = timeStamp.substring(4, 6) + "/" + 
							timeStamp.substring(6, 8) + " "+ 
							timeStamp.substring(8, 10) + ":" + 
							timeStamp.substring(10, 12);
				fname = mCursor.getString(mCursor.getColumnIndex("food_name"));
				fname = fname.substring(0, 18)  + "...";
				GL = mCursor.getString(mCursor.getColumnIndex("GL"));
				item = timeStamp + " Had" + fname;
 				groupItem.add(item);
 				childItem.add("GL = " + GL);
					while (!mCursor.isLast()) {
						mCursor.moveToNext();
						timeStamp = mCursor.getString(mCursor.getColumnIndex("GPS_time"));
						timeStamp = timeStamp.substring(4, 6) + "/" + 
									timeStamp.substring(6, 8) + " "+ 
									timeStamp.substring(8, 10) + ":" + 
									timeStamp.substring(10, 12);
						fname = mCursor.getString(mCursor.getColumnIndex("food_name"));
						fname = fname.substring(0, 18)  + "...";
						GL = mCursor.getString(mCursor.getColumnIndex("GL"));
						item = timeStamp + " Had" + fname;
		 				groupItem.add(item);
		 				childItem.add("GL = " + GL);
					}
					
			}
			else{
				Log.d(TAG,"Dynamic Log is empty");
				groupItem.add("Empty");
				childItem.add("Empty");
				showToastMessage("Nothing is in the database...");
			}
			
			String[] stockArr = new String[groupItem.size()];
			stockArr = groupItem.toArray(stockArr);
			return stockArr;
		}

		public String[][] setChildGroupData() { // WIP - hard coding

			String[][] storeChild = new String[childItem.size()][1];
			
			for (int i = 0; i < childItem.size(); i++ ){
				storeChild[i][0] = childItem.get(i);
			}
			
			return storeChild; 
		}

		// public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.activity_home_page, menu);
		// return true;
		// }

		public Object getChild(int groupPosition, int childPosition) {
			return children[groupPosition][childPosition];
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		public int getChildrenCount(int groupPosition) {
			return children[groupPosition].length;
		}

		public TextView getGenericView() {
			// Layout parameters for the ExpandableListView
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, 64);
			TextView textView = new TextView(
					HomePageFragment.this.getActivity());
			textView.setLayoutParams(lp);
			// Center the text vertically
			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			// Set the text starting position
			textView.setPadding(36, 0, 0, 0);
			
			return textView;
		}

		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			View childView = convertView;
			if (childView == null) {
				LayoutInflater vi = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				childView = vi.inflate(R.layout.childrow, null);
			}
			TextView childText = (TextView) childView
					.findViewById(R.id.child_name);
			if (childText != null) {
				childText.setText(getChild(groupPosition, childPosition)
						.toString());
			}

			return childText;

		}

		public Object getGroup(int groupPosition) {
			return groups[groupPosition];
		}

		public int getGroupCount() {
			return groups.length;
		}

		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			View parentView = convertView;
			if (parentView == null) {
				LayoutInflater vi = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				parentView = vi.inflate(R.layout.grouprow, null);
			}
			TextView parentText = (TextView) parentView
					.findViewById(R.id.group_name);
			if (parentText != null) {
				parentText.setText(getGroup(groupPosition).toString());
			}

			return parentText;

		}

		public boolean hasStableIds() {
			return true;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}




		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			// TODO Auto-generated method stub
			return false;
		}




		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			
			
		}


	}
	
	
	public class ShowCustomProgressBarAsyncTask extends
			AsyncTask<Void, Integer, Void> {

		int myProgress;
		int currentGL;
		int limitGL;

		public ShowCustomProgressBarAsyncTask(int GL, int limit) {
			currentGL = GL;
			limitGL = limit ;
		}

		@Override
		protected void onPostExecute(Void result) {

		}

		@Override
		protected void onPreExecute() {
			myProgress = 0;
			// GLprogressBar.setSecondaryProgress(0);
		}

		@Override
		protected Void doInBackground(Void... params) {
			while (myProgress < currentGL) {
				myProgress++;
				publishProgress(myProgress);
				SystemClock.sleep(20);
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {

			if (currentGL  <= limitGL ) {
				GLprogressBar.setProgress((int) (values[0]* GL_SCALE / 100));
				leftoverGL.setText("+" + (int) (GL_LIMIT - values[0]));
				leftoverGL.setTextColor(Color.parseColor("#FF58A03D"));

			} else if ((currentGL  >= limitGL ) && (currentGL < limitGL/GL_SCALE * 100)) {
				GLprogressBar.setProgress(0);
				GLprogressBar.setSecondaryProgress((int) (values[0]* GL_SCALE / 100));
				leftoverGL.setText("-" + (int) Math.abs(values[0] - GL_LIMIT));
				leftoverGL.setTextColor(Color.parseColor("#FFCD0102"));

			} else {
				GLprogressBar.setProgress(0);
				GLprogressBar.setSecondaryProgress(100);
				leftoverGL.setText("-" + (int) Math.abs(values[0] - GL_LIMIT));
				leftoverGL.setTextColor(Color.parseColor("#FFCD0102"));

			}
		}
	}
	

	

	Gauge meter; 
	//Button add;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			// We have different layouts, and in one of them this
			// fragment's containing frame doesn't exist. The fragment
			// may still be created from its saved state, but there is
			// no reason to try to create its view hierarchy because it
			// won't be displayed. Note this is not needed -- we could
			// just run the code below, where we would create and return
			// the view hierarchy; it would just never be used.
			return null;
		}

		LinearLayout mlinearLayout = (LinearLayout) inflater.inflate(
				R.layout.activity_home_page, container, false);
		
/*		mlinearLayout.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
			        showToastMessage("Touched!!");
			        return true;
			    }
				return false;
			}
		});*/
		
		leftoverGL = (TextView) mlinearLayout.findViewById(R.id.leftoverGL);
		TotalGL = (TextView) mlinearLayout.findViewById(R.id.textView2);
		recommendations = (TextView) mlinearLayout.findViewById(R.id.recomendations);
		
	//String tempGl=mCursor.getString(mCursor.getColumnIndex("GPS_time"));
		String sql = "SELECT SUM(GL) AS TotalGL FROM foodGPS";
		mDbHelper = new DatabaseHelper(getActivity());
			mDb = mDbHelper.getWritableDatabase();
		mCursor = mDb.rawQuery(sql,null);
		mCursor.moveToFirst();	
		String tempGl=mCursor.getString(mCursor.getColumnIndex("TotalGL"));
		if (tempGl == null) {
			TotalGL.setText("0");
			leftoverGL.setText(""+ (int) GL_LIMIT); }
		else
			TotalGL.setText(tempGl);
		//TotalGL.setTextColor(-16711681);
		//TotalGL.setText(temporaryGL);
		//meter = (Gauge) mlinearLayout.findViewById(R.id.meter);
		//meter.setValue(30);
		
		//meter.setValue(temp);
		//meter.setOnClickListener(this);
		
		//add = (Button) mlinearLayout.findViewById(R.id.add);
		//add.setOnClickListener(this);
		
		tButton = (ToggleButton) mlinearLayout.findViewById(R.id.toggleButton1);

		tButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {

				// if(isChecked){
				// tvStateofToggleButton.setText("ON");
				// }else{
				// tvStateofToggleButton.setText("OFF");
				// }

			}
		});
		
		
		GLprogressBar = (ProgressBar) mlinearLayout.findViewById(R.id.GLprogressBar);
		GLcheck = (ImageView) mlinearLayout.findViewById(R.id.gl_check);
		int anything;
		//boolean a = tempGl.isEmpty();
		if (tempGl == null){
			anything = 0;
			GLprogressBar.setProgress(0);
			//GLprogressBar.setSecondaryProgress(50);
		}	
		else {
		 anything = Integer.parseInt(tempGl);
		 //GLprogressBar.setProgress(Integer.parseInt(tempGl));
		 new ShowCustomProgressBarAsyncTask(Integer.parseInt(tempGl), (int) GL_LIMIT).execute(); 
			if ( anything > GL_LIMIT ){
				GLcheck.setImageResource(R.drawable.ic_delete);
			}
			else
				GLcheck.setImageResource(R.drawable.btn_check_buttonless_on);
		}
		

		
		lv = (ExpandableListView) mlinearLayout.findViewById(R.id.log_list);
		MyExpandableListAdapter expandableAdapter = new MyExpandableListAdapter();
		lv.setAdapter(expandableAdapter);
		lv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
		PopupWindow pw;
		private MapView mapView;
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				//showToastMessage("Work! + ParentID =" + groupPosition + " ChildID = " + childPosition );
				
				
		        //We need to get the instance of the LayoutInflater, use the context of this activity
		        LayoutInflater inflater = (LayoutInflater) getActivity()
		                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		        //Inflate the view from a predefined XML layout
		        View layout = inflater.inflate(R.layout.popup_layout,
		                (ViewGroup) getActivity().findViewById(R.id.popup_element));
				 // create a 300px width and 470px height PopupWindow
		        pw = new PopupWindow(layout, 375, 600, true);
		        // display the popup in the center
		        pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
		        Button cancelButton = (Button) layout.findViewById(R.id.end_data_send_button);
		        
		       // mapView = (MapView) layout.findViewById(R.id.mapview);
		        //mapView.setBuiltInZoomControls(true);
		        
		        cancelButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						pw.dismiss();
					}
				});
		        
				return false;
			}
		});
		refreshButton = (Button) mlinearLayout.findViewById(R.id.testing);
		refreshButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), MainActivity.class );
				showToastMessage("Refreshing");
				startActivity(i);
				//finish();
				
			}
		});

		return mlinearLayout;
	}

	

	float temp = (float) 20;
	
/*	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.add:
			temp = temp + 20;
			meter.setValue(temp);
			//showToastMessage("It works!!!!");
			showToastMessage("=" + meter.getValue() );
			break;
		}
		return;
		
	}*/
	
	/* Toast Method */
	void showToastMessage(String message) {
		  Toast.makeText(this.getActivity(), message, Toast.LENGTH_SHORT).show();
	}

}
	 

