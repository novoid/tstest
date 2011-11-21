package org.me.TagStore.core;


import java.util.ArrayList;

/**
 * This class enumerates all present files in the tagstore and writes them to the sync file log
 * @author Johannes Anderwald
 *
 */
public class SyncFileWriter {

	/**
	 * instance of database manager
	 */
	private final DBManager m_db;
	
	/**
	 * stores all sync entries
	 */
	private final ArrayList<SyncLogEntry> m_entries;
	
	/**
	 * constructor of class SyncManager
	 */
	public SyncFileWriter() { 
		
		//
		// acquire database manager
		//
		m_db = DBManager.getInstance();
		
		//
		// construct entries
		//
		m_entries = new ArrayList<SyncLogEntry>();
	}

	
	/**
	 * converts a array list of tags into a string where each tag is seperated by a comma
	 * @param tags to be converted
	 * @return string
	 */
	private String convertToString(ArrayList<String> tags) {
		
		StringBuilder builder = new StringBuilder();
		
		for (String tag : tags)
		{
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
	private void constructSyncLogEntries() {
		
		//
		// clear log entries
		//
		m_entries.clear();
		
		
		//
		// get all files
		//
		ArrayList<String> files = m_db.getFiles();
		
		//
		// enumerate each files' tag and build sync log entry
		//
		for (String file_name : files)
		{
			
			SyncLogEntry new_entry = new SyncLogEntry();
			
			//
			// get tags, timestamp and hashsum
			//
			new_entry.m_tags = convertToString(m_db.getAssociatedTags(file_name));
			new_entry.m_time_stamp = m_db.getFileDate(file_name);
			new_entry.m_hash_sum = m_db.getHashsum(file_name);
			new_entry.m_file_name = file_name;
			
			//
			// add entry
			//
			m_entries.add(new_entry);
		}
	}

	/**
	 * writes the sync log entries
	 */
	private void writeSyncLogEntries() {
		
		//
		// instantiate sync file log
		//
		SyncFileLog file_log = SyncFileLog.getInstance();
		
		//
		// write log entries
		//
		file_log.writeLogEntries(m_entries);
	}
	
	/**
	 * writes all files currently available into the tag store sync log file
	 */
	public void writeTagstoreFiles() {
		
		Logger.e("SyncFileWriter::writeTagstoreFiles()");
		
		//
		// now update those entries from the database
		//
		constructSyncLogEntries();
		
		//
		// write sync log entries
		//
		writeSyncLogEntries();
	}
}
