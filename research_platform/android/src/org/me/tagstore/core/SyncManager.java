package org.me.tagstore.core;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class is responsible for syncing the tagstore database with those entries of the sync file
 * @author Johannes Anderwald
 *
 */
public class SyncManager {

	/**
	 * reference to database manager
	 */
	private final DBManager m_db;
	
	/**
	 * stores all present files in the tagstore
	 */
	private ArrayList<String> m_files;
	
	/**
	 * stores all file entries in the sync log
	 */
	private final ArrayList<String> m_sync_files;

	/**
	 * stores a list of files which should be added
	 */
	private ArrayList<String> m_new_files;
	
	/**
	 * stores a list of files which should be removed from the tag store
	 */
	private ArrayList<String> m_removed_files;
	
	/**
	 * stores a list of files whose tags got changed during the last sync
	 */
	private ArrayList<String> m_changed_files;
	
	/**
	 * stores the mapping file name -> log entry
	 */
	private HashMap<String, SyncLogEntry> m_map;
	
	/**
	 * stores the context
	 */
	private Context m_context;
	
	
	public SyncManager(Context context)
	{
		//
		// get instance of database manager
		//
		m_db = DBManager.getInstance();
	
		//
		// get all present files in the tagstore
		//
		m_files = m_db.getFiles();
		if (m_files == null)
			m_files = new ArrayList<String>();
		
		//
		// construct sync files
		//
		m_sync_files = new ArrayList<String>();
		m_map = new HashMap<String, SyncLogEntry>();
		m_context = context;
		
		//
		// initialize rest of object
		//
		initialize();
	}
	
	/**
	 * initializes the sync manager
	 */
	private void initialize() {
		
		//
		// instantiate the sync file log
		//
		SyncFileLog file_log = SyncFileLog.getInstance();
		
		//
		// get all entries
		//
		ArrayList<SyncLogEntry> sync_entries = new ArrayList<SyncLogEntry>();
		file_log.readLogEntries(sync_entries);
		
		//
		// add to sync entries
		//
		for(SyncLogEntry entry : sync_entries)
		{
			m_sync_files.add(entry.m_file_name);
			m_map.put(entry.m_file_name, entry);
		}
		
		//
		// get new / changed / removed files
		//
		m_changed_files = getChangedFiles();
		m_new_files = getNewFiles();
		m_removed_files = getRemovedFiles();
	}
	
	/**
	 * returns a list of files which were removed during the sync process
	 * @return
	 */
	private ArrayList<String> getRemovedFiles() {
		
		//
		// copy file list
		//
		ArrayList<String> files = new ArrayList<String>(m_files);
		
		//
		// remove all entries which are present in the sync log
		//
		files.removeAll(m_sync_files);
		
		//
		// done
		//
		return files;
	}
	
	/**
	 * gets the number of removed files
	 * @return
	 */
	public int getNumberOfRemovedFiles() {
		return m_removed_files.size();
	}
	
	/**
	 * returns a list of files which were added during the sync process
	 * @return
	 */
	private ArrayList<String> getNewFiles() {
		
		//
		// copy file list
		//
		ArrayList<String> files = new ArrayList<String>(m_sync_files);
		
		//
		// remove all entries from the tagstore from the sync log
		//
		files.removeAll(m_files);
		
		//
		// done
		//
		return files;
	}

	/**
	 * returns a list of files whose tags have been changed during the last sync
	 * @return
	 */
	private ArrayList<String> getChangedFiles() {
		
		//
		// copy file list
		//
		ArrayList<String> files = new ArrayList<String>(m_files);
		
		//
		// remove all new files from the collection
		//
		files.retainAll(m_sync_files);
		
		//
		// construct empty result list
		//
		ArrayList<String> result_list = new ArrayList<String>();
		
		//
		// add all files whose tags got changed
		//
		for(String file : files)
		{
			if (!areTagsEqual(file))
			{
				//
				// add them
				//
				result_list.add(file);
			}
		}
		
		//
		// done
		//
		return result_list;
	}
	
	
	/**
	 * returns true when the tags of the sync entry are contained in the tag store and vice versa
	 * @param file_name
	 * @return
	 */
	private boolean areTagsEqual(String file_name) {
		
		//
		// get all tags of the file
		//
		ArrayList<String> tags = m_db.getAssociatedTags(file_name);
		
		//
		// get all tags from the sync log entry
		//
		SyncLogEntry entry = m_map.get(file_name);
		Set<String> sync_tags = FileTagUtility.splitTagText(entry.m_tags);
		
		if (tags.containsAll(sync_tags) && sync_tags.containsAll(tags))
			return true;
		else
			return false;
	}
	
	/**
	 * returns the number of files whose tags changed during the last sync
	 * @return
	 */
	public int getNumberOfChangedFiles() {
		return m_changed_files.size();
	}
	
	
	/**
	 * returns the number of removed files during the sync process
	 * @return
	 */
	public int getNumberOfNewFiles() {
		return m_new_files.size();
	}
	
	/**
	 * adds all new files to the store
	 */
	public void syncNewFiles() {
		
		for(String file_name : m_new_files)
		{
			//
			// get sync entries
			//
			SyncLogEntry entry = m_map.get(file_name);
			
			//
			// check if file exists
			//
			File file = new File(file_name);
			if (!file.exists())
			{
				Logger.e("file not existing: " + file.getAbsolutePath());
				continue;
			}
			
			if (entry != null)
			{
				//
				// add file
				//
				FileTagUtility.tagFile(file_name, entry.m_tags, false);
			}
		}
		
		if (m_new_files.size() > 0)
			updateSyncDate();
	}

	/**
	 * removes all removed files from the tag store
	 */
	public void syncRemovedFiles() {
		
		for(String file_name : m_removed_files) {
			
			//
			// get sync entries
			//
			SyncLogEntry entry = m_map.get(file_name);
			Logger.e("removed file: " + file_name);			
			if (entry != null)
			{
				//
				// acquire database manager
				//
				DBManager db_man = DBManager.getInstance();				
				
				//
				// remove file from store
				//
				db_man.removeFile(file_name);
			}
		}
		
		if (m_removed_files.size() > 0)
			updateSyncDate();

	}
	
	/**
	 * syncs all files whose tags got changed during last sync
	 */
	public void syncChangedFiles() {
		
		for(String file_name : m_changed_files)
		{
			//
			// get sync entries
			//
			SyncLogEntry entry = m_map.get(file_name);
			
			//
			// check if file exists
			//
			File file = new File(file_name);
			if (!file.exists())
				continue;
			
			if (entry != null)
			{
				//
				// retag file
				//
				FileTagUtility.retagFile(file_name, entry.m_tags, false);
			}
		}
		
		if (m_changed_files.size() > 0)
			updateSyncDate();
		
	}
	
	/**
	 * updates the sync date
	 */
	private void updateSyncDate() {
		
		if (m_context == null)
			return;
		
		//
		// construct date format
		//
		SimpleDateFormat date_format = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		//
		// format date
		//
		String date = date_format.format(new Date());
		
		//
		// get settings
		//
		SharedPreferences settings = m_context.getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		//
		// get settings editor
		//
		SharedPreferences.Editor editor = settings.edit();
			
		//
		// store synchronization date
		//
		editor.putString(ConfigurationSettings.SYNCHRONIZATION_HISTORY, date);
			
			
		//
		// and commit the changes
		//
		editor.commit();
		
	}
	
	
}
