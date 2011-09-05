package org.me.TagStore;

import java.util.ArrayList;
import org.me.TagStore.R;

import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

/**
 * This class is the first activity which is launched when the TagStore
 * application starts. It creates the tabs and adds them to the tab host
 * 
 * @author Johannes Anderwald
 * 
 */
public class MainActivity extends TabActivity {

	/**
	 * holds the instance of main activity
	 */
	public static MainActivity s_Instance;

	/**
	 * stores the tab host - which is used to dynamically create tabs
	 */
	private TabHost m_tab_host;

	/**
	 * name of tag tab spec
	 */
	public final static String TAG_TAB_SPEC_NAME = "TagStore";

	/**
	 * name of configuration tab spec
	 */
	public final static String CONFIGURATION_TAB_SPEC_NAME = "Configuration";

	/**
	 * name of pending file tab spec
	 */
	public final static String PENDING_FILE_TAB_SPEC_NAME = "Pending";

	/**
	 * stores the tab specs
	 */
	private ArrayList<TabSpec> m_tab_spec_list;

	/**
	 * holds reference to service
	 */
	FileWatchdogService m_service;

	protected void onResume() {

		//
		// call super method
		//
		super.onResume();

		Logger.e("MainActivity::onResume");

		//
		// clear spec list
		//
		m_tab_spec_list.clear();

		//
		// HACK for android 2.2
		// looks like tab host is not updating the last view index when removing
		// all tabs
		//
		m_tab_host.setCurrentTab(0);

		//
		// remove all tabs
		//
		m_tab_host.clearAllTabs();

		//
		// build prime tabs
		//
		buildPrimeTabs();

		//
		// add pending file tab if necessary
		//
		addPendingFileTab();
	}

	/**
	 * returns true if the tab with that name is present
	 * 
	 * @param tab_spec_name
	 *            name of the tab
	 * @return boolean
	 */
	public boolean isTabPresent(String tab_spec_name) {

		for (TabSpec current_spec : m_tab_spec_list) {
			//
			// get tag name
			//
			String tag_name = current_spec.getTag();

			if (tag_name.compareTo(tab_spec_name) == 0) {
				//
				// tag is already present
				//
				return true;
			}

		}

		//
		// tab with that name is not present
		//
		return false;
	}

	/**
	 * removes the tab with the name from the list
	 * 
	 * @param tab_spec_name
	 *            name of tab
	 * @return true if successfull
	 */
	public boolean removeTab(String tab_spec_name) {

		for (TabSpec current_spec : m_tab_spec_list) {
			//
			// get tag name
			//
			String tag_name = current_spec.getTag();

			if (tag_name.compareTo(tab_spec_name) == 0) {
				//
				// found tab
				//
				boolean removed = m_tab_spec_list.remove(current_spec);
				Logger.i("MainActivity::removed tab: " + removed + " name: "
						+ tab_spec_name + " spec: " + current_spec);
				return true;
			}

		}

		//
		// tab with that name is not present
		//
		return false;

	}

	/**
	 * rebuilds the tabs
	 * 
	 * @return
	 */
	public void rebuildTabs() {

		//
		// HACK for android 2.2
		// looks like tab host is not updating the last view index when removing
		// all tabs
		//
		m_tab_host.setCurrentTab(0);

		//
		// remove all tabs
		//
		m_tab_host.clearAllTabs();

		//
		// re-add all tabs
		//
		for (TabSpec current_spec : m_tab_spec_list) {
			//
			// add to tab host
			//
			m_tab_host.addTab(current_spec);
		}
	}

	/**
	 * returns the tab index
	 * 
	 * @param tab_spec_name
	 *            name of the spec
	 * @return int
	 */
	public int getTabIndex(String tab_spec_name) {

		//
		// go through all tabs and find the index
		//
		for (int index = 0; index < m_tab_spec_list.size(); index++) {
			//
			// get current tab spec
			//
			TabSpec current_spec = m_tab_spec_list.get(index);

			if (current_spec.getTag().compareTo(tab_spec_name) == 0) {
				//
				// found index
				//
				return index;

			}
		}

		//
		// error: tab not present
		//
		return -1;
	}

	/**
	 * selects the tab with that index
	 * 
	 * @param tab_index
	 *            index of tab
	 */
	public void selectTabIndex(int tab_index) {

		if (tab_index < 0 || tab_index >= m_tab_spec_list.size()) {

			//
			// invalid parameter
			//
			return;
		}

		//
		// select index
		//
		m_tab_host.setCurrentTab(tab_index);

	}

	/**
	 * adds the specified tab to the list
	 * 
	 * @param tab_name
	 *            name of the tab
	 * @return true if successful
	 */
	public boolean addTab(String tab_name, TabSpec tab_spec) {

		//
		// check if tab is not already present
		//
		if (isTabPresent(tab_name)) {

			//
			// error: same tab already exists
			//
			Logger.e("Error: MainActivity::addTab tab " + tab_name
					+ " already present");
			return false;
		}

		//
		// store in tab list
		//
		m_tab_spec_list.add(tab_spec);

		//
		// done
		//
		return true;
	}

	/**
	 * creates a tab spec with the provided name
	 * 
	 * @param tab_spec_name
	 *            name of tab spec
	 * @return TabSpec
	 */
	public TabSpec createTabSpec(String tab_spec_name) {

		//
		// create tab spec
		//
		return m_tab_host.newTabSpec(tab_spec_name);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		//
		// initialize sub class
		//
		super.onCreate(savedInstanceState);

		//
		// construct tab specs array list
		//
		m_tab_spec_list = new ArrayList<TabSpec>();

		//
		// set layout
		//
		setContentView(R.layout.main);

		//
		// store instance member
		//
		s_Instance = this;

		//
		// get the tab host
		//
		m_tab_host = (TabHost) findViewById(android.R.id.tabhost);

		//
		// check if tab host was found
		//
		if (m_tab_host == null) {

			//
			// failed to get tab host
			//
			Toast toast = Toast.makeText(getApplicationContext(),
					"Failed to initialize UI", Toast.LENGTH_LONG);

			//
			// display error
			//
			toast.show();

			//
			// exit
			//
			finish();

			//
			// done
			//
			return;
		}

		//
		// setup tab host
		//
		m_tab_host.setup();

		//
		// initialize database manager
		//
		DBManager db_man = DBManager.getInstance();
		db_man.initialize(this);

		//
		// build prime tabs
		//
		buildPrimeTabs();

		//
		// add pending file tab if necessary
		//
		addPendingFileTab();

		//
		// launch file watch dog service asynchronously
		//
		startFileWatchdogService();

		//
		// completed
		//
		Logger.i("TagStore: INIT COMPLETE ");

	}

	private void buildPrimeTabs() {

		//
		// get resources
		//
		Resources res = getResources();

		//
		// construct tag tab
		//
		TabSpec tag_tab = m_tab_host.newTabSpec(TAG_TAB_SPEC_NAME);

		//
		// construct new activity which will launch the tab
		//
		Intent tag_intent = new Intent(this, TagActivityGroup.class);

		//
		// set name of tag and icon
		//
		tag_tab.setIndicator(TAG_TAB_SPEC_NAME, res.getDrawable(R.drawable.tag));

		//
		// set content for tag
		//
		tag_tab.setContent(tag_intent);

		//
		// now construct configuration tag
		//
		TabSpec configuration_tab = m_tab_host
				.newTabSpec(CONFIGURATION_TAB_SPEC_NAME);

		//
		// create intent to launch configuration tab
		//
		Intent configuration_intent = new Intent(this,
				ConfigurationActivityGroup.class);

		//
		// put parameters
		//
		configuration_intent.putExtra(ConfigurationActivityGroup.CLASS_NAME,
				"org.me.TagStore.ConfigurationTabActivity");

		//
		// set name and icon
		//
		configuration_tab.setIndicator(CONFIGURATION_TAB_SPEC_NAME,
				res.getDrawable(R.drawable.options_unselected));

		//
		// set content for configuration tab
		//
		configuration_tab.setContent(configuration_intent);

		//
		// now add the tabs to the host
		//
		m_tab_host.addTab(tag_tab);
		m_tab_host.addTab(configuration_tab);

		//
		// store in tab spec list
		//
		m_tab_spec_list.add(tag_tab);
		m_tab_spec_list.add(configuration_tab);

	}

	/**
	 * adds the pending file tab
	 */
	private void addPendingFileTab() {

		//
		// acquire instance of database manager
		//
		DBManager db_man = DBManager.getInstance();

		//
		// get pending files from database
		//
		ArrayList<String> files = db_man.getPendingFiles();

		//
		// are there pending files
		//
		if (files == null || files.size() == 0) {
			//
			// there are no pending files to display
			//
			Logger.i("MainActivitiy::addPendingFileTab no pending files found");

			//
			// done
			//
			return;
		}

		//
		// get resources
		//
		Resources res = getResources();

		//
		// now create the pending file tab
		//
		TabSpec tab_spec = m_tab_host
				.newTabSpec(MainActivity.PENDING_FILE_TAB_SPEC_NAME);

		//
		// create intent to launch configuration tab
		//
		Intent tab_intent = new Intent(this, PendingFileActivityGroup.class);

		//
		// set name and icon
		//
		tab_spec.setIndicator(MainActivity.PENDING_FILE_TAB_SPEC_NAME,
				res.getDrawable(R.drawable.file));

		//
		// set content for configuration tab
		//
		tab_spec.setContent(tab_intent);

		//
		// now add the tab
		//
		m_tab_host.addTab(tab_spec);

		//
		// put tab spec list
		//
		m_tab_spec_list.add(tab_spec);

		//
		// set current tab
		//
		m_tab_host.setCurrentTab(2);
	}

	/**
	 * starts the file watchdog service
	 */
	private void startFileWatchdogService() {

		//
		// create a thread which will start the service
		//
		Thread service_launch_thread = new Thread("ServiceLauncherThread") {

			public void run() {

				//
				// now create intent to launch the file watch dog service
				//
				Intent intent = new Intent(MainActivity.s_Instance,
						FileWatchdogService.class);

				//
				// start service
				//
				startService(intent);

				//
				// bind the service now
				//
				bindService(intent, new MainActivityServiceConnection(),
						Context.BIND_AUTO_CREATE);

				Logger.d("MainActivity::run service started");
			}
		};

		//
		// now start the thread
		//
		service_launch_thread.start();
	}

	private class MainActivityServiceConnection implements ServiceConnection {

		@SuppressWarnings("unchecked")
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			//
			// store service
			//
			m_service = ((FileWatchdogServiceBinder<FileWatchdogService>) service)
					.getService();

			Logger.i("MainActivity::onServiceConnected service " + m_service);

			if (m_service != null) {
				//
				// register our notification handler
				//
				m_service
						.registerExternalNotification(new MainActivityFileSystemObserverNotification());
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

			//
			// should not happen
			//

		}
	};

	private class MainActivityFileSystemObserverNotification implements
			FileSystemObserverNotification {

		@Override
		public void notify(String file_name, NotificationType type) {

			//
			// check notification type
			//
			if (type == NotificationType.FILE_CREATED) {
				//
				// new file arrived, is there already a file queued
				//
				boolean file_tab_pending_present = isTabPresent(MainActivity.PENDING_FILE_TAB_SPEC_NAME);
				if (file_tab_pending_present) {
					//
					// nothing to do
					//
					return;
				}

				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						//
						// add pending file tab if necessary
						//
						addPendingFileTab();

						//
						// rebuild tabs
						//
						rebuildTabs();

						//
						// set current tab
						//
						m_tab_host.setCurrentTab(2);
					}
				});

				//
				// done
				//
				return;
			}
		}
	};

}
