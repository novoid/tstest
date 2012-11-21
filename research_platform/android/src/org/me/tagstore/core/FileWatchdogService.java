package org.me.tagstore.core;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.me.tagstore.R;
import org.me.tagstore.interfaces.FileSystemObserverNotification;
import org.me.tagstore.ui.StatusBarNotification;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

public class FileWatchdogService extends Service implements
		org.me.tagstore.core.StorageTimerTask.TimerTaskCallback {

	/**
	 * file system observer class
	 */
	private FileSystemObserver m_observer = null;

	/**
	 * stores external registered notifications
	 */
	private LinkedHashSet<FileSystemObserverNotification> m_external_observers;

	/**
	 * if notification has been registered
	 */
	private boolean m_registered;

	/**
	 * notification handle
	 */
	private FileSystemObserverNotification m_notification;

	/**
	 * stores the sync active status
	 */
	private boolean m_sync_active;

	/**
	 * holds the status bar notification
	 */
	private StatusBarNotification m_status_bar;

	/**
	 * service has been created
	 */
	private boolean m_service_created;

	public void onCreate() {

		Logger.e("FileWatchdogService::onCreate");
		//
		// call the base class
		//
		super.onCreate();

		//
		// acquire instance
		//
		m_observer = FileSystemObserver.acquireInstance();

		//
		// create array list for external observers
		//
		m_external_observers = new LinkedHashSet<FileSystemObserverNotification>();

		//
		// create notification
		//
		m_notification = new FileSystemObserverNotification() {

			public void notify(String file_name, NotificationType type) {
				notificationCallback(file_name, type);

			}
		};

		//
		// construct bar notification
		//
		m_status_bar = new StatusBarNotification();
		m_status_bar.initializeStatusBarNotification(getApplicationContext());

		//
		// for debugging
		//
		m_service_created = true;
	}

	/**
	 * returns true when the service has been created
	 * 
	 * @return
	 */
	public boolean isServiceCreated() {
		return m_service_created;
	}

	/**
	 * returns true when a sync is active
	 */
	public boolean isSyncActive() {

		String file_path = Environment.getExternalStorageDirectory()
				+ File.separator + ConfigurationSettings.TAGSTORE_DIRECTORY
				+ File.separator + getString(R.string.storage_directory)
				+ File.separator
				+ ConfigurationSettings.TAGSTORE_LOCK_FILE_NAME;

		File file = new File(file_path);
		return file.exists();
	}

	protected void notificationCallback(
			String file_name,
			org.me.tagstore.interfaces.FileSystemObserverNotification.NotificationType type) {

		Logger.i("Received notification of : " + file_name + " Type: " + type);

		//
		// acquire database manager
		//
		DBManager db_man = DBManager.getInstance();

		if (isSyncActive()) {

			// ignore file changes
			m_sync_active = true;
			return;
		} else {
			// was the sync active
			if (m_sync_active) {

				// sync no longer is active
				m_sync_active = false;

				// get list of new files
				DirectoryChangeWorker worker = new DirectoryChangeWorker();
				FileTagUtility utility = new FileTagUtility();
				utility.initializeFileTagUtility();
				worker.initializeDirectoryChangeWorker(DBManager.getInstance(),
						utility, new PendingFileChecker());

				// get list of new files
				ArrayList<String> files = worker.getAllNewFiles();
				if (files.size() != 0)
					Logger.e("FIXME: need to add untagged files");

			}

		}

		if (type == org.me.tagstore.interfaces.FileSystemObserverNotification.NotificationType.FILE_DELETED) {

			//
			// file was deleted
			// remove file from store
			//
			db_man.removeFile(file_name);
			db_man.removePendingFile(file_name);

			//
			// notify external observers
			//
			for (FileSystemObserverNotification notification : m_external_observers) {
				//
				// performing notification
				//
				notification.notify(file_name, type);
			}

			//
			// done
			//
			return;
		}

		//
		// file was created, construct file object
		//
		File new_file = new File(file_name);

		//
		// get parent directory of file
		//
		String parent_directory = new_file.getParent();

		//
		// get observed list
		//
		ArrayList<String> observed_directory_list = db_man.getDirectories();

		//
		// loop list to see if the change was from an observed directory
		//
		boolean found = false;
		for (String current_directory : observed_directory_list) {
			//
			// compare each entry
			//
			Logger.i("FileWatchDogService::notificationCallback> current entry "
					+ current_directory
					+ " parent directory: "
					+ parent_directory);
			if (current_directory.compareTo(parent_directory) == 0) {
				//
				// found matching entry
				//
				found = true;
				break;
			}
		}

		if (!found) {
			//
			// entry not found
			//
			Logger.i("FileWatchdogService::notificationCallback> new file not in observed list");
			return;
		}

		//
		// file is new and it is in observed directory list
		// put it into pending file list
		//
		db_man.addPendingFile(file_name);

		//
		// notify external observers
		//
		for (FileSystemObserverNotification notification : m_external_observers) {
			//
			// performing notification
			//
			notification.notify(file_name, type);
		}

		//
		// are notification settings enabled
		//
		if (m_status_bar.isStatusBarNotificationEnabled()) {
			//
			// add tool bar notification
			//
			m_status_bar.addStatusBarNotification(file_name);
		}
	}

	public void onDestroy() {
		Logger.i("FileWatchdogService::onDestroy");
		//
		// cleanup observer
		//
		m_observer.removeAllObservers();

		//
		// call base method
		//
		super.onDestroy();
	}

	/**
	 * starts the service
	 */
	private void internalServiceStartup() {

		//
		// register with the storage timer task
		//
		Logger.i("internalServiceStartup");

		if (Environment.getExternalStorageState().compareTo(
				Environment.MEDIA_MOUNTED) == 0
				|| Environment.getExternalStorageState().compareTo(
						Environment.MEDIA_MOUNTED_READ_ONLY) == 0) {

			//
			// directly start service
			//
			diskAvailable();
		} else {
			//
			// external disk not available
			// let's try later
			//
			StorageTimerTask.acquireInstance().addCallback(this);
		}
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		Logger.i("FileWatchdogService::onStartCommand flags " + flags
				+ " startId " + startId + " registered: " + m_registered);

		//
		// start service
		//
		internalServiceStartup();

		return START_STICKY;
	}

	/**
	 * registers an external observer, which will get notified when a change in
	 * observed directories occur
	 * 
	 * @param notification
	 *            to be invoked for callback
	 */
	public boolean registerExternalNotification(
			FileSystemObserverNotification notification) {

		//
		// add to external observer list
		//
		return m_external_observers.add(notification);
	}

	/**
	 * removes an registered external notification
	 */
	public boolean unregisterExternalNotification(
			FileSystemObserverNotification notification) {

		//
		// remove
		//
		return m_external_observers.remove(notification);

	}

	public IBinder onBind(Intent intent) {

		//
		// starts the service
		//
		internalServiceStartup();

		//
		// construct service binder
		//
		FileWatchdogServiceBinder<FileWatchdogService> binder = new FileWatchdogServiceBinder<FileWatchdogService>();
		binder.initializeFileWatchdogServiceBinder(FileWatchdogService.this);

		return binder;
	}

	public boolean diskAvailable() {

		//
		// are we already registered
		//
		if (!m_registered) {
			//
			// get observed directories from the database
			//
			DBManager db_man = DBManager.getInstance();
			ArrayList<String> observed_directory_list = db_man.getDirectories();
			if (observed_directory_list != null) {

				//
				// add each directory
				//
				for (String path : observed_directory_list) {

					Logger.i("observing directory: " + path);
					boolean result = m_observer.addObserver(path,
							m_notification);
					if (!result) {
						Logger.e("Error: failed to add observer for path:"
								+ path);
					}
				}

				//
				// we are now registered
				//
				m_registered = true;
			}
		}

		//
		// continue scheduling
		//
		return true;
	}

	public boolean diskNotAvailable() {

		//
		// informal debug print
		//
		// Logger.i("diskAvailable(): " + m_registered);

		if (m_registered) {
			//
			// unregisters all available observers
			//
			m_observer.removeAllObservers();

			//
			// set to unregistered
			//
			m_registered = false;
		}

		//
		// continue scheduling
		//
		return true;

	}

	/**
	 * adds a new directory to be observed
	 * 
	 * @param path
	 * @return
	 */
	public boolean registerDirectory(String path) {

		if (m_registered) {
			//
			// only register the directory when external disk is available
			// if the disk is not available, it will be added when the disk
			// becomes available later
			// Reason: no observers are active when the disk is not mounted
			// see diskAvailable callback of StorageTimerTask
			return m_observer.addObserver(path, m_notification);
		}

		//
		// no disk available
		//
		return false;
	}

	/**
	 * removes a path from the observed list
	 * 
	 * @param path
	 *            to be removed
	 * @return true on success
	 */
	public boolean unregisterDirectory(String path) {

		if (m_registered) {
			//
			// unregister observer
			//
			return m_observer.removeObserver(path);
		}

		//
		// no observers currently active
		//
		return false;
	}

}
