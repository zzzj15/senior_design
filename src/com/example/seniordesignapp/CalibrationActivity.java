package com.example.seniordesignapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.os.CountDownTimer;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

public class CalibrationActivity extends Fragment {
	ImageButton runningButton,walkingButton;
	private MyCount mc;

    public void addListenerOnButton() {
    	 
		runningButton.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
//			   Toast.makeText(CalibrationActivity.this,
//				"running button is clicked!", Toast.LENGTH_SHORT).show();
//				mc = new MyCount(30000, 1000);  
//		        mc.start();
			}
 
		});
		walkingButton.setOnClickListener(new OnClickListener() {
			 
			@Override
			public void onClick(View arg0) {
//			   Toast.makeText(CalibrationActivity.this,
//				"walking button is clicked!", Toast.LENGTH_SHORT).show();
//				mc = new MyCount(30000, 1000);  
//		        mc.start();
			}
 
		});
 
	}
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_calibration, menu);
//        return true;
//    }
    class MyCount extends CountDownTimer {

		public MyCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			// TODO Auto-generated constructor stub
		} 
		@Override     
        public void onFinish() {  
			
        }    
		@Override     
        public void onTick(long millisUntilFinished) {
			//Toast.makeText(CalibrationActivity.this.getActivity(), millisUntilFinished / 1000 + "", Toast.LENGTH_LONG).show();
		}
    	
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
		RelativeLayout mRelativeLayout = (RelativeLayout)inflater.inflate(R.layout.activity_calibration, container, false);
		runningButton = (ImageButton) mRelativeLayout.findViewById(R.id.calibration_running);
		walkingButton = (ImageButton) mRelativeLayout.findViewById(R.id.calibration_walking);
		addListenerOnButton();
		return mRelativeLayout;
    }
}
