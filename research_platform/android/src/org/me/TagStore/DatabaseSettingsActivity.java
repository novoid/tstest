package org.me.TagStore;

import java.io.File;
import java.util.ArrayList;

import org.me.TagStore.R;
import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.DBManager;
import org.me.TagStore.core.Logger;
import org.me.TagStore.core.MainServiceConnection;
import org.me.TagStore.core.ServiceLaunchRunnable;
import org.me.TagStore.core.SyncFileLog;
import org.me.TagStore.ui.MainPageAdapter;
import org.me.TagStore.ui.StatusBarNotification;
import org.me.TagStore.ui.ToastManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;


public class DatabaseSettingsActivity extends Fragment {

	/**
	 * stores the result of database reset thread
	 */
	private boolean m_reset_database;
	
	/**
	 * stores the reset button
	 */
	private Button m_reset_button;
	
	/**
	 * stores the connection handle to the watch dog service
	 */
	private MainServiceConnection m_connection;
	
	/**
	 * status bar notification
	 */
	private StatusBarNotification m_status_bar; 
	
	public void onCreate(Bundle savedInstanceState) {

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);

		//
		// informal debug message
		//
		Logger.i("DatabaseSettingsActivity::onCreate");
		
		//
		// build status bar
		//
		m_status_bar = new StatusBarNotification(getActivity().getApplicationContext());
	}

	public void onPause() {

		//
		// call super method
		//
		super.onPause();

		//
		// cancel any on-going toast when switching the view
		//
		ToastManager.getInstance().cancelToast();
	}	
	
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
			
		 //
		 // construct layout
		 //
		 View view = inflater.inflate(R.layout.database_settings, null);

		//
		// get reset button
		//
		m_reset_button = (Button) view.findViewById(R.id.button_reset);
		if (m_reset_button != null) {

			//
			// add click listener
			//
			m_reset_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					//
					// disable reset button
					//
					m_reset_button.setEnabled(false);
					
					//
					// create database reset worker
					//
					Thread database_worker = new Thread(new DatabaseResetTask());
					
					//
					// start the thread
					//
					database_worker.start();
				}
			});
		}

		//
		// build main service connection
		//
		m_connection = new MainServiceConnection();
		
		//
		// construct service launcher
		//
		ServiceLaunchRunnable launcher = new ServiceLaunchRunnable(getActivity().getApplicationContext(), m_connection);
		
		//
		// create a thread which will start the service
		//
		Thread launcher_thread = new Thread(launcher);
		
		//
		// now start the thread
		//
		launcher_thread.start();		
		
		//
		// done
		//
		return view;
		
	}

	/**
	 * callback which is invoked when database worker has been reset
	 * @param reset_database if true the database has been reset
	 */
	protected void resetDatabaseCallback(boolean reset_database) {
		
		//
		// store result
		//
		m_reset_database = reset_database;
		
		//
		// run on ui thread
		//
		getActivity().runOnUiThread(new Runnable() {
		    public void run() {
		    	updateUIDatabaseCallback(m_reset_database);
		    }
		});
		
	}
	
	
	/**
	 * updates the ui after the database has been reset
	 * @param reset_database if true the database has successfully been reset
	 */
	protected void updateUIDatabaseCallback(boolean reset_database) {

		//
		// re-enable reset button
		//
		m_reset_button.setEnabled(true);
		
		//
		// get main page adapter
		//
		MainPageAdapter adapter = MainPageAdapter.getInstance();

		//
		// is the add file tag fragment present
		//
		if (adapter.isAddFileTagFragmentActive())
		{
			//
			// hide that fragment as the database is now empty
			//
			adapter.removeAddFileTagFragment();
				
			//
			// clear notification settings
			//
			clearNotification();
		}

		//
		// clear file settings
		//
		clearCurrentFile();		

		if (reset_database) {
			//
			// successfully deleted database
			//
			ToastManager.getInstance().displayToastWithString(R.string.reset_database);
		} else {
			//
			// failed to reset database
			//
			ToastManager.getInstance().displayToastWithString(R.string.error_reset_database);			
		}
	}

	/**
	 * clears any active status bar notification when enabled
	 */
	private void clearNotification()
	{
		//
		// are notification settings enabled
		//
		boolean enabled_status_bar = m_status_bar.isStatusBarNotificationEnabled();
		if(enabled_status_bar)
		{
			//
			// cancel notification
			//
			m_status_bar.removeStatusBarNotification();
		}
	}
	
	/**
	 * clears the current file setting, which is used to determine which file was last tagged / started to be tagged
	 */
	private void clearCurrentFile() {
		
		//
		// get settings
		//
		SharedPreferences settings = getActivity().getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		//
		// get settings editor
		//
		SharedPreferences.Editor editor = settings.edit();

		//
		// set current file
		//
		editor.putString(ConfigurationSettings.CURRENT_FILE_TO_TAG, "");

		//
		// set current tag line
		//
		editor.putString(ConfigurationSettings.CURRENT_TAG_LINE, "");
		
		//
		// and commit the changes
		//
		editor.commit();
	}
	
	private class DatabaseResetTask implements Runnable {

		@Override
		public void run() {

			//
			// acquire database manager instance
			//
			DBManager db_man = DBManager.getInstance();

			//
			// get default storage directory
			//
			String default_storage_path = Environment.getExternalStorageDirectory().getAbsolutePath();
			default_storage_path += File.separator + ConfigurationSettings.TAGSTORE_DIRECTORY;
			default_storage_path += File.separator + getString(R.string.storage_directory);			
			
			//
			// get observed directories
			//
			ArrayList<String> observed_directory_list = db_man.getDirectories();
			if (observed_directory_list != null) {
				
				//
				// unregister all directories
				//
				for(String path : observed_directory_list)
				{
					if (!m_connection.unregisterDirectory(path))
					{
						Logger.e("DatabaseResetTask failed to unregister path: " + path);
					}
				}
			}
			
			//
			// reset the database
			//
			boolean reset_database = db_man.resetDatabase(getActivity());
			if (reset_database)
			{
				//
				// add default storage directory
				//
				db_man.addDirectory(default_storage_path);
				
				//
				// re-register path with watch dog service
				//
				m_connection.registerDirectory(default_storage_path);
				
				//
				// clear sync log
				//
				clearSyncLog();
			}
			
			//
			// invoke callback
			//
			resetDatabaseCallback(reset_database);

		}
		
		/**
		 * removes all file entries from the log file
		 */
		private void clearSyncLog() {
			
			//
			// construct sync log
			//
			SyncFileLog file_log = new SyncFileLog();
			
			//
			// clear the log
			//
			file_log.clearLogEntries();
		}
		
	}
	
	
}
