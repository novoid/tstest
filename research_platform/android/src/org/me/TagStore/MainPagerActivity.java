package org.me.TagStore;

import org.me.TagStore.core.DBManager;
import org.me.TagStore.core.Logger;
import org.me.TagStore.core.ServiceLaunchRunnable;
import org.me.TagStore.core.TagStackManager;
import org.me.TagStore.ui.MainPageAdapter;
import org.me.TagStore.ui.TabPageIndicator;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;

public class MainPagerActivity extends FragmentActivity {

	/**
	 * stores the view pager
	 */
	private ViewPager m_view_pager;

	/**
	 * stores the page adapter
	 */
	private MainPageAdapter m_page_adapter;

	/**
	 * stores the service launcher
	 */
	private ServiceLaunchRunnable m_launcher;
	
	/**
	 * stores the service launcher thread
	 */
	private Thread m_launcher_thread;
	
	/**
	 * stores the tab indicator
	 */
	private TabPageIndicator m_tab_indicator;

	public void onStop() {
		
		super.onStop();
		Logger.e("onStop");
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//
		// check if not back button was pressed
		//
		if (keyCode != KeyEvent.KEYCODE_BACK) {
			//
			// pass on to default handler
			//
			return super.onKeyDown(keyCode, event);
		}
	
		if (m_tab_indicator == null)
		{
			//
			// app not yet initialized
			//
			return super.onKeyDown(keyCode, event);
		}		
		
		//
		// get current view index
		//
		int index = m_tab_indicator.getCurrentItem();
		
		if (index != 0)
		{
			//
			// pass on to default handler
			//
			Logger.i("no backkeycallback");
			return super.onKeyDown(keyCode, event);
		}
		
		
		//
		// acquire tag stack
		//
		TagStackManager tag_stack = TagStackManager.getInstance();
		
		
		//
		// check if object stack is empty
		//
		if (tag_stack.isEmpty()) {
			
			//
			// empty stack
			//
			return super.onKeyDown(keyCode, event);
		}

		if (tag_stack.getSize() == 1) {
			
			//
			// clear tag stack
			//
			tag_stack.clearTags();
		}
		else
		{
			//
			// pop last tag from tag stack
			//
			tag_stack.removeLastTag();
		}
		
		//
		// notify fragment of back key pressed
		//
		if(!m_page_adapter.notifyBackKeyPressed())
		{
			//
			// let it handle base class
			//
			super.onKeyDown(keyCode, event);
		}
		
		//
		// key was handled
		//
		return true;
	}
	
	public void onResume() {
		
		super.onResume();
		
		Logger.e("MainPager::onResume");
		
		
	}
	
	public void onCreate(Bundle savedInstanceState) {

		//
		// initialize sub class
		//
		super.onCreate(savedInstanceState);

		Logger.e("MainPager::onCreate");
		
		//
		// apply layout
		//
		setContentView(R.layout.main_pager);

		//
		// initialize database manager
		//
		DBManager db_man = DBManager.getInstance();
		db_man.initialize(this);

		//
		// launch file watch dog service asynchronously
		//
		startFileWatchdogService();

		//
		// initialize rest of ui
		//
		initialize();
	}

	private void initialize() {

		//
		// find the view pager
		//
		m_view_pager = (ViewPager) findViewById(R.id.view_pager);
		
		//
		// get tab page indicator
		//
		m_tab_indicator = (TabPageIndicator) findViewById(R.id.tab_page_indicator);

		//
		// build main page adapter
		//
		m_page_adapter = MainPageAdapter.constructPageAdapter(getSupportFragmentManager(), getApplicationContext(), m_tab_indicator);

		//
		// apply adapter
		//
		m_view_pager.setAdapter(m_page_adapter);
		
		//
		// set view pager
		//
		m_tab_indicator.setViewPager(m_view_pager);
			
		//
		// set on page change listener
		//
		m_tab_indicator.setOnPageChangeListener(m_page_adapter);
	}

	/**
	 * starts the file watchdog service
	 */
	private void startFileWatchdogService() {

		//
		// construct service launcher
		//
		m_launcher = new ServiceLaunchRunnable(this);
		
		//
		// create a thread which will start the service
		//
		m_launcher_thread = new Thread(m_launcher);

		//
		// now start the thread
		//
		m_launcher_thread.start();
	}

}
