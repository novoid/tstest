package org.me.tagstore.core;

import org.me.tagstore.ui.ToastManager;
import android.app.Application;

/*
 * This class is used to initialize the application objects
 */
public class TagstoreApplication extends Application {

	/**
	 * db manager instance
	 */
	public DBManager m_db_man;
	private FileTagUtility m_file_utility;
	private SyncFileWriter m_sync_file_writer;
	private FileLog m_file_log;
	private SyncFileLog m_sync_file_log;
	private TagValidator m_validator;
	private ToastManager m_toast_man;
	private VocabularyManager m_voc_man;
	private ConfigurationChecker m_conf_checker;
	private EventDispatcher m_event_dispatcher;
	private FileSystemObserver m_observer;
	private TagStackManager m_tag_stack;
	private IOUtils m_io_utils;
	private StorageTimerTask m_storage_timer;
	private org.me.tagstore.core.TimeFormatUtility m_time_format;
	
	
	public void onCreate() {
		
		// call base class
		super.onCreate();
		
		// initializes the db manager
		m_db_man = new DBManager();
		m_db_man.initializeDBManager(TagstoreApplication.this);
		
		// construct tag validator
		m_validator = new TagValidator();
		
		// construct sync file log
		m_sync_file_log = new SyncFileLog();
				
		// get file writer
		m_sync_file_writer = new SyncFileWriter();
		m_sync_file_writer.setDBManagerAndFileLog(m_db_man, m_sync_file_log);
		
		// io utils
		m_io_utils = new IOUtils();
		
		// build toast manager
		m_toast_man = new ToastManager();
		m_toast_man.initializeToastManager(TagstoreApplication.this);
		
		// build vocabulary manager
		m_voc_man = new VocabularyManager();
		m_voc_man.initializeVocabularyManager(getApplicationContext(), m_io_utils);
		
		// build configuration checker
		m_conf_checker = new ConfigurationChecker();
		m_conf_checker.initializeConfigurationChecker(getApplicationContext());
		
		// build event manager
		m_event_dispatcher = new EventDispatcher();
		m_event_dispatcher.initialize();

		// construct time format
		m_time_format = new TimeFormatUtility();		
		
		// initialize file tag utility
		m_file_utility = new FileTagUtility();
		m_file_utility.initializeFileTagUtility(m_db_man, m_sync_file_writer, m_validator, m_toast_man, m_voc_man, m_time_format);
		
		// construct file system observer
		m_observer = new FileSystemObserver();
		
		// construct tag stack
		m_tag_stack = new TagStackManager();
		m_tag_stack.setDBManager(m_db_man);
		
		// construct storage timer task
		m_storage_timer = new StorageTimerTask();
		


	}
	
	/**
	 * returns the db manager
	 * @return
	 */
	public DBManager getDBManager() {
		return m_db_man;
	}

	public FileTagUtility getFileTagUtility() {
		return m_file_utility;
	}

	public ToastManager getToastManager() {
		return m_toast_man;
	}

	public EventDispatcher getEventDispatcher() {
		return m_event_dispatcher;
	}

	public SyncFileLog getSyncFileLog() {
		return m_sync_file_log;
	}

	public FileSystemObserver getFileSystemObserver() {
		return m_observer;
	}

	public TagValidator getTagValidator() {
		return m_validator;
	}

	public TagStackManager getTagStackManager() {
		return m_tag_stack;
	}

	public StorageTimerTask getStorageTimerTask() {
		return m_storage_timer;
	}

	public VocabularyManager getVocabularyManager() {
		return m_voc_man;
	}

	public TimeFormatUtility getTimeFormatUtility() {
		return m_time_format;
	}
	
	
}
