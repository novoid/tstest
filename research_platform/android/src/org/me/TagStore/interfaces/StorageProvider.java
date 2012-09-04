package org.me.TagStore.interfaces;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;

public interface StorageProvider {

	/**
	 * returns true when the user has been logged in
	 * @return boolean
	 */
	public abstract boolean isLoggedIn();

	/**
	 * grabs a list of files from a directory
	 * @param directory_path path of the directory
	 * @param hash of the directory
	 * @return
	 */
	public abstract ArrayList<String> getFiles(String directory_path,
			String hash);

	/**
	 * returns true when it is a file
	 * @param filepath path to the file
	 * @return boolean
	 */
	public abstract boolean isFile(String filepath);

	/** 
	 * uploads a file to given target directory
	 * @param filename file to be uploaded
	 * @param target_directory target directory inside the app folder
	 * @return true on success
	 */
	public abstract boolean uploadFile(String filename,
			String target_directory);

	/**
	 * downloads a file to a given path
	 * @param newfile path to new file
	 * @param source_path path to source directory
	 * @return true on success
	 */
	public abstract boolean downloadFile(String newfile, String source_path);

	/**
	 * closes the session and logs out
	 */
	public abstract void unlink(Context context);
	
	/**
	 * retrieves the file size
	 * @param filepath path to file
	 */
	public abstract long getFileSize(String filepath);

	/**
	 * deletes the specified file
	 * @param target_lock_file
	 */
	public abstract void deleteFile(String target_lock_file);
	
	/**
	 * returns the current file revision
	 * @param file_path file path
	 * @return revision when available, otherwise null
	 */
	public abstract String getFileRevision(String file_path);
	
	/**
	 * returns the file modification date
	 */
	public abstract Date getFileModificationDate(String file_path);
	
}