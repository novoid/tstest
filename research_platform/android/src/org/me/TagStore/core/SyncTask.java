package org.me.TagStore.core;

import android.content.Context;

/**
 * This class executes the synchronization task
 * @author Johannes Anderwald
 *
 */
public class SyncTask implements Runnable {
	
	/**
	 * application context
	 */
	private final Context m_context;

	/**
	 * constructor of class SyncTask
	 * @param context application context
	 */
	public SyncTask(Context context) {
		
		//
		// init members
		//
		m_context = context;
	}
	
	
	@Override
	public void run() {

		//
		// instantiate the sync manager
		//
		SyncManager sync_mgr = new SyncManager(m_context);
		
		int new_files = sync_mgr.getNumberOfNewFiles();
		int removed_files = sync_mgr.getNumberOfRemovedFiles();
		int changed_files = sync_mgr.getNumberOfChangedFiles();
		
		
		
		Logger.i("New     Files: " + new_files);
		Logger.i("Removed Files: " + removed_files);
		Logger.i("Changed files: " + changed_files);
		//
		// now sync the files
		//
		sync_mgr.syncRemovedFiles();
		sync_mgr.syncNewFiles();
		sync_mgr.syncChangedFiles();	
		
		//
		// did any updates happen?
		//
		boolean performed_sync = (new_files != 0) || (removed_files != 0) || (changed_files != 0);

		if (performed_sync)
		{
			//
			// instantiate the sync file write
			//
			SyncFileWriter file_writer = new SyncFileWriter();
			
			//
			// write entries
			//
			file_writer.writeTagstoreFiles();	
		}
		
		//
		// prepare event args
		//
		Object [] event_args = new Object[]{new_files, removed_files, changed_files};
		
		//
		// signal event
		//
		EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_COMPLETE_EVENT, event_args);
	}
}