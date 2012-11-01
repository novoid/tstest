package org.me.tagstore.core;

/**
 * This class is used to store details of a file, like tags, hash sum, time stamp
 * @author Johannes Anderwald
 *
 */

public class SyncLogEntry {

	/**
	 * stores the file name
	 */
	public String m_file_name;
	
	/**
	 * stores the tags
	 */
	public String m_tags;
	
	/**
	 * stores the hash sum
	 */
	public String m_hash_sum;
	
	/**
	 * stores the time stamp
	 */
	public String m_time_stamp;
	
	/**
	 * constructor of log entry
	 */
	public SyncLogEntry() {
		
		m_file_name = m_hash_sum = m_time_stamp = m_tags = "";
	}
}
