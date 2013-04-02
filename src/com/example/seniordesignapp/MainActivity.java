package com.example.seniordesignapp;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;


public class MainActivity extends FragmentActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
	//Better Change this code to ActionBar, because this is the default after android 3.0
	
	//Tag for GPS log
	private static final String DEBUG_TAG = MainActivity.class.getSimpleName();
	
	private TabHost mTabHost;
	private ViewPager mViewPager;
	private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, MainActivity.TabInfo>();
	private PagerAdapter mPagerAdapter;
	/**
	 * Maintains extrinsic info of a tab's construct
	 */
	private class TabInfo {
		 private String tag;
         private Class<?> clss;
         private Bundle args;
         TabInfo(String tag, Class<?> clazz, Bundle args) {
        	 this.tag = tag;
        	 this.clss = clazz;
        	 this.args = args;
         }

	}
	/**
	 * A simple factory that returns dummy views to the Tabhost
	 */
	class TabFactory implements TabContentFactory {
		private final Context mContext;
	    public TabFactory(Context context) {
	        mContext = context;
	    }
	    public View createTabContent(String tag) {
	        View v = new View(mContext);
	        v.setMinimumWidth(0);
	        v.setMinimumHeight(0);
	        return v;
	    }
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.initialiseTabHost(savedInstanceState);
		if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
        }
		// Intialise ViewPager
		this.intialiseViewPager();
	}
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("tab", mTabHost.getCurrentTabTag()); //save the tab selected
        super.onSaveInstanceState(outState);
    }

    private void intialiseViewPager() {
		List<Fragment> fragments = new Vector<Fragment>();
		fragments.add(Fragment.instantiate(this, HomePageFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, FoodTrackingFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, CalibrationFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, ProfileFragment.class.getName()));
		
		this.mPagerAdapter  = new PageAdapter(super.getSupportFragmentManager(), fragments);
		this.mViewPager = (ViewPager)super.findViewById(R.id.viewpager);
		this.mViewPager.setAdapter(this.mPagerAdapter);
		this.mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
		    @Override
		    public void onPageSelected(int position) {
		        final InputMethodManager imm = (InputMethodManager)getSystemService(
		            Context.INPUT_METHOD_SERVICE);
		        imm.hideSoftInputFromWindow(mViewPager.getWindowToken(), 0);
		    }

		    @Override
		    public void onPageScrolled(int position, float offset, int offsetPixels) {
		    }

		    @Override
		    public void onPageScrollStateChanged(int state) {
		    }
		});
    }
	private void initialiseTabHost(Bundle args) {
		mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();
        mTabHost.getTabWidget().setBackgroundColor(Color.LTGRAY);
        TabInfo tabInfo = null;
        MainActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab1").setIndicator("Homepage"), ( tabInfo = new TabInfo("Tab1", HomePageFragment.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        MainActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab2").setIndicator("Food Tracking"), ( tabInfo = new TabInfo("Tab2", FoodTrackingFragment.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        MainActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab3").setIndicator("Calibration"), ( tabInfo = new TabInfo("Tab3", CalibrationFragment.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        MainActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab4").setIndicator("Profile"), ( tabInfo = new TabInfo("Tab3", ProfileFragment.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        // Default to first tab
        //this.onTabChanged("Tab1");
        //
        mTabHost.setOnTabChangedListener(this);
	}

	/**
	 * Add Tab content to the Tabhost
	 */
	private static void AddTab(MainActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
		// Attach a Tab view factory to the spec
		tabSpec.setContent(activity.new TabFactory(activity));
        tabHost.addTab(tabSpec);
	}
	public void onTabChanged(String tag) {
		TabInfo newTab = this.mapTabInfo.get(tag);
		int pos = this.mTabHost.getCurrentTab();
		this.mViewPager.setCurrentItem(pos);
    }
	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}
	@Override
	public void onPageSelected(int position) {
		this.mTabHost.setCurrentTab(position);
	}
	@Override
	public void onPageScrollStateChanged(int state) {
	}
	
}

