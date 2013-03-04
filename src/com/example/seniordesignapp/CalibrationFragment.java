package com.example.seniordesignapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class CalibrationFragment extends Fragment {
	ImageButton runningButton,walkingButton;

    public void addListenerOnButton() {
		runningButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			}
 
		});
		walkingButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			}
 
		});
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
		RelativeLayout mRelativeLayout = (RelativeLayout)inflater.inflate(R.layout.fragment_calibration, container, false);
		runningButton = (ImageButton) mRelativeLayout.findViewById(R.id.calibration_running);
		walkingButton = (ImageButton) mRelativeLayout.findViewById(R.id.calibration_walking);
		addListenerOnButton();
		return mRelativeLayout;
    }
}
