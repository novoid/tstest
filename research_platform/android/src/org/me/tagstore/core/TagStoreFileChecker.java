package org.me.tagstore.core;

import java.io.File;
import java.util.ArrayList;

import junit.framework.Assert;

/**
 * checks all tagstore files if they still exist. If they no longer exist, then
 * the file is removed from the tagstore. After all files have been checked, it
 * is removed from storage task This task is run everytime the application
 * starts up or is restarted
 * 
 */
public class TagStoreFileChecker implements
		org.me.tagstore.core.StorageTimerTask.TimerTaskCallback {

	/**
	 * database manager
	 */
	private DBManager m_db_man = null;
	
	/**
	 * file log
	 */
	private SyncFileWriter m_file_writer = null;

	/**
	 * sets handle for database manager
	 * 
	 * @param db_man
	 */
	public void initializeTagStoreFileChecker(DBManager db_man, SyncFileWriter file_writer) {

		// store handle
		m_db_man = db_man;
		m_file_writer  = file_writer;
	}

	/**
	 * called when the disk is available
	 */
	public boolean diskAvailable() {

		Logger.i("TagStoreFileChecker::diskAvailable");

		// sanity check
		Assert.assertNotNull(m_db_man);

		//
		// get all files
		//
		ArrayList<String> files = m_db_man.getFiles();
		if (files == null)
			return true;

		boolean modified = false;
		
		//
		// check if all files still exist
		//
		for (String file_path : files) {
			File file = new File(file_path);
			if (!file.isFile() || !file.exists()) {
				//
				// remove file from database
				//
				modified = m_db_man.removeFile(file_path);
			}
		}
		
		if (modified) {
			
			// refresh database
			m_file_writer.writeTagstoreFiles();
		}
		
		
		//
		// now unregister ourselves
		//
		return false;
	}

	public boolean diskNotAvailable() {

		//
		// continue scheduling untill first successfull run
		//
		return true;
	}
}
