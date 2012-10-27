package org.me.TagStore.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TimeZone;

import org.me.TagStore.R;
import org.me.TagStore.interfaces.StorageProvider;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

public class SynchronizationAlgorithmBackend {

    /**
     * storage provider interface	
     */
	private StorageProvider m_provider;
	
	/**
	 * application context
	 */
	private Context m_context;

	/**
	 * target tagstore path
	 */
	private String m_target_store_path;

	/*
	 * source tagstore store.tgs file path
	 */
	private String m_source_store_tag_path;
	
	/**
	 * temp tagstore store.tgs file path
	 */
	private String m_target_store_tag_path;
	
	/**
	 * target log entries
	 */
	private ArrayList<SyncLogEntry> m_target_entries;
	
	/**
	 * source log entries
	 */
	private ArrayList<SyncLogEntry> m_source_entries;
	
	/**
	 * target sync log entries
	 */
	private ArrayList<SyncLogEntry> m_target_sync_entries;
	
	/**
	 * source sync log entries
	 */
	private ArrayList<SyncLogEntry> m_source_sync_entries;
	
	/**
	 * target storage path
	 */
	private String m_target_storage_path;

	/**
	 * source storage path
	 */
	private String m_source_storage_path;

	/**
	 * stores target store sync tag temporary file path
	 */
	private String m_target_store_sync_tag_path;

	/**
	 * stores the source store sync tag file path
	 */
	private String m_source_store_sync_tag_path;
	
	/**
	 * file cache
	 */
	private HashMap<String, String> m_file_cache;
	
	/**
	 * conflict list
	 */
	private ArrayList<ConflictData> m_conflict_list;
	
	/**
	 * constructor of class SynchronizationAlgorithmBackend
	 * @param provider storage provider
	 */
	public SynchronizationAlgorithmBackend(StorageProvider provider, Context context) {
		
		// store provider 
		m_provider = provider;
		
		// store context
		m_context = context;
		
		// initialize basic properties
		initialize();
	}
	
	private void initialize() {
		
		// acquire shared settings
		SharedPreferences settings = m_context.getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		
		// read last tagstore name
		m_target_store_path = settings.getString(ConfigurationSettings.CURRENT_SYNCHRONIZATION_TAGSTORE, "");
		
		// init paths
		initPaths();
	}
	
	private File createTempFile(String name, String extension) {
		
		File temp_file = null;
		try
		{
			// construct temp path
			temp_file = File.createTempFile("store", ".tgs");
		}
		catch(IOException exc)
		{
			Logger.e("IOException while creating temporary file");
			return null;
		}
		
		return temp_file;
	}
	
	
	private void initPaths() {
		
		// source tag path
		m_source_store_tag_path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + 
			File.separator + ConfigurationSettings.TAGSTORE_DIRECTORY +		
            File.separator + ConfigurationSettings.CONFIGURATION_DIRECTORY + 
            File.separator + ConfigurationSettings.LOG_FILENAME;
		
		// tagstore storage paths
		m_target_storage_path = m_target_store_path + File.separator + m_context.getString(R.string.storage_directory);
		m_source_storage_path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + 
								File.separator + ConfigurationSettings.TAGSTORE_DIRECTORY + 
								File.separator + m_context.getString(R.string.storage_directory);	
	
		
		m_source_store_sync_tag_path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + 
		File.separator + ConfigurationSettings.TAGSTORE_DIRECTORY +		
        File.separator + ConfigurationSettings.CONFIGURATION_DIRECTORY + 
        File.separator + m_target_store_path.substring(1) + ConfigurationSettings.DEFAULT_SYNC_FILE_NAME;		
				
		
		
	}
	
	
	/**
	 * downloads the target store tag file to a temporary file
	 * @return true on success
	 */
	private boolean downloadStoreTagFile() {
		
		// construct target path
		String target_path = m_target_store_path + File.separator + ConfigurationSettings.CONFIGURATION_DIRECTORY +
							 File.separator + ConfigurationSettings.LOG_FILENAME;
		
		// create temp file
		File temp_file = createTempFile("store", ".tgs");
		if (temp_file == null)
			return false;
		
		
		// perform callback
		EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_DOWNLOAD_EVENT, new Object[]{target_path});
		
		// now download the file
		boolean result = m_provider.downloadFile(temp_file.getAbsolutePath(), target_path);

		// store target file path
		m_target_store_tag_path = temp_file.getAbsolutePath();	
		
		// done
		return result;
	}
	
	/**
	 * downloads the store sync tag file
	 * @return
	 */
	private boolean downloadStoreSyncTagFile() {
		
		// construct target path
		String target_path = m_target_store_path + File.separator + ConfigurationSettings.CONFIGURATION_DIRECTORY +
							 File.separator + ConfigurationSettings.DEFAULT_ANDROID_SYNC_FILE_NAME;
	
		
		// create temp file
		File temp_file = createTempFile("tagstoresync", ".tgs");
		if (temp_file == null)
			return false;		
		
		// perform callback
		EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_DOWNLOAD_EVENT, new Object[]{target_path});

		// store target sync file
		m_target_store_sync_tag_path = temp_file.getAbsolutePath();
		
		// check if file exists
		if (m_provider.getFileSize(target_path) <= 0)
		{
			// file not exists yet
			// create empty store file
			// get external storage path
			
			FileLog temp_log = new FileLog();
			temp_log.writeLogEntries(m_target_store_sync_tag_path, m_target_storage_path, new ArrayList<SyncLogEntry>());
			return true;
		}
			
		// download file
		boolean result = m_provider.downloadFile(m_target_store_sync_tag_path, target_path);
		
		return result;
	}
	
	/**
	 * reads the file logs
	 */
	private void readFileLogs() {
		
		// construct file logs
		FileLog target_log = new FileLog();
		FileLog source_log = new FileLog();
		FileLog target_sync_log = new FileLog();
		FileLog source_sync_log = new FileLog();
		
		// construct sync log entries
		m_target_entries = new ArrayList<SyncLogEntry>();
		m_source_entries = new ArrayList<SyncLogEntry>();
		m_target_sync_entries = new ArrayList<SyncLogEntry>();
		m_source_sync_entries = new ArrayList<SyncLogEntry>();
		
		// inform activity
		String info_msg = m_context.getString(R.string.analyize_store_tag_file);
		EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_INFO_EVENT, new Object[]{info_msg});
		
		// get external storage path
		String source_item_path_prefix = android.os.Environment.getExternalStorageDirectory()
		.getAbsolutePath();
				
		// source item prefix
		String target_item_path_prefix = m_target_storage_path;
		
		// target log entries
		target_log.readLogEntries(m_target_store_tag_path, target_item_path_prefix, m_target_entries);
		
		// source log entries
		source_log.readLogEntries(m_source_store_tag_path, source_item_path_prefix, m_source_entries);
		
		// target sync log entries
		target_sync_log.readLogEntries(m_target_store_sync_tag_path, target_item_path_prefix, m_target_sync_entries);
		
		// source sync log entries
		source_sync_log.readLogEntries(m_source_store_sync_tag_path, source_item_path_prefix, m_source_sync_entries);
		
		// notify sync info
		EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_FILE_INFO_EVENT, new Object[]{m_source_entries.size(), m_target_entries.size()});
	}
	
	private boolean downloadFile(String source_path, String target_path) {

		// inform activity
		EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_DOWNLOAD_EVENT, new Object[]{source_path});
		
		// download file
		boolean result = m_provider.downloadFile(target_path, source_path);
		return result;
	}
	
	/**
	 * uploads a file
	 * @param file_name file name without path
	 * @param target_path target path and file name
	 * @return true on success
	 */
	private boolean uploadFile(String source_path, String target_path) {

		// inform activity
		EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_UPLOAD_EVENT, new Object[]{source_path});
		
		// upload file
		boolean result = m_provider.uploadFile(source_path, target_path);
		return result;
	}
	
	public void synchronize() {
		
		// create lock files
		boolean result = createLockFiles();
		if (!result)
		{
			// failed to create the lock files
			String error_msg = m_context.getString(R.string.sync_active);
        	EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_ERROR_EVENT, new Object[]{error_msg});
        	return;			
		}
		
		// first download store file
		result = downloadStoreTagFile();
        if (!result)
        {
        	// failed to download store files
        	String error_msg = m_context.getString(R.string.error_failed_connect);
        	EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_ERROR_EVENT, new Object[]{error_msg});
        	return;
        }
		
        // now download the store sync file
        result = downloadStoreSyncTagFile();
        if (!result)
        {
        	// failed to download store sync file
        	String error_msg = m_context.getString(R.string.error_failed_connect);
        	EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_ERROR_EVENT, new Object[]{error_msg});
        	return;        	
        }
        
        // now initialize the store files
        readFileLogs();
		
        // dump file logs
        //dumpFileLogs();
        
        // construct conflict list
        m_conflict_list = new ArrayList<ConflictData>();
        
        // construct file cache list
        m_file_cache = new HashMap<String, String>();
        
       
        // now synchronize the stores
        synchronizeStores();
        
        // dump new state
        Logger.i("[SYNC] done");
        
        if (m_conflict_list.isEmpty()) {
        	
        	// finish synchronization
        	finishSynchronization();
        }
        else
        {
        	// dispatch conflict event
        	EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_CONFLICT_EVENT, null);
        }
        
        
        
	}
	
	/**
	 * finishes the synchronization
	 */
	public void finishSynchronization() {

		
        // updateStores
        updateStores();
        
        // import new changes
		Thread sync_thread = new Thread(new SyncTask(m_context));
		sync_thread.start();
		
		// wait for the sync task to finish
		try {
			sync_thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		
		// delete lock files
		deleteLockFiles();
	
		// clear file cache
		deleteFileCache();
	}

	/**
	 * returns true when there are conflicts remaining
	 * @return
	 */
	public boolean hasConflicts() {
		return m_conflict_list.isEmpty() == false;
	}
	
	/**
	 * returns the next available conflict
	 * @return ConflictData
	 */
	public ConflictData nextConflict() {
		
		if (m_conflict_list.isEmpty())
			return null;
		
		// remove item
		return m_conflict_list.remove(0);
	}
	
	public void handleConflict(ConflictData data) {
		
		// remove similar conflicts
		for(ConflictData item : m_conflict_list)
		{
			if (item.m_source_file.equals(data.m_target_file) &&
				item.m_target_file.equals(data.m_source_file))
			{
				// remove it
				m_conflict_list.remove(item);
			}
		}

		// get entries
		SyncLogEntry entry, target_entry;
		entry = getEntry(data.m_source_file, isLocalPath(data.m_source_file));
		target_entry = getEntry(data.m_source_file, isLocalPath(data.m_target_file));

		if (isLocalPath(data.m_target_file))
		{
			// sync file
			syncNewTags(entry, target_entry, false, m_target_sync_entries);
			
			// sync file
			syncFile(entry, target_entry);
		}
		else
		{
			// sync file
			syncNewTags(entry, target_entry, true, m_source_sync_entries);
			
			// sync file
			syncFile(entry, target_entry);			
		}
		
		if (!m_conflict_list.isEmpty())
		{
			// fire conflict event
			EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_CONFLICT_EVENT, null);
		}
		
	}
	
	/**
	 * returns true when the path is a local file path
	 * @param path to be checked
	 * @return boolean
	 */
	private boolean isLocalPath(String path) {

		// target storage path
		return !path.startsWith(m_target_store_path);
	}
	
	
	private void deleteFileCache() {
		
		// get keys
		Set<String> keys = m_file_cache.keySet();
		
		// delete all temporary files
		for(String key : keys)
		{
			// get temp file name
			String temp_file = m_file_cache.get(key);
			
			// delete file obj
			File temp_file_obj = new File(temp_file);
			temp_file_obj.delete();
		}
	}
	
	
	
	/**
	 * deletes the lock files
	 */
	private void deleteLockFiles() {
		
		// source lock file
		String source_lock_file = m_source_storage_path + File.separator + ConfigurationSettings.TAGSTORE_LOCK_FILE_NAME;

		// file obj
		File source_lock = new File(source_lock_file);
		
		// delete
		source_lock.delete();

		// target lock file
		String target_lock_file = m_target_storage_path + File.separator + ConfigurationSettings.TAGSTORE_LOCK_FILE_NAME;
		
		// delete file
		m_provider.deleteFile(target_lock_file);
	}
	
	/**
	 * creates the lock files
	 * @return
	 */
	private boolean createLockFiles() {

		FileOutputStream out = null;
		boolean ret = false;
		try {

			// create remote lock file
			String target_lock_file = m_target_storage_path + File.separator + ConfigurationSettings.TAGSTORE_LOCK_FILE_NAME;

			// does the remote file already exist
			if (m_provider.getFileSize(target_lock_file) > 0)
			{
				// sync is active
				return false;
			}
			
			// source lock file
			String source_lock_file = m_source_storage_path + File.separator + ConfigurationSettings.TAGSTORE_LOCK_FILE_NAME;

			// check if local lock file exists
			File source_lock = new File(source_lock_file);
			if (source_lock.exists())
			{
				// source is locked
				return false;
			}
			
			// open file stream
			out = new FileOutputStream(new File(source_lock_file));
			
			// write file
			String temp = "Android";
			out.write(temp.getBytes());
			out.flush();

			
			ret = uploadFile(source_lock_file, target_lock_file);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		catch(IOException e) {
			
		}
		finally
		{
			if (out != null)
			{
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return ret;
	}
	
	private void updateStores() {
		
		// construct file logs
		FileLog target_log = new FileLog();
		FileLog source_log = new FileLog();
		FileLog target_sync_log = new FileLog();
		FileLog source_sync_log = new FileLog();
		
		// inform activity
		String info_msg = m_context.getString(R.string.updating_stores);
		EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_INFO_EVENT, new Object[]{info_msg});
		
		// source item prefix
		String target_item_path_prefix = m_target_storage_path;
		
		// get external storage path
		String source_item_path_prefix = android.os.Environment.getExternalStorageDirectory()
		.getAbsolutePath();		
		
        // dump state
		//dumpFileLogs();
		
		// target log entries
		target_log.writeLogEntries(m_target_store_tag_path, target_item_path_prefix, m_target_entries);
		
		// source log entries
		source_log.writeLogEntries(m_source_store_tag_path, source_item_path_prefix, m_source_entries);
		
		// target sync log entries
		target_sync_log.writeLogEntries(m_target_store_sync_tag_path, target_item_path_prefix, m_target_sync_entries);
		
		// source sync log entries
		source_sync_log.writeLogEntries(m_source_store_sync_tag_path, source_item_path_prefix, m_source_sync_entries);
		
		
		// init paths
		String target_sync_path = m_target_store_path + File.separator + ConfigurationSettings.CONFIGURATION_DIRECTORY +
							 File.separator + ConfigurationSettings.DEFAULT_ANDROID_SYNC_FILE_NAME;
		String target_path = m_target_store_path + File.separator + ConfigurationSettings.CONFIGURATION_DIRECTORY +
							 File.separator + ConfigurationSettings.LOG_FILENAME;		
		
		// now upload files
		uploadFile(m_target_store_tag_path, target_path);
		uploadFile(m_target_store_sync_tag_path, target_sync_path);
		

	}
	
	private void dumpFileLogs() {
		
        Logger.i("source entries");
        dumpFileLog(m_source_entries);
        
        Logger.i("target_entries");
        dumpFileLog(m_target_entries);
        
        Logger.i("source sync_entries");
        dumpFileLog(m_source_sync_entries);
        
        Logger.i("target_sync_entries");
        dumpFileLog(m_target_sync_entries);
	}
	
	/**
	 * dumps all sync log entries from an array
	 * @param entries array list
	 */
	private void dumpFileLog(ArrayList<SyncLogEntry> entries) {
		
		for(SyncLogEntry entry : entries)
		{
			Logger.i("Entry: " + entry.m_file_name + " tags: " + entry.m_tags);
			
			String filepath = m_target_storage_path + File.separator + new File(entry.m_file_name).getName();
			Logger.i("FileSize: " + m_provider.getFileSize(filepath));
			
		}
	}
	
	/**
	 * synchronizes the stores
	 */
	private void synchronizeStores() {
		
		// first synchronize remote store
		synchronizeStore(m_target_entries, m_source_entries, m_source_sync_entries);
		
		// synchronize local store
		synchronizeStore(m_source_entries, m_target_entries, m_target_sync_entries);
	
	}
	
	
	/**
	 * synchronizes the give source list with target store
	 * @param source_list source list
	 * @param target_list target list
	 * @param source_sync_list source sync list
	 * @param target_sync_list target sync list
	 */
	private void synchronizeStore(ArrayList<SyncLogEntry> source_list,
								  ArrayList<SyncLogEntry> target_list,
								  ArrayList<SyncLogEntry> target_sync_list) {
		
		for(SyncLogEntry entry : source_list) {
			
			synchronizeLogEntry(entry, target_list, target_sync_list);
		}
		
		
	}
	
	/**
	 * synchronizes a file log entry
	 * @param entry entry of list
	 * @param source_sync_entries source sync list
	 * @param target_entries target list
	 * @param target_sync_entries target sync list
	 */
	private void synchronizeLogEntry(SyncLogEntry entry, 
			                         ArrayList<SyncLogEntry> target_entries, 
			                         ArrayList<SyncLogEntry> target_sync_entries) {
		
		
		SyncLogEntry target_entry;
		
		// extract file name
		String file_name = getFilenameFromEntry(entry);
		
		// get target entry
		target_entry = findEntry(file_name, target_entries);
		if (target_entry != null) {
			
			// sync existing entry
			synchronizeExistingEntry(entry, target_entry, target_sync_entries);
		}
		else
		{
			// synchronize new entry
			synchronizeNewEntry(entry, target_entries, target_sync_entries);
		}
	}
	
	private Date getModificationDate(String file_path, boolean is_local) {
		
		if (is_local)
		{
			// construct source file
			File source_file = new File(file_path);
			if (!source_file.exists())
			{
				// no such file
				return null;
			}

			// acquire source time modified
			return new Date(source_file.lastModified());
		}
		else
		{
			// get modification date
			return m_provider.getFileModificationDate(file_path);
		}
	}
	
	/**
	 * returns a sync date
	 * @param entry to look for sync date
	 * @param target_sync_entries array list of sync date
	 * @return 
	 */
	private Date getSyncDateFromEntry(SyncLogEntry entry, ArrayList<SyncLogEntry> target_sync_entries) {
		
		// check for a sync date
		SyncLogEntry sync_entry = findEntry(getFilenameFromEntry(entry), target_sync_entries);
		if (sync_entry == null)
		{
			// no sync date found
			return null;
		}
		
		// time format
		String local_time = TimeFormatUtility.convertStringToLocalTime(sync_entry.m_time_stamp);
			
		// get date object
		Logger.i("Local_time: " + local_time + " utc: " + sync_entry.m_time_stamp);
				
		Date sync_date = TimeFormatUtility.parseDate(local_time, TimeZone.getDefault());
		return sync_date;
	}
	
	
	private void synchronizeExistingEntry(SyncLogEntry entry, SyncLogEntry target_entry, ArrayList<SyncLogEntry> target_sync_entries) {
		
		// check if files are equal
		boolean files_equal = isFileEqual(entry.m_file_name, target_entry.m_file_name, isLocalEntry(entry), target_sync_entries);
		
		if (files_equal)
		{
			// sync new tags
			syncNewTags(entry, target_entry, isLocalEntry(entry), target_sync_entries);

			String target_rev = m_provider.getFileRevision(isLocalEntry(entry) ? target_entry.m_file_name : entry.m_file_name);
			if (target_rev != null)
			{
				// store target revision
				DBManager.getInstance().addSyncFileLog(m_target_store_path.substring(1), 
						isLocalEntry(entry) ? target_entry.m_file_name : entry.m_file_name, 
						target_rev, TimeFormatUtility.getCurrentTimeInUTC(), "NOHASHSUM");
			}			
			return;
		}
		
		// check for a sync date
		Date sync_date = getSyncDateFromEntry(entry, target_sync_entries);
		if (sync_date == null)
		{
			// conflict
			Logger.e("[CONFLICT] create conflict file " + entry.m_file_name);
			
			// add conflict
			addConflict(entry.m_file_name, target_entry.m_file_name, ConflictType.CREATE_CONFLICT_TYPE);
			return;
		}
		
		// get modification date from android store
		Date source_date = getModificationDate(entry.m_file_name, isLocalEntry(entry));
		if (source_date.compareTo(sync_date) <= 0) {
			
			Logger.i("[SYNC] syncing source file " + entry.m_file_name);
			
			// sync new entries
			syncNewTags(entry, target_entry, isLocalEntry(entry), target_sync_entries);
			
			// sync file
			syncFile(entry, target_entry);
			return;
		}
		
		// get modification date from remote store
		Date target_date = getModificationDate(target_entry.m_file_name, isLocalEntry(target_entry));
		if (target_date.compareTo(sync_date) <= 0) {
			
			Logger.i("[SYNC] syncing target file " + entry.m_file_name);			
			
			// sync new entries
			syncNewTags(entry, target_entry, isLocalEntry(entry), target_sync_entries);
			
			// sync file
			syncFile(target_entry, entry);
			return;
		}
		
		// both files modified or semantic conflict
		Logger.e("[SYNC] write conflict sync date: " + sync_date + " source_file: " + source_date + " target date: " + target_date);

		// add conflict
		addConflict(entry.m_file_name, target_entry.m_file_name, ConflictType.WRITE_CONFLICT_TYPE);		
	}
	
	private void updateSyncLogEntryTimeStamp(SyncLogEntry entry) {
		
		if (isLocalEntry(entry)) {
			
			// update sync entry
			SyncLogEntry sync_entry = findEntry(getFilenameFromEntry(entry), m_source_sync_entries); 
			if (sync_entry != null) {
				sync_entry.m_time_stamp =  TimeFormatUtility.getCurrentTimeInUTC();
			}
		}
		else
		{
			// update sync entry
			SyncLogEntry sync_entry = findEntry(getFilenameFromEntry(entry), m_target_sync_entries); 
			if (sync_entry != null) {
				sync_entry.m_time_stamp =  TimeFormatUtility.getCurrentTimeInUTC();
			}			
		}
	}
	
	
	private void syncFile(SyncLogEntry target_entry, SyncLogEntry source_entry) {
		
		String target_rev = null;
		String remote_path = "";
		
		if (isLocalEntry(target_entry))
		{
			// get file cache entry
			File cache_entry = getCacheFile(source_entry.m_file_name);
			if (cache_entry == null)
				return;
			
			// update file
			IOUtils.copyFile(cache_entry, new File(target_entry.m_file_name));
			
			// get target rev
			target_rev = m_provider.getFileRevision(source_entry.m_file_name);
			remote_path = source_entry.m_file_name;
			
			// update sync time
			updateSyncLogEntryTimeStamp(target_entry);
			updateSyncLogEntryTimeStamp(source_entry);
			
		}
		else
		{
			// upload file
			uploadFile(source_entry.m_file_name, target_entry.m_file_name);
			
			// get target rev
			target_rev = m_provider.getFileRevision(target_entry.m_file_name);
			remote_path = target_entry.m_file_name;
		}
		
		if (target_rev != null)
		{
			// store target revision
			DBManager.getInstance().addSyncFileLog(m_target_store_path.substring(1), remote_path, 
					target_rev, TimeFormatUtility.getCurrentTimeInUTC(), "NOHASHSUM");
		}
		
		// update sync time
		updateSyncLogEntryTimeStamp(target_entry);
		updateSyncLogEntryTimeStamp(source_entry);		
		
	}
	
	/**
	 * returns a file from the file cache
	 * @param target_path path of the target file
	 * @return File object
	 */
	private File getCacheFile(String target_path) {
		
		// check if the file is already present in the file cache
		if (m_file_cache.containsKey(target_path)) {
			return new File(m_file_cache.get(target_path));
		}
		
		// okay file not yet present, download it
		EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_DOWNLOAD_EVENT, new Object[]{target_path});
		
		// construct temp file
		File temp_file = getTempFile(target_path);
		
		// download new file
		boolean result = downloadFile(target_path, temp_file.getAbsolutePath());		
		if (!result)
		{
			// failed to download
			String msg = m_context.getString(R.string.error_download_file) + target_path;
			EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_ERROR_EVENT, new Object[]{msg});
			return null;
		}
	
		// store in file cache
		m_file_cache.put(target_path, temp_file.getAbsolutePath());		
		return temp_file;
	}
	
	
	private void syncNewTags(SyncLogEntry entry, SyncLogEntry target_entry, boolean source_local, ArrayList<SyncLogEntry> target_sync_entries) {
		
		// source tags
		Set<String> source_tags = FileTagUtility.splitTagText(entry.m_tags);

		// target tags
		Set<String> target_tags = FileTagUtility.splitTagText(target_entry.m_tags);
		
		// get new tags
		source_tags.addAll(target_tags);

		// update target entry
		target_entry.m_tags = convertToString(source_tags);
		
		// check for a sync entry
		SyncLogEntry sync_entry = findEntry(getFilenameFromEntry(entry), target_sync_entries);
		if (sync_entry == null)
		{
			// no sync entry, construct new entry
			sync_entry = replicateNewEntry(target_entry);
			
			// add to target sync list
			target_sync_entries.add(sync_entry);
		}

		// store sync date
		sync_entry.m_time_stamp = TimeFormatUtility.getCurrentTimeInUTC();
	}
	
	/**
	 * converts a array list of tags into a string where each tag is seperated by a comma
	 * @param tags to be converted
	 * @return string
	 */
	private String convertToString(Set<String> tags) {
		
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
	 * determines if both files are equal
	 * @param source_path source path
	 * @param target_path target path
	 * @param source_local if source_path is a local file
	 * @param target_sync_entries 
	 * @param entry 
	 * @return true when equal
	 */
	private boolean isFileEqual(String source_path, String target_path, boolean source_local, ArrayList<SyncLogEntry> target_sync_entries) {
		
		String remote_file;
		File target_file_obj;
		File source_file_obj;
		if (source_local) {
			
			// source file
			source_file_obj = new File(source_path);
			
			// compare file sizes
			long target_size = m_provider.getFileSize(target_path);
			long source_size = source_file_obj.length();
			
			if (target_size != source_size)
			{
				// files are not equal
				return false;
			}
			
			// remote file
			remote_file = target_path;
			
		}
		else
		{
			// source obj
			source_file_obj = new File(target_path);
			
			// compare file sizes
			long source_size = m_provider.getFileSize(source_path);
			long target_size = source_file_obj.length();
			
			if (target_size != source_size)
			{
				// files are not equal
				return false;
			}
			
			// remote file
			remote_file = source_path;
		}

		// files have equal size - for an rev file entry
		String file_rev = m_provider.getFileRevision(remote_file);
		if (file_rev != null)
		{
			// provider supports revision
			// get sync revision
			String sync_rev = DBManager.getInstance().getSyncFileRev(m_target_store_path.substring(1), remote_file);
			if (sync_rev != null) {
				
				Logger.i("source file: " + source_path + " rev: " + sync_rev);
				Logger.i("target file: " + remote_file + " rev:" + file_rev);
				if (file_rev.equals(sync_rev))
				{
					// remote file was not modified
					// check for a sync date
					Date sync_date = getSyncDateFromEntry(getEntry(remote_file, false), target_sync_entries);
					if (sync_date != null)
					{
						// get modification date from android store
						Date source_date = getModificationDate(source_file_obj.getAbsolutePath(), true);
						Logger.i("SyncDate: " + sync_date);
						Logger.i("SrcDate: " + source_date);
						Logger.i("Compare: " + (source_date.compareTo(sync_date) <= 0));
						return (source_date.compareTo(sync_date) <= 0);					
					}
					else
					{
						Logger.i("No sync date found for entry " + remote_file);
					}
				}
			}
			
		}
		
		// get target file obj
		target_file_obj = getCacheFile(remote_file);
		if (target_file_obj == null)
			return false;
		
		// compare files
		String source_hashsum = FileHashsumGenerator.generateFileHashsum(m_context, source_file_obj.getAbsolutePath());
		String target_hashsum = FileHashsumGenerator.generateFileHashsum(m_context, target_file_obj.getAbsolutePath());
		
		// compare results
		return source_hashsum.equals(target_hashsum);
	}
	

	
	private File getTempFile(String file_path) {
		
		// construct file obj
		File file = new File(file_path);
		
		try
		{
			// get grab new file
			File new_file = File.createTempFile(file.getName(), null);
			return new_file;
		}
		catch(IOException exc)
		{
			
		}
		
		return null;
		
	}
	
	private void synchronizeNewEntry(SyncLogEntry source_entry, 
			                         ArrayList<SyncLogEntry> target_entries, 
			                         ArrayList<SyncLogEntry> target_sync_entries) {
		
		// get source file name
		String source_file_name = getFilenameFromEntry(source_entry);
		
		// was the entry already synchronized once
		SyncLogEntry target_sync_entry = findEntry(source_file_name, target_sync_entries);
		if (target_sync_entry != null)
		{
			Logger.i("[SKIP] File " + source_file_name + " was already synchronized once");
			return;
		}
		
		// target revision
		String target_rev = "";
		String remote_path;
		
		if (isLocalEntry(source_entry)) {
			
			// build target path
			String target_path = m_target_storage_path + File.separator + source_file_name;
			
			if (m_provider.getFileSize(target_path) < 0)
			{
				// there is no such file in the target store's storage directory
				// signal uploading
				EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_UPLOAD_EVENT, new Object[]{source_entry.m_file_name});
			
				// upload new file
				boolean result = uploadFile(source_entry.m_file_name, target_path);
				if (!result)
				{
					// failed to upload file
					String msg = m_context.getString(R.string.error_upload_file) + source_entry.m_file_name;
					EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_ERROR_EVENT, new Object[] {msg});
					return;
				}
			}
			else if (!isFileEqual(source_entry.m_file_name, target_path, true, target_sync_entries))
			{
				Logger.i("[CONFLICT] files " + source_entry.m_file_name + " target: " + target_path + " not equal");
				
				// add conflict
				addConflict(source_entry.m_file_name, target_path, ConflictType.CREATE_CONFLICT_TYPE);		
				return;
			}
			
			// obtain file revision
			target_rev = m_provider.getFileRevision(target_path);
			remote_path = target_path;
		}
		else
		{
			// build target path
			String target_path = m_source_storage_path + File.separator + source_file_name;
			File target_file = new File(m_source_storage_path + File.separator + source_file_name);
			
			if (!target_file.exists())
			{
				// there is no such file in the source store's storage directory
				// signal uploading
				EventDispatcher.getInstance().signalEvent(EventDispatcher.EventId.SYNC_DOWNLOAD_EVENT, new Object[]{source_entry.m_file_name});
			
				File cache_file = getCacheFile(source_entry.m_file_name);
				if (cache_file != null)
				{
					// copy file
					IOUtils.copyFile(cache_file, new File(target_path));
				}
				else
				{
					// failed to download
					return;
				}
			}
			else if (!isFileEqual(source_entry.m_file_name, target_path, false, target_sync_entries))
			{
				Logger.i("[CONFLICT] files " + source_entry.m_file_name + " target: " + target_path + " not equal");

				// add to conflict list				
				addConflict(source_entry.m_file_name, target_path, ConflictType.CREATE_CONFLICT_TYPE);
				return;
			}			
			
			// obtain file revision
			target_rev = m_provider.getFileRevision(source_entry.m_file_name);		
			remote_path = source_entry.m_file_name;
		}
		
		// construct replication entry for the log
		SyncLogEntry new_entry = replicateNewEntry(source_entry);
		
		// add new entry
		target_entries.add(new_entry);

		// add to sync
		target_sync_entries.add(new_entry);
		
		if (target_rev != null)
		{
			// store target revision
			DBManager.getInstance().addSyncFileLog(m_target_store_path.substring(1), remote_path, 
					target_rev, new_entry.m_time_stamp, new_entry.m_hash_sum);
		}
	}

	private SyncLogEntry replicateNewEntry(SyncLogEntry entry) {
		
		// construct new entry
		SyncLogEntry new_entry = new SyncLogEntry();
		
		// init entry
		new_entry.m_hash_sum = entry.m_hash_sum;
		new_entry.m_tags = entry.m_tags;
		
		// get source name
		String source_file_name = getFilenameFromEntry(entry);
		
		if (isLocalEntry(entry))
		{
			// construct file name
			new_entry.m_file_name = m_target_storage_path + File.separator + source_file_name;
		}
		else
		{
			// construct local file name
			new_entry.m_file_name = m_source_storage_path + File.separator + source_file_name;
		}
		
		Logger.e("newFile: " + new_entry.m_file_name);
		
		// adjust timestamp
		new_entry.m_time_stamp = TimeFormatUtility.getCurrentTimeInUTC();
		
		// done
		return new_entry;
	}
	
	
	
	/**
	 * finds a sync log entry with the given file name
	 * @param file_name file name to be searched
	 * @param array sync log entry array
	 * @return matching sync log entry
	 */
	public SyncLogEntry findEntry(String file_name, ArrayList<SyncLogEntry> array) {
		
		for(SyncLogEntry entry : array) {
			
			// extract name
			String current_file_name = getFilenameFromEntry(entry);
			
			// compare for equality
			if (current_file_name.equals(file_name))
			{
				// found it
				return entry;
			}
		}
		
		// no such entry
		return null;
	}
	
	/**
	 * extracts the file name
	 * @param entry containing the file name
	 * @return String
	 */
	private String getFilenameFromEntry(SyncLogEntry entry) {
		
		// construct file object
		File file = new File(entry.m_file_name);
		
		// extract file name
		return file.getName();
	}
	
	/**
	 * returns true when the specified entry is from the local sync file
	 * @param entry to be checked
	 * @return boolean
	 */
	private boolean isLocalEntry(SyncLogEntry entry) {
		
		return m_source_entries.contains(entry);
	}
	
	private SyncLogEntry getEntry(String file_name, boolean is_local) {
		
		ArrayList<SyncLogEntry> entries;
		if (is_local)
			entries = m_source_entries;
		else
			entries = m_target_entries;
		
		for(SyncLogEntry entry : entries)
		{
			if (entry.m_file_name.equals(file_name))
				return entry;
		}
		return null;
	}
	
	/**
	 * adds a conflict
	 * @param source_path source path
	 * @param target_path target path
	 * @param type conflict type
	 */
	private void addConflict(String source_path, String target_path, ConflictType type) {
		
		ConflictData data = new ConflictData();
		data.m_source_file = source_path;
		data.m_target_file = target_path;
		data.m_type = type;		
		if (isLocalPath(source_path)) {
			data.m_source_store = "Android";
			data.m_target_store = m_target_store_path.substring(1);
		}
		else
		{
			data.m_source_store = m_target_store_path.substring(1);
			data.m_target_store = "Android";
		}
		
		// add to conflict list
		m_conflict_list.add(data);		
		
	}
	
	
	public enum ConflictType {
		
		CREATE_CONFLICT_TYPE,
		WRITE_CONFLICT_TYPE,
		SEMANTIC_CONFLICT_TYPE
	};
	
	public class ConflictData {
		
		public String m_source_file;
		public String m_target_file;
		public ConflictType m_type;
		public String m_source_store;
		public String m_target_store;
	}
}	
