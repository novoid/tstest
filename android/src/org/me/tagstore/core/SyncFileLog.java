package org.me.tagstore.core;

import java.io.File;
import java.util.ArrayList;

/**
 * This class is used to write new tagged files to a log.
 * 
 * 
 */
public class SyncFileLog {

	/**
	 * file log handle
	 */
	private FileLog m_file_log = null;

	/**
	 * sets the file log
	 * 
	 * @param file_log
	 */
	public void setFileLog(FileLog file_log) {
		m_file_log = file_log;

	}

	private String getStoreConfigPath() {

		//
		// get path to be observed (external disk)
		//
		String path = android.os.Environment.getExternalStorageDirectory()
				.getAbsolutePath();

		//
		// append seperator
		//
		path += File.separator + ConfigurationSettings.TAGSTORE_DIRECTORY;
		path += File.separator + ConfigurationSettings.CONFIGURATION_DIRECTORY;
		path += File.separator + ConfigurationSettings.LOG_FILENAME;
		return path;
	}

	/**
	 * This function creates an empty log file. All previous entries are
	 * truncated
	 */
	public void clearLogEntries() {

		if (m_file_log == null) {
			// create file log
			m_file_log = new FileLog();
		}

		// let FileLog handle it
		m_file_log.clearLogEntries(getStoreConfigPath());

	}

	/**
	 * reads all sync log entries into the provided arraylist
	 * 
	 * @param entries
	 *            populated list of sync log entries after reading the log
	 */
	public boolean readLogEntries(ArrayList<SyncLogEntry> entries) {

		if (m_file_log == null) {
			// create file log
			m_file_log = new FileLog();
		}

		// get external storage path
		String item_path_prefix = android.os.Environment
				.getExternalStorageDirectory().getAbsolutePath();

		// let FileLog handle it
		return m_file_log.readLogEntries(getStoreConfigPath(),
				item_path_prefix, entries, true);

	}

	/**
	 * writes the entries of the array into the log
	 * 
	 * @param entries
	 *            to be written
	 * @return true on success
	 */
	public boolean writeLogEntries(ArrayList<SyncLogEntry> entries) {

		if (m_file_log == null) {
			// create file log
			m_file_log = new FileLog();
		}

		// get external storage path
		String item_path_prefix = android.os.Environment
				.getExternalStorageDirectory().getAbsolutePath();

		// let FileLog handle it
		return m_file_log.writeLogEntries(getStoreConfigPath(),
				item_path_prefix, entries, true);
	}
}
