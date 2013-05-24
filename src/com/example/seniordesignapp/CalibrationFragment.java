package com.example.seniordesignapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;

public class CalibrationFragment extends Fragment {
	private int mPosition;
	private RadioGroup mRadioPositionGroup;
	private final String DEBUG_TAG = CalibrationFragment.class.getSimpleName();
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
		Button runningButton = (Button) mRelativeLayout.findViewById(R.id.calibration_running);
		Button walkingButton = (Button) mRelativeLayout.findViewById(R.id.calibration_walking);
		Button sittingButton = (Button) mRelativeLayout.findViewById(R.id.calibration_sitting);
		Button testingButton = (Button) mRelativeLayout.findViewById(R.id.calibration_testing);
		
		RadioButton rhButton = (RadioButton) mRelativeLayout.findViewById(R.id.right_hand);
		RadioButton lhButton = (RadioButton) mRelativeLayout.findViewById(R.id.left_hand);
//		RadioButton pdButton = (RadioButton) mRelativeLayout.findViewById(R.id.pocket_face_down);
//		RadioButton puButton = (RadioButton) mRelativeLayout.findViewById(R.id.pocket_face_up);
		
		mPosition = 0;
		
		mRadioPositionGroup = (RadioGroup) mRelativeLayout.findViewById(R.id.positionGroup);
		mRadioPositionGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
            	if(checkedId == R.id.right_hand){
        			mPosition = 0;
//        			Log.d(DEBUG_TAG,"position "+mPosition);
                }
               if(checkedId == R.id.left_hand){
            	   mPosition = 1;
//            		Log.d(DEBUG_TAG,"position "+mPosition);
               }
//               if(checkedId == R.id.pocket_face_down){
//            	   mPosition = 2;
////            	   	Log.d(DEBUG_TAG,"position "+mPosition);
//               }
//               if(checkedId == R.id.pocket_face_up){
//            	   mPosition = 3;
////            	   	Log.d(DEBUG_TAG,"position "+mPosition);
//               }
			}
          });
		
		
		runningButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	            Intent intent = new Intent(getActivity(), CalibrationActivity.class);
	            intent.putExtra("mode", "running");
	            intent.putExtra("position", mPosition);
	            getActivity().startActivity(intent);
			}
		});
		walkingButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), CalibrationActivity.class);
				intent.putExtra("mode", "walking");
				intent.putExtra("position", mPosition);
	            getActivity().startActivity(intent);
			}
		});
		sittingButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), CalibrationActivity.class);
				intent.putExtra("mode", "sitting");
				intent.putExtra("position", mPosition);
	            getActivity().startActivity(intent);
			}
		});
		testingButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), TestingActivity.class);
	            getActivity().startActivity(intent);
			}
		});
		return mRelativeLayout;
    }
}
