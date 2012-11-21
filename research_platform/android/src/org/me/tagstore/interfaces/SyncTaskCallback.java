package org.me.tagstore.interfaces;

/**
 * This interface is used to signal the completion of the sync task
 *
 */
public interface SyncTaskCallback {

	/**
	 * callback which gets the details of the sync task
	 * @param new_files number of new files added during sync
	 * @param old_files number of deleted files during sync
	 * @param modified_files number of updated files during sync
	 */
	public abstract void onSyncTaskCompletion(final int new_files, final int old_files, final int modified_files);
}
