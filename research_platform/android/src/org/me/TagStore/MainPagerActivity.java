package org.me.TagStore;

import java.io.File;
import java.util.Locale;

import org.me.TagStore.core.ConfigurationChecker;
import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.DBManager;
import org.me.TagStore.core.EventDispatcher;
import org.me.TagStore.core.Logger;
import org.me.TagStore.core.MainFileSystemObserverNotification;
import org.me.TagStore.core.MainServiceConnection;
import org.me.TagStore.core.ServiceLaunchRunnable;
import org.me.TagStore.core.StorageTimerTask;
import org.me.TagStore.core.TagStackManager;
import org.me.TagStore.core.TagStoreFileChecker;
import org.me.TagStore.core.VocabularyManager;
import org.me.TagStore.ui.MainPageAdapter;
import org.me.TagStore.ui.TabPageIndicator;
import org.me.TagStore.ui.ToastManager;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class MainPagerActivity extends FragmentActivity {

	/**
	 * stores the view pager
	 */
	private ViewPager m_view_pager = null;

	/**
	 * stores the page adapter
	 */
	private MainPageAdapter m_page_adapter = null;

	/**
	 * stores the service launcher
	 */
	private ServiceLaunchRunnable m_launcher = null;
	
	/**
	 * stores the service connection
	 */
	private MainServiceConnection m_connection = null;
	
	/**
	 * storage timer task
	 */
	private StorageTimerTask m_timer_task = null;
	
	/**
	 * register notification
	 */
	private MainFileSystemObserverNotification m_notification = null;

	
	/**
	 * stores the service launcher thread
	 */
	private Thread m_launcher_thread = null;
	
	/**
	 * stores the tab indicator
	 */
	private TabPageIndicator m_tab_indicator = null;

	/**
	 * tagstore file checker
	 */
	private TagStoreFileChecker m_file_checker = null; 
	
	/**
	 * configuration checker
	 */
	private ConfigurationChecker m_configuration_checker = null;
	
	
	public void onStop() {
		
		super.onStop();

		//
		// informal debug print
		//
		Logger.e("MainPagerActivity::onStop");

		if (m_connection != null && m_notification != null)
		{
			//
			// unregister external observer
			//
			m_connection.unregisterExternalNotification(m_notification);
		
			if (m_connection.isServiceConnected())
			{
				//
				// unbind service connection
				//
				getApplicationContext().unbindService(m_connection);
			}
		}
		else
		{
			Logger.e("Error: service not connected");
		}
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
		// notify fragment of back key pressed via event dispatcher
		//
		EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.BACK_KEY_EVENT, null);
		
		//
		// key was handled
		//
		return true;
	}
	
	public void onResume() {
		
		super.onResume();
		
		Logger.e("MainPager::onResume");
	}
	
	public void onRestart() {
		
		super.onRestart();
		
		Logger.e("MainPager::onRestart");
		
		//
		// register external notification
		//
		m_connection.registerExternalNotification(m_notification);
				
		//
		// restart launcher thread
		//
		m_launcher_thread = new Thread(m_launcher);
		m_launcher_thread.start();
		
		//
		// register file checker task / configuration checker task
		//
		m_timer_task.addCallback(m_file_checker);
		m_timer_task.addCallback(m_configuration_checker);
		
		initialize();
	}
	
	/**
	 * initializes the database
	 */
	private void initDatabase() {
		
		long start = System.currentTimeMillis();
		
		//
		// initialize database manager
		//
		DBManager db_man = DBManager.getInstance();
		db_man.initialize(this);
		
		long diff = System.currentTimeMillis() - start;
		
		Logger.e("DB startup time: " + diff);
		
		//
		// check if the default storage directory is registered
		//
		String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		path += File.separator + ConfigurationSettings.TAGSTORE_DIRECTORY;
		path += File.separator + getString(R.string.storage_directory);
		
		if (!db_man.isDirectoryObserved(path))
		{
			//
			// register path
			//
			db_man.addDirectory(path);
		}
	}
	
	/**
	 * initializes the configuration checker
	 */
	private void initConfiguration() {
		
		//
		// construct configuration checker
		//
		m_configuration_checker = new ConfigurationChecker(this);

		//
		// construct file checker
		//
		m_file_checker = new TagStoreFileChecker();
		
		//
		// acquire instance of storage timer task
		//
		m_timer_task = StorageTimerTask.acquireInstance();
		
		//
		// register tasks
		//
		m_timer_task.addCallback(m_configuration_checker);
		m_timer_task.addCallback(m_file_checker);
		
	}
	
	/**
	 * initializes the toast manager
	 */
	private void initToastManager()
	{
		//
		// initializes the toast manager
		//
		ToastManager.buildToastManager(getApplicationContext());
	}
	
	private void initLocale()  {
		
		   String languageToLoad  = "en";
		    Locale locale = new Locale(languageToLoad); 
		    Locale.setDefault(locale);
		    Configuration config = new Configuration();
		    config.locale = locale;
		    getBaseContext().getResources().updateConfiguration(config, 
		    getBaseContext().getResources().getDisplayMetrics());
	}
	
	private void initVocabulary() {
		
		//
		// initialize vocabulary manager
		//
		VocabularyManager.buildVocabularyManager(getApplicationContext());
	}
	/**
	 * initializes the application
	 */
	private void initApplication() {
		
		//
		// initialize database manager
		//
		initDatabase();

		//
		// launch file watch dog service asynchronously
		//
		startFileWatchdogService();

		//
		// init cfg
		//
		initConfiguration();
		
		//
		// run the configuration tasks
		//
		initConfiguration();

		//
		// init vocabulary manager
		//
		initVocabulary();
	}
	
	public void onStart() {
		
		super.onStart();
		
		initToastManager();
	}
	
	
	public void onCreate(Bundle savedInstanceState) {

		//
		// initialize sub class
		//
		super.onCreate(savedInstanceState);

		Logger.e("MainPager::onCreate");
		
		//initLocale();

		//
		// no window should be displayed
		//
		requestWindowFeature(Window.FEATURE_NO_TITLE);


		//
		// apply layout
		//
		setContentView(R.layout.main_pager);

		//
		// init application
		//
		initApplication();		
		
		//
		// initialize rest of ui
		//
		initialize();
		
/*		
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectNetwork()   // or .detectAll() for all detectable problems
        .penaltyLog()
        .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectAll()
        .penaltyLog()
        .penaltyDeath()
        .build());
*/        
	}

	private void initialize() {

		//
		// find the view pager
		//
		m_view_pager = (ViewPager) findViewById(R.id.view_pager);
		if (m_view_pager == null)
		{
			//
			// failed to initialize
			//
			ToastManager.getInstance().displayToastWithString("Error: failed to initialize application layout");
			return;
		}
		
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
		// construct new notification
		//
		m_notification = new MainFileSystemObserverNotification(this);

		//
		// construct service connection
		//
		m_connection = new MainServiceConnection();
		
		//
		// register external notification
		//
		m_connection.registerExternalNotification(m_notification);
		
		//
		// construct service launcher
		//
		m_launcher = new ServiceLaunchRunnable(getApplicationContext(), m_connection);
		
		//
		// create a thread which will start the service
		//
		m_launcher_thread = new Thread(m_launcher);
		//
		// now start the thread
		//
		m_launcher_thread.start();
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		
		//
		// call base class
		//
		super.onOptionsItemSelected(item);
		
		//
		// informal debug print
		//
		Logger.i("onOptionsItemSelected " + item.getTitle());
		
		Intent intent = null;
		switch(item.getItemId())
		{
		    case R.string.configuration:
		    	intent = new Intent(MainPagerActivity.this, ConfigurationTabActivity.class);
		    	break;
		    case R.string.sync_tagstore:
		    	intent = new Intent(MainPagerActivity.this, SynchronizeTagStoreActivity.class);
		    	break;
		    case R.string.about_tagstore:
		    	intent = new Intent(MainPagerActivity.this, InfoTab.class);
		    	break;
		}
		
		//
		// start activity
		//
		super.startActivity(intent);
		return true;
	}
	
	public boolean onCreateOptionsMenu (Menu menu)
	{
		//
		// initialize the options menu
		//
		
		menu.add(Menu.NONE, R.string.configuration, Menu.NONE, R.string.configuration).setIcon(R.drawable.options_selected);
		menu.add(Menu.NONE, R.string.sync_tagstore, Menu.NONE, R.string.sync_tagstore).setIcon(R.drawable.refresh);
		menu.add(Menu.NONE, R.string.about_tagstore, Menu.NONE, R.string.about_tagstore).setIcon(R.drawable.info);
		
		//
		// done
		//
		return super.onCreateOptionsMenu(menu);
	}
}
