package org.me.TagStore;

import java.util.ArrayList;

import org.me.TagStore.R;
import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.DBManager;
import org.me.TagStore.core.Logger;
import org.me.TagStore.core.MainServiceConnection;
import org.me.TagStore.core.ServiceLaunchRunnable;
import org.me.TagStore.core.SyncFileLog;
import org.me.TagStore.ui.MainPageAdapter;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

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
	
	public void onCreate(Bundle savedInstanceState) {

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);

		//
		// informal debug message
		//
		Logger.i("DatabaseSettingsActivity::onCreate");
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

		Toast toast;
		
		//
		// re-enable reset button
		//
		m_reset_button.setEnabled(true);
		
		if (reset_database) {
			//
			// successfully deleted database
			//
			String text = getActivity().getString(R.string.reset_database);
			toast = Toast.makeText(getActivity(),
					text, Toast.LENGTH_SHORT);
		} else {
			//
			// failed to reset database
			//
			String text = getActivity().getString(R.string.error_reset_database);			
			toast = Toast.makeText(getActivity(),
					text, Toast.LENGTH_SHORT);
		}
		
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

		//
		// display toast
		//
		toast.show();
	}

	/**
	 * clears any active status bar notification when enabled
	 */
	private void clearNotification()
	{
		//
		// get settings
		//
		SharedPreferences settings = getActivity().getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		
		//
		// are notification settings enabled
		//
		boolean enable_notifications = settings.getBoolean(ConfigurationSettings.SHOW_TOOLBAR_NOTIFICATIONS, true);
		
		//
		// check notification settings are enabled
		//
		if (!enable_notifications)
			return;
		
		//
		// get notification service name
		//
		String ns_name = Context.NOTIFICATION_SERVICE;

		//
		// get notification service manager instance
		//
		NotificationManager notification_manager = (NotificationManager) getActivity().getSystemService(ns_name);

		//
		// clear notification
		//
		notification_manager.cancel(ConfigurationSettings.NOTIFICATION_ID);
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
			
			//
			// clear sync log
			//
			//clearSyncLog();
			
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
