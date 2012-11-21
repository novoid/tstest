package org.me.tagstore.core;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * This class analyizes directories and searches for existing files which have
 * not yet been added to the tagstore or are not in the pending file list
 * 
 */
public class DirectoryChangeWorker {

	/**
	 * reference to the database manager
	 */
	private DBManager m_db;

	/**
	 * holds all pending files
	 */
	private ArrayList<String> m_pending_files;

	/**
	 * holds the tagstore files
	 */
	private ArrayList<String> m_files;

	/**
	 * file tag utility
	 */
	private FileTagUtility m_utility;

	/**
	 * file checker
	 */
	private PendingFileChecker m_file_checker;

	/**
	 * initializes the directory change worker
	 */
	public void initializeDirectoryChangeWorker(DBManager db_man,
			FileTagUtility utility, PendingFileChecker file_checker) {

		// init members
		m_db = db_man;
		m_utility = utility;
		m_file_checker = file_checker;

		//
		// init change worker
		//
		initialize();
	}

	/**
	 * initialize the DirectoryChangeWorker object
	 */
	private void initialize() {

		//
		// get pending file checker
		//
		m_file_checker.initializePendingFileChecker(m_db, m_utility);

		//
		// get pending files
		//
		m_pending_files = m_file_checker.getPendingFiles();

		//
		// get tagstore files
		//
		m_files = m_db.getFiles();
		if (m_files == null)
			m_files = new ArrayList<String>();
	}

	/**
	 * returns list of existing files in that directory
	 * 
	 * @param new_directory
	 *            directory whose files should be checked
	 * @return
	 */
	private ArrayList<String> getExistingFilesFromDirectory(String new_directory) {

		//
		// construct file object
		//
		File path = new File(new_directory);

		//
		// get list of existing files
		//
		File[] file_list = path.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return (pathname.isDirectory() == false);
			}
		});

		//
		// construct result list
		//
		ArrayList<String> existing_files = new ArrayList<String>();

		for (File file : file_list) {
			existing_files.add(file.getAbsolutePath());
		}

		//
		// done
		//
		return existing_files;
	}

	/**
	 * returns all new files from a given directory
	 * 
	 * @param new_directory
	 *            path to directory
	 * @return
	 */
	private ArrayList<String> getFilesFromDirectory(String new_directory) {

		//
		// construct file object
		//
		File path = new File(new_directory);

		//
		// parameter check
		//
		if (!path.exists() || !path.isDirectory())
			return null;

		//
		// get list of existing files
		//
		ArrayList<String> existing_files = getExistingFilesFromDirectory(new_directory);

		//
		// remove pending files
		//
		existing_files.removeAll(m_pending_files);

		//
		// remove tagstore files
		//
		existing_files.removeAll(m_files);

		//
		// done
		//
		return existing_files;
	}

	/**
	 * returns all files from that directory
	 * 
	 * @param new_directory
	 *            directory to be checked
	 * @return
	 */
	public ArrayList<String> getNewFilesFromDirectory(String new_directory) {
		return getFilesFromDirectory(new_directory);
	}

	/**
	 * builds a list of new files which exist in the observed directories but
	 * are not in the pending file list / tagstore
	 * 
	 * @return
	 */
	private ArrayList<String> getAllFiles() {

		//
		// get all observed directories
		//
		ArrayList<String> observed_directories = m_db.getDirectories();

		//
		// build result list
		//
		ArrayList<String> result_list = new ArrayList<String>();

		for (String observed_directory : observed_directories) {
			//
			// get existing files
			//
			result_list
					.addAll(getExistingFilesFromDirectory(observed_directory));
		}

		//
		// remove pending files
		//
		result_list.removeAll(m_pending_files);

		//
		// remove tagstore files
		//
		result_list.removeAll(m_files);

		//
		// done
		//
		return result_list;
	}

	/**
	 * returns all new files
	 * 
	 * @return
	 */
	public ArrayList<String> getAllNewFiles() {
		return getAllFiles();
	}
}
