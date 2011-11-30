package org.me.TagStore.ui;

import java.util.ArrayList;

import junit.framework.Assert;

import org.me.TagStore.AddFileTagActivity;
import org.me.TagStore.ConfigurationTabActivity;
import org.me.TagStore.R;
import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.Logger;
import org.me.TagStore.core.PendingFileChecker;
import org.me.TagStore.interfaces.BackKeyCallback;
import org.me.TagStore.interfaces.TabPageIndicatorCallback;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

public class MainPageAdapter extends FragmentStatePagerAdapter implements
		TabPageIndicatorCallback, ViewPager.OnPageChangeListener {

	/**
	 * stores the fragments
	 */
	private final ArrayList<Fragment> m_fragments;

	/**
	 * stores the titles
	 */
	private final ArrayList<String> m_titles;

	/**
	 * single instance of page adapter
	 */
	private static MainPageAdapter s_Instance;
	
	/**
	 * application context
	 */
	private final Context m_context;
	
	/**
	 * stores the current view class
	 */
	private String m_cached_view_class;
	
	/**
	 * stores the tab indicator
	 */
	private final TabPageIndicator m_tab_indicator;
	
	/**
	 * constructor of class MainPageAdapter
	 * @param fm fragment manager
	 * @param context context
	 * @param tab_indicator tab indicator
	 */
	private MainPageAdapter(FragmentManager fm, Context context, TabPageIndicator tab_indicator) {

		//
		// initialize super class
		//
		super(fm);

		//
		// init members
		//
		m_fragments = new ArrayList<Fragment>();;
		m_titles = new ArrayList<String>();
		m_context = context;
		m_tab_indicator = tab_indicator;
		
		//
		// build default fragments
		//
		m_cached_view_class = getCurrentViewClass();
		addFragment(m_cached_view_class, m_context.getString(R.string.tagstore));
		addFragment(ConfigurationTabActivity.class.getName(), m_context.getString(R.string.configuration));
		addPendingFileItem();
	}

	/**
	 * adds the pending file item when there are pending files
	 */
	private void addPendingFileItem() {
		
		//
		// construct pending file checker
		//
		PendingFileChecker file_checker = new PendingFileChecker();
		
		//
		// any files pending
		//
		boolean pending_files = file_checker.hasPendingFiles();
		if (pending_files) {
			
			//
			// add pending file fragment
			//
			addFragment(AddFileTagActivity.class.getName(), m_context.getString(R.string.new_file));
		}
	}

	/**
	 * returns the current view class
	 * 
	 * @return
	 */

	private String getCurrentViewClass() {

		//
		// acquire shared preference settings instance
		//
		SharedPreferences settings = m_context
				.getSharedPreferences(
						ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
						Context.MODE_PRIVATE);

		//
		// get current list view class
		//
		return settings.getString(
				ConfigurationSettings.CURRENT_LIST_VIEW_CLASS,
				ConfigurationSettings.DEFAULT_LIST_VIEW_CLASS);
	}

	/**
	 * returns an instance of mainpageadapter
	 * @return
	 */
	public static MainPageAdapter getInstance() {
		
		//
		// return instance
		//
		return s_Instance;
		
	}
	
	/**
	 * constructs the MainPageAdapter
	 * @param fm fragment manager
	 * @param context application context
	 * @param tab_indicator tab page indicator
	 * @return instance of MainPageAdapter
	 */
	public static MainPageAdapter constructPageAdapter(FragmentManager fm, Context context, TabPageIndicator tab_indicator) {
		
		//
		// build new instance
		//
		s_Instance = new MainPageAdapter(fm, context, tab_indicator);
		
		//
		// done
		//
		return s_Instance;
	}
	
	//
	//=================================================================
	//
	// public support functions
	//	

	/**
	 * selects the fragment of index to be current
	 * @param index index of fragment
	 */
	public void setCurrentFragmentByIndex(int index) {
		
		//
		// sanity check
		//
		Assert.assertTrue(index < m_fragments.size()); 
		
		//
		// set new index
		//
		m_tab_indicator.setCurrentItem(index);
	}
	
	
	/**
	 * removes the add file fragment 
	 */
	public void removeAddFileTagFragment() {

		for(Fragment fragment : m_fragments)
		{
			if (fragment instanceof AddFileTagActivity)
			{
				int index = m_fragments.indexOf(fragment);
				
				m_fragments.remove(index);
				m_titles.remove(index);
				
				//m_page_adapter.notifyDataSetChanged();	
				m_tab_indicator.setCurrentItem(index - 1);
				break;
			}
		}
	}

	/**
	 * adds the fragment to the list of fragments
	 * @param class_name
	 * @param fragment_name
	 */
	public void addFragment(String class_name, String fragment_name) {
		
		addFragmentByIndex(class_name, fragment_name, m_fragments.size());
	}
		

	/**
	 * adds the fragment at a specific position
	 * @param class_name class name
	 * @param fragment_name fragment title name
	 * @param index index to add
	 */
	public void addFragmentByIndex(String class_name, String fragment_name, int index) {

		//
		// create fragment
		//
		Fragment fragment = Fragment.instantiate(m_context, class_name);
		
		//
		// add fragment to list
		//
		m_fragments.add(index, fragment);
		m_titles.add(index, fragment_name);
	}
	
	
	/**
	 * returns true when the add file tag fragment is present
	 * @return
	 */
	public boolean isAddFileTagFragmentActive() {
		
		for(Fragment fragment : m_fragments)
		{
			if (fragment instanceof AddFileTagActivity)
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	//
	//=================================================================
	//
	// PagerView support functions
	//
	@Override
	public Fragment getItem(int position) {
		
		if (position != 0)
		{
			//
			// return cached entry
			//
			return m_fragments.get(position);
		}
		
		//
		// get current view class
		//
		String current_view_class = getCurrentViewClass();
		
		//
		// did the view class change
		//
		if (current_view_class.equalsIgnoreCase(m_cached_view_class))
		{
			//
			// no change, return cached fragment
			//
			return m_fragments.get(0);
		}

		//
		// construct fragment
		//
		Fragment fragment = Fragment.instantiate(m_context, current_view_class);
		
		//
		// replace old fragment with new one
		//
		m_fragments.remove(0);
		m_fragments.add(0, fragment);
		
		//
		// store new view class
		//
		m_cached_view_class = current_view_class;
		
		//
		// done
		//
		return fragment;
	}

	@Override
	public int getCount() {
		return m_fragments.size();
	}

	//
	//=================================================================
	//
	// TabIndicatorCallback support function
	//	
	@Override
	public String getTitle(int position) {
		return m_titles.get(position);
	}
	

	//
	//=================================================================
	//
	// ViewPager.OnPageChangeListener interface functions
	//
	@Override
	public void onPageScrollStateChanged(int state) {
		
	}
	
	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		
	}

	@Override
	public void onPageSelected(int position) {

		//
		// get current item position
		//
		int old_position = m_tab_indicator.getCurrentItem();
		
		Logger.i("onPageSelected new_position: " + position + " old position: " + old_position);
		
		
		if (old_position == 2 && position == 1)
		{
			if (old_position == m_fragments.size())
			{
				//
				// tab was already removed
				//
				return;
			}
			
			//	
			// remove every fragment on back button except add file fragment
			//
			Fragment fragment = m_fragments.get(old_position);
			if (!(fragment instanceof AddFileTagActivity)) {
				
				//
				// remove fragment from list
				//
				m_fragments.remove(old_position);
				m_titles.remove(old_position);
				
				//
				// refresh HACK: it seems ViewPager only fetches the item position when tab
				// was changed multiple times
				//
				m_tab_indicator.setCurrentItem(0);
				m_tab_indicator.setCurrentItem(1);				
			}
		}
	}

	/**
	 * returns the current active time
	 * @return int
	 */
	public int getCurrentItem() {
		return m_tab_indicator.getCurrentItem();
	}
	
	public boolean notifyBackKeyPressed() {

		//
		// get current fragment
		//
		int index = m_tab_indicator.getCurrentItem();

		
		try
		{
			//
			// get fragment at that position
			//
			Fragment fragment = m_fragments.get(index);

			//
			// cast into backkey callback
			//
			BackKeyCallback callback = (BackKeyCallback)fragment;
			
			//
			// invoke callback
			//
			callback.backKeyPressed();
			
			//
			// done
			//
			return true;
		}
		catch(ClassCastException exc)
		{
			Logger.e("ClassCastException while accessing index: " + index);
			return false;
		}
		catch(IndexOutOfBoundsException exc)
		{
			Logger.e("Error: IndexOutOfBoundsException while accessing index " + index);
			return false;
		}
	}
}