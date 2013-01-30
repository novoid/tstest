package org.me.tagstore.interfaces;

public interface SynchronizationAlgorithmCallback {

	/**
	 * called when the storage provider is about to upload a file
	 * @param file_name file to be uploaded
	 */
	public abstract void onUploadFile(String file_name);
	
	/**
	 * called when the storage provider is about to download a file
	 * @param file_name file to be downloaded
	 */
	public abstract void onDownloadFile(String file_name);
	
	/**
	 * called to display informal updates
	 * @param msg informal messages
	 */
	public abstract void notifyInfo(String msg);
	
	/**
	 * called when an error has happened
	 */
	public abstract void notifyError(String msg);

	/**
	 * called to inform how many files are synchronized
	 */
	public abstract void notifyFileSyncInfo(final int source_files, final int target_files);
	
	/**
	 * called when there is a conflict
	 */
	public abstract void onNotifyConflict();
	
	
}
