package org.me.tagstore.core;

/**
 * This class is used to store details of a file, like tags, hash sum, time
 * stamp
 * 
 */

public class SyncLogEntry {

	/**
	 * stores the file name
	 */
	public String m_file_name = "";

	/**
	 * stores the tags
	 */
	public String m_tags = "";

	/**
	 * stores the hash sum
	 */
	public String m_hash_sum = "";

	/**
	 * stores the time stamp
	 */
	public String m_time_stamp = "";

	/**
	 * constructor of log entry
	 */
	public SyncLogEntry() {
	}

	public boolean equals(SyncLogEntry entry) {

		Logger.e("entry: " + entry.m_file_name + " tags: " + entry.m_tags
				+ " hash: " + entry.m_hash_sum + " timestamp:"
				+ entry.m_time_stamp);
		Logger.e("this: " + this.m_file_name + " tags: " + this.m_tags
				+ " hash: " + this.m_hash_sum + " timestamp:"
				+ this.m_time_stamp);
		return this.m_file_name.equals(entry.m_file_name)
				&& this.m_tags.equals(entry.m_tags)
				&& this.m_hash_sum.equals(entry.m_hash_sum)
				&& this.m_time_stamp.equals(entry.m_time_stamp);
	}

}
