package org.me.TagStore.core;

import java.io.File;
import java.util.ArrayList;

import android.os.Environment;

/**
 * This class queries for pending files and removes non-existant files
 * @author Johannes Anderwald
 *
 */
public class PendingFileChecker {

	/**
	 * reference to the database manager
	 */
	private final DBManager m_db; 
	
	/**
	 * result list
	 */
	private final ArrayList<String> m_pending_files;
	
	/**
	 * constructor of class PendingFileChecker
	 */
	public PendingFileChecker() {
		
		m_db = DBManager.getInstance();
		m_pending_files = new ArrayList<String>();
		
		initialize();
	}
	
	/**
	 * returns true when external storage disk is accessible
	 * @return
	 */
	private boolean isStorageAccesssible() {
		
		//
		// get current storage state
		//
		String storage_state = Environment.getExternalStorageState();
		if (!storage_state.equals(Environment.MEDIA_MOUNTED) && !storage_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY))
		{
			//
			// the media is currently not accessible
			//
			return false;
		}
		
		//
		// disk is accessible
		//
		return true;
	}
	
	/**
	 * initializes the pending file list
	 * @return
	 */
	private void initialize(){

		//
		// checks if there external storage is available
		//
		if (!isStorageAccesssible())
			return;
		
		//
		// get pending files
		//
		ArrayList<String> pending_files = m_db.getPendingFiles();
		if (pending_files == null || pending_files.size() == 0)
			return;
		
		//
		// enumerate all files and add them to pending file list if the exist otherwise remove them from the list
		//
		for(String file_path : pending_files)
		{
			if (isFileExisting(file_path))
			{
				m_pending_files.add(file_path);
				Logger.e("pending: " + file_path);
			}
			else
				removePendingFile(file_path);
		}
	}
	
	/**
	 * returns true when it has pending files
	 * @return
	 */
	public boolean hasPendingFiles() {
		if (m_pending_files.size() > 0)
			return true;
		else
			return false;
	}
	
	/**
	 * returns the pending files
	 * @return
	 */
	public ArrayList<String> getPendingFiles() {
		
		//
		// returns the pending files
		//
		return m_pending_files;
	}
	
	
	/**
	 * checks if a file still exists
	 * @param file_path file path to be checked
	 * @return true if exists
	 */
	private boolean isFileExisting(String file_path) {
		File file = new File(file_path);
		return file.exists();
	}
	
	/**
	 * removes a file from the pending file list
	 * @param file_path
	 */
	private void removePendingFile(String file_path) {
		FileTagUtility.removePendingFile(file_path);
	}
}
