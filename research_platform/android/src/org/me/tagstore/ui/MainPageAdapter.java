package org.me.tagstore.ui;

import java.util.ArrayList;

import junit.framework.Assert;

import org.me.tagstore.R;
import org.me.tagstore.core.ConfigurationSettings;
import org.me.tagstore.core.DBManager;
import org.me.tagstore.core.FileTagUtility;
import org.me.tagstore.core.Logger;
import org.me.tagstore.core.PendingFileChecker;
import org.me.tagstore.interfaces.TabPageIndicatorCallback;

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
	 * 
	 * @param fm
	 *            fragment manager
	 * @param context
	 *            context
	 * @param tab_indicator
	 *            tab indicator
	 */
	private MainPageAdapter(FragmentManager fm, Context context,
			TabPageIndicator tab_indicator) {

		//
		// initialize super class
		//
		super(fm);

		//
		// init members
		//
		m_fragments = new ArrayList<Fragment>();
		;
		m_titles = new ArrayList<String>();
		m_context = context;
		m_tab_indicator = tab_indicator;

		//
		// build default fragments
		//
		m_cached_view_class = getCurrentViewClass();
		addFragment(m_cached_view_class, m_context.getString(R.string.app_name));
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
		FileTagUtility utility = new FileTagUtility();
		utility.initializeFileTagUtility();
		file_checker.initializePendingFileChecker(DBManager.getInstance(),
				utility);

		//
		// any files pending
		//
		boolean pending_files = file_checker.hasPendingFiles();
		if (pending_files) {

			//
			// add pending file fragment
			//
			addFragment(ConfigurationSettings.TAG_FILE_ACTIVITY_CLASS_NAME,
					m_context.getString(R.string.new_file));
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
		SharedPreferences settings = m_context.getSharedPreferences(
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
	 * 
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
	 * 
	 * @param fm
	 *            fragment manager
	 * @param context
	 *            application context
	 * @param tab_indicator
	 *            tab page indicator
	 * @return instance of MainPageAdapter
	 */
	public static MainPageAdapter constructPageAdapter(FragmentManager fm,
			Context context, TabPageIndicator tab_indicator) {

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
	// =================================================================
	//
	// public support functions
	//

	/**
	 * selects the fragment of index to be current
	 * 
	 * @param index
	 *            index of fragment
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

		//
		// check if there are at least 2 fragments
		//
		if (m_fragments.size() > 1) {
			//
			// change tab index to first one
			//
			m_tab_indicator.setCurrentItem(0);

			//
			// the add file tag fragment follows the current view class
			//
			m_fragments.remove(1);
			m_titles.remove(1);
		}
	}

	/**
	 * adds the fragment to the list of fragments
	 * 
	 * @param class_name
	 * @param fragment_name
	 */
	public boolean addFragment(String class_name, String fragment_name) {

		return addFragmentByIndex(class_name, fragment_name, m_fragments.size());
	}

	/**
	 * adds the fragment at a specific position
	 * 
	 * @param class_name
	 *            class name
	 * @param fragment_name
	 *            fragment title name
	 * @param index
	 *            index to add
	 */
	public boolean addFragmentByIndex(String class_name, String fragment_name,
			int index) {

		if (m_titles.contains(fragment_name))
			return false;

		//
		// create fragment
		//
		Fragment fragment = Fragment.instantiate(m_context, class_name);

		//
		// add fragment to list
		//
		m_fragments.add(index, fragment);
		m_titles.add(index, fragment_name);
		return true;
	}

	/**
	 * returns true when the add file tag fragment is present
	 * 
	 * @return
	 */
	public boolean isAddFileTagFragmentActive() {

		if (m_fragments.size() > 1) {
			//
			// add file fragment is already present
			//
			return true;
		}

		//
		// not present
		//
		return false;
	}

	//
	// =================================================================
	//
	// PagerView support functions
	//
	public Fragment getItem(int position) {

		if (position != 0) {
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
		if (current_view_class.equalsIgnoreCase(m_cached_view_class)) {
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

	public int getCount() {
		return m_fragments.size();
	}

	//
	// =================================================================
	//
	// TabIndicatorCallback support function
	//

	public String getTitle(int position) {

		if (position >= m_titles.size())
			return "invalid" + position;

		return m_titles.get(position);
	}

	//
	// =================================================================
	//
	// ViewPager.OnPageChangeListener interface functions
	//

	public void onPageScrollStateChanged(int state) {

	}

	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {

	}

	public void onPageSelected(int position) {

		//
		// get current item position
		//
		int old_position = m_tab_indicator.getCurrentItem();

		Logger.i("onPageSelected new_position: " + position + " old position: "
				+ old_position);
	}

	/**
	 * returns the current active time
	 * 
	 * @return int
	 */
	public int getCurrentItem() {
		return m_tab_indicator.getCurrentItem();
	}
}