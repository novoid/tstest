package org.me.TagStore.core;

import java.io.File;
import java.util.ArrayList;

import org.me.TagStore.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

public class DatabaseResetTask implements Runnable {

	/**
	 *  application context
	 */
	private final Context m_context;
	
	/**
	 * stores connection to the service
	 */
	private final MainServiceConnection m_connection;

	
	public DatabaseResetTask(Context context, MainServiceConnection connection) {
		
		//
		// init members
		//
		m_context = context;
		m_connection = connection;
	}

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
		default_storage_path += File.separator + m_context.getString(R.string.storage_directory);			
		
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
		boolean reset_database = db_man.resetDatabase(m_context);
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
			
			//
			// clear current file
			//
			clearCurrentFile();			
		}
		
		//
		// build event args
		//
		Object [] event_args = new Object[]{reset_database};
		
		
		//
		// signal completion of the database reset task
		//
		EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.DATABASE_RESET_EVENT, event_args);
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
	
	/**
	 * clears the current file setting, which is used to determine which file was last tagged / started to be tagged
	 */
	private void clearCurrentFile() {
		
		//
		// get settings
		//
		SharedPreferences settings = m_context.getSharedPreferences(
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
	
	
}