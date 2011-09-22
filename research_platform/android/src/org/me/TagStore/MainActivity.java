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
import android.view.View;
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
		// show pending file tab if necessary
		//
		showPendingFileTab();
	}

	/**
	 * returns true if the tab with that name is visible
	 * 
	 * @param tab_spec_name
	 *            name of the tab
	 * @return boolean
	 */
	public boolean isTabVisible(String tab_spec_name) {

		for (TabSpec current_spec : m_tab_spec_list) {
			//
			// get tag name
			//
			String tag_name = current_spec.getTag();

			if (tag_name.compareTo(tab_spec_name) == 0) {
				
				//
				// tab exists
				//
				int tab_index = m_tab_spec_list.indexOf(current_spec);
				
				
				//
				// get view
				//
				View view = m_tab_host.getTabWidget().getChildAt(tab_index);
				if (view != null && view.getVisibility() != View.GONE)
					return true;
				else
					return false;
			}

		}

		//
		// tab with that name is not present
		//
		return false;
	}

	/**
	 * toggles the visibility of the tab
	 * 
	 * @param tab_spec_name
	 *            name of tab
	 * @param show true if the view should be made active
	 * @return true if successful
	 */
	public boolean showTab(String tab_spec_name, boolean show) {

		for (TabSpec current_spec : m_tab_spec_list) {
			
			//
			// get tag name
			//
			String tag_name = current_spec.getTag();

			if (tag_name.compareTo(tab_spec_name) == 0) {
				
				//
				// found tab
				//
				int tab_index = m_tab_spec_list.indexOf(current_spec);
				
				//
				// get view
				//
				View view = m_tab_host.getTabWidget().getChildAt(tab_index);
				if (view != null)
				{
					if (show)
					{
						//
						// show view
						//
						view.setVisibility(View.VISIBLE);
						
						//
						// display it
						//
						m_tab_host.setCurrentTab(tab_index);
					}
					else
					{
						//
						// hide view
						//
						view.setVisibility(View.GONE);
						
						if (m_tab_host.getCurrentTab() == tab_index)
						{
							//
							// select first tab if the current tab is being removed
							//
							m_tab_host.setCurrentTab(0);
						}
						
						Logger.e("hiding view....");
						
					}
					return true;
				}
			}
		}

		//
		// tab with that name is not present
		//
		return false;

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
		addTagStoreTab();
		addConfigurationTab();
		addPendingFileTab();

		//
		// show pending file tab if necessary
		//
		showPendingFileTab();

		//
		// launch file watch dog service asynchronously
		//
		startFileWatchdogService();

		//
		// completed
		//
		Logger.i("TagStore: INIT COMPLETE ");

	}

	/**
	 * adds the tagstore tab
	 */
	private void addTagStoreTab() {
		
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
		// now add the tab to the host
		//
		m_tab_host.addTab(tag_tab);
		
		//
		// store in tab spec list
		//
		m_tab_spec_list.add(tag_tab);
	}
	
	/**
	 * adds the configuration tab
	 */
	private void addConfigurationTab() {

		//
		// get resources
		//
		Resources res = getResources();

		//
		// get localized 'configuration' name
		//
		String config_name = getApplicationContext().getString(R.string.configuration);
		
		//
		// now construct configuration tag
		//
		TabSpec configuration_tab = m_tab_host
				.newTabSpec(config_name);

		//
		// set name and icon
		//
		configuration_tab.setIndicator(config_name,
				res.getDrawable(R.drawable.options_unselected));		
		
		
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
		// set content for configuration tab
		//
		configuration_tab.setContent(configuration_intent);

		//
		// now add the tab to the host
		//
		m_tab_host.addTab(configuration_tab);

		//
		// store in tab spec list
		//
		m_tab_spec_list.add(configuration_tab);

	}

	/**
	 * adds the pending file tab
	 */
	private void addPendingFileTab() {

		//
		// get resources
		//
		Resources res = getResources();

		//
		// get localized new file string
		//
		String new_file = getApplicationContext().getString(R.string.new_file);
		
		
		//
		// now create the pending file tab
		//
		TabSpec tab_spec = m_tab_host
				.newTabSpec(new_file);

		//
		// create intent to launch configuration tab
		//
		Intent tab_intent = new Intent(this, PendingFileActivityGroup.class);

		//
		// set name and icon
		//
		tab_spec.setIndicator(new_file,
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
	}

	/**
	 * displays the pending file tab if necessary
	 */
	private void showPendingFileTab() {
		
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
			// hide pending file tab
			//
			m_tab_host.getTabWidget().getChildAt(2).setVisibility(View.GONE);
			
			//
			// done
			//
			
			
			return;
		}
		
		//
		// show pending file tab
		//
		m_tab_host.getTabWidget().getChildAt(2).setVisibility(View.VISIBLE);
		
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
				// get localized new file
				//
				String new_file = getApplicationContext().getString(R.string.new_file);
				
				//
				// new file arrived, is there already a file queued
				//
				boolean file_tab_pending_present = isTabVisible(new_file);
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
						// rebuild tabs
						//
						showPendingFileTab();
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
