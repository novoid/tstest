package org.me.tagstore.core;

import org.me.tagstore.interfaces.EventDispatcherInterface;

import android.content.Context;

/**
 * This class executes the synchronization task
 * 
 */
public class SyncTask implements Runnable {

	/**
	 * application context
	 */
	private Context m_context;
	private SyncManager m_sync_mgr;
	private EventDispatcherInterface m_event_dispatcher;
	private SyncFileWriter m_sync_writer;
	private DBManager m_db_man;
	private SyncFileLog m_file_log;
	private FileTagUtility m_utility;

	public void initializeSyncTask(Context context, SyncManager sync_mgr,
			SyncFileWriter sync_writer,
			EventDispatcherInterface event_dispatcher, DBManager db_man,
			SyncFileLog file_log, FileTagUtility utility) {

		//
		// init members
		//
		m_context = context;
		m_sync_mgr = sync_mgr;
		m_sync_writer = sync_writer;
		m_event_dispatcher = event_dispatcher;
		m_db_man = db_man;
		m_file_log = file_log;
		m_utility = utility;
	}

	public void run() {

		//
		// instantiate the sync manager
		//
		m_sync_mgr.initializeSyncManager(m_context, m_db_man, m_file_log,
				m_utility);

		int new_files = m_sync_mgr.getNumberOfNewFiles();
		int removed_files = m_sync_mgr.getNumberOfRemovedFiles();
		int changed_files = m_sync_mgr.getNumberOfChangedFiles();

		Logger.i("New     Files: " + new_files);
		Logger.i("Removed Files: " + removed_files);
		Logger.i("Changed files: " + changed_files);
		//
		// now sync the files
		//
		m_sync_mgr.syncRemovedFiles();
		m_sync_mgr.syncNewFiles();
		m_sync_mgr.syncChangedFiles();

		//
		// did any updates happen?
		//
		boolean performed_sync = (new_files != 0) || (removed_files != 0)
				|| (changed_files != 0);

		if (performed_sync) {
			//
			// write entries
			//
			m_sync_writer.writeTagstoreFiles();
		}

		//
		// prepare event args
		//
		Object[] event_args = new Object[] { new_files, removed_files,
				changed_files };

		//
		// signal event
		//
		m_event_dispatcher.signalEvent(
				EventDispatcher.EventId.SYNC_COMPLETE_EVENT, event_args);
	}
}