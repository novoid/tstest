package org.me.tagstore.core;

import java.util.ArrayList;

/**
 * This class enumerates all present files in the tagstore and writes them to
 * the tagstore store file
 * 
 */
public class SyncFileWriter {

	/**
	 * database manager instance
	 */
	private DBManager m_db_man = null;

	/**
	 * sync file log instance
	 */
	private SyncFileLog m_file_log = null;

	/**
	 * sets the database manager and file log. Used for unit testing
	 * 
	 * @param db_man
	 *            database manager
	 * @param file_log
	 *            file log
	 */
	public void setDBManagerAndFileLog(DBManager db_man, SyncFileLog file_log) {
		m_db_man = db_man;
		m_file_log = file_log;
	}

	/**
	 * converts a array list of tags into a string where each tag is separated
	 * by a comma
	 * 
	 * @param tags
	 *            to be converted
	 * @return string
	 */
	private String convertToString(ArrayList<String> tags) {

		StringBuilder builder = new StringBuilder();

		for (String tag : tags) {
			if (builder.length() == 0)
				builder.append(tag);
			else
				builder.append(", " + tag);
		}

		return builder.toString();
	}

	/**
	 * fills the sync log entry array
	 */
	private void constructSyncLogEntries(ArrayList<SyncLogEntry> entries) {

		if (m_db_man == null) {

			//
			// get instance of database manager
			//
			m_db_man = DBManager.getInstance();
		}

		//
		// get all files
		//
		ArrayList<String> files = m_db_man.getFiles();

		//
		// enumerate each files' tag and build sync log entry
		//
		for (String file_name : files) {

			SyncLogEntry new_entry = new SyncLogEntry();

			//
			// get tags, timestamp and hashsum
			//
			new_entry.m_tags = convertToString(m_db_man
					.getAssociatedTags(file_name));
			new_entry.m_time_stamp = m_db_man.getFileDate(file_name);
			new_entry.m_hash_sum = m_db_man.getHashsum(file_name);
			new_entry.m_file_name = file_name;

			//
			// add entry
			//
			entries.add(new_entry);
		}
	}

	/**
	 * writes the sync log entries
	 */
	private boolean writeSyncLogEntries(ArrayList<SyncLogEntry> entries) {

		if (m_file_log == null) {
			//
			// instantiate sync file log
			//
			m_file_log = new SyncFileLog();
		}

		//
		// write log entries
		//
		return m_file_log.writeLogEntries(entries);
	}

	/**
	 * writes all files currently available into the tag store sync log file
	 */
	public boolean writeTagstoreFiles() {

		Logger.e("SyncFileWriter::writeTagstoreFiles()");

		//
		// construct log entry list
		//
		ArrayList<SyncLogEntry> entries = new ArrayList<SyncLogEntry>();

		//
		// now update those entries from the database
		//
		constructSyncLogEntries(entries);

		//
		// write sync log entries
		//
		return writeSyncLogEntries(entries);
	}
}
