package org.me.TagStore.core;

import java.io.File;
import java.util.ArrayList;

/**
 * This class is used to write new tagged files to a log.
 * 
 * @author Johannes Anderwald
 * 
 */
public class SyncFileLog {

	/**
	 * file log handle
	 */
	private FileLog m_FileLog;
	
	/**
	 * constructor of class SyncFileLog
	 */
	private SyncFileLog() {
		
		// construct file log
		m_FileLog = new FileLog();
	}
	
	
	/**
	 * returns instance of SyncFileLog
	 * @return SyncFileLog
	 */
	public static SyncFileLog getInstance() {

		//
		// create new instance
		//
		SyncFileLog instance = new SyncFileLog();

		//
		// done
		//
		return instance;
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
	 * This function creates an empty log file. All previous entries are truncated
	 */
	public void clearLogEntries() {
		
		// let FileLog handle it
		m_FileLog.clearLogEntries(getStoreConfigPath());
		
	}
	
	
	/**
	 * reads all sync log entries into the provided arraylist
	 * @param entries populated list of sync log entries after reading the log
	 */
	public void readLogEntries(ArrayList<SyncLogEntry> entries) {
	
		// get external storage path
		String item_path_prefix = android.os.Environment.getExternalStorageDirectory()
		.getAbsolutePath();
		
		
		// let FileLog handle it
		m_FileLog.readLogEntries(getStoreConfigPath(), item_path_prefix, entries);
		
	}
	
	/**
	 * writes the entries of the array into the log
	 * @param entries to be written
	 * @return true on success
	 */
	public boolean writeLogEntries(ArrayList<SyncLogEntry> entries) {
		
		// get external storage path
		String item_path_prefix = android.os.Environment.getExternalStorageDirectory()
		.getAbsolutePath();		
		
		// let FileLog handle it
		return m_FileLog.writeLogEntries(getStoreConfigPath(), item_path_prefix, entries);
	}
}
