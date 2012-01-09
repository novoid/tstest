package org.me.TagStore.core;

import java.io.File;
import java.util.ArrayList;

import org.me.TagStore.core.DBManager;

/**
 * checks all tagstore files if they still exist. If they no longer exist, then the file is removed from the tagstore. After all files
 * have been checked, it is removed from storage task
 * This task is run everytime the application starts up or is restarted
 * @author Johannes Anderwald
 *
 */
public class TagStoreFileChecker implements org.me.TagStore.core.StorageTimerTask.TimerTaskCallback {

	
	/**
	 * constructor of TagStoreFileChecker
	 */
	public TagStoreFileChecker() {
	}
	
	/**
	 * called when the disk is available
	 */
	public void diskAvailable() {
		
		Logger.i("TagStoreFileChecker::diskAvailable");
		
		//
		// acquire database manager
		//
		DBManager db_man = DBManager.getInstance();
		
		//
		// get all files
		//
		ArrayList<String> files = db_man.getFiles();
		if (files == null)
			return;
		
		//
		// check if all files still exist
		//
		for(String file_path : files)
		{
			File file = new File(file_path);
			if (!file.isFile() || !file.exists())
			{
				//
				// remove file from database
				//
				FileTagUtility.removeFile(file_path, false);
			}
		}
		
		//
		// now unregister ourselves
		//
		StorageTimerTask task = StorageTimerTask.acquireInstance();
		Logger.i("TagStoreFileChecker::diskAvailable remove");		
		task.removeCallback(this);
	}

	public void diskNotAvailable() {
		
	}
}
