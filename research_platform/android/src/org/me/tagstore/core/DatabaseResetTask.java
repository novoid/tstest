package org.me.tagstore.core;

import java.io.File;
import java.util.ArrayList;

import org.me.tagstore.R;
import org.me.tagstore.interfaces.EventDispatcherInterface;
import org.me.tagstore.interfaces.WatchdogServiceConnection;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

public class DatabaseResetTask implements Runnable {

	/**
	 * application context
	 */
	private Context m_context;

	/**
	 * stores connection to the service
	 */
	private WatchdogServiceConnection m_connection;

	/**
	 * event dispatcher
	 */
	private EventDispatcherInterface m_event_dispatcher;

	/**
	 * db manager instance
	 */
	private DBManager m_db_man;

	private SyncFileLog m_file_log;

	/**
	 * initializes the reset task object
	 * 
	 * @param context
	 *            application context
	 * @param connection
	 *            connection
	 * @param event_dispatcher
	 *            event dispatcher
	 */
	public void initializeDatabaseResetTask(Context context,
			WatchdogServiceConnection connection,
			EventDispatcherInterface event_dispatcher, DBManager db_man, SyncFileLog file_log) {

		//
		// init members
		//
		m_context = context;
		m_connection = connection;
		m_event_dispatcher = event_dispatcher;
		m_db_man = db_man;
		m_file_log = file_log;
	}

	public void run() {

		//
		// get default storage directory
		//
		String default_storage_path = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		default_storage_path += File.separator
				+ ConfigurationSettings.TAGSTORE_DIRECTORY;
		default_storage_path += File.separator
				+ m_context.getString(R.string.storage_directory);

		//
		// get observed directories
		//
		ArrayList<String> observed_directory_list = m_db_man.getDirectories();
		if (observed_directory_list != null) {

			//
			// unregister all directories
			//
			for (String path : observed_directory_list) {
				if (!m_connection.unregisterDirectory(path)) {
					Logger.e("DatabaseResetTask failed to unregister path: "
							+ path);
				}
			}
		}

		//
		// reset the database
		//
		boolean reset_database = m_db_man.resetDatabase(m_context);
		Logger.e("resetDatabase: " + reset_database);
		if (reset_database) {
			//
			// add default storage directory
			//
			m_db_man.addDirectory(default_storage_path);

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
		Object[] event_args = new Object[] { reset_database };

		//
		// signal completion of the database reset task
		//
		m_event_dispatcher.signalEvent(
				EventDispatcher.EventId.DATABASE_RESET_EVENT, event_args);
	}

	/**
	 * removes all file entries from the log file
	 */
	private void clearSyncLog() {

		if (m_file_log != null) {
		
			//
			// clear the log
			//
			m_file_log.clearLogEntries();
		}
	}

	/**
	 * clears the current file setting, which is used to determine which file
	 * was last tagged / started to be tagged
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
		// clear all preferences
		//
		editor.clear();

		//
		// and commit the changes
		//
		editor.commit();
	}

}