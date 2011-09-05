package org.me.TagStore;

import java.io.File;
import java.util.ArrayList;

import org.me.TagStore.FileSystemObserverNotification.NotificationType;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class FileWatchdogService extends Service {

	/**
	 * file system observer class
	 */
	FileSystemObserver m_observer = null;

	/**
	 * notification manager
	 */
	NotificationManager m_notification_manager;

	/**
	 * stores external registered notifications
	 */
	ArrayList<FileSystemObserverNotification> m_external_observers;

	/**
	 * if notification has been registered
	 */
	boolean m_registered;

	@Override
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
		// get notification service name
		//
		String ns_name = Context.NOTIFICATION_SERVICE;

		//
		// get notification service manager instance
		//
		m_notification_manager = (NotificationManager) getSystemService(ns_name);

		//
		// create array list for external observers
		//
		m_external_observers = new ArrayList<FileSystemObserverNotification>();

	}

	protected void notificationCallback(String file_name, NotificationType type) {

		Logger.i("Received notification of : " + file_name + " Type: " + type);

		//
		// acquire database manager
		//
		DBManager db_man = DBManager.getInstance();

		if (type == NotificationType.FILE_DELETED) {
			//
			// file was deleted
			// remove file from database
			//
			db_man.removeFile(file_name, true, true);

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

		if (observed_directory_list == null) {
			//
			// no directories observed or error
			//
			Logger.i("FileWatchdogService::notificationCallback directory list is empty");

			//
			// nothing more to do
			//
			return;
		}

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
		// FIXME: check if notifications are not activated
		//
		addStatusBarNotification(file_name);
	}

	/**
	 * adds a notification to the status bar
	 * 
	 * @param filename
	 *            to be added
	 */
	private void addStatusBarNotification(final String filename) {

		//
		// construct new notification
		//
		Notification notification = new Notification(R.drawable.tagstore,
				"TagStore", System.currentTimeMillis());

		//
		// get application context
		//
		Context context = getApplicationContext();

		//
		// set notification title
		//
		CharSequence contentTitle = "New file";

		//
		// set notification text
		//
		CharSequence contentText = "Click on file to handle event";

		//
		// construct new intent
		//
		Intent notificationIntent = new Intent(getApplicationContext(),
				MainActivity.class);

		//
		// construct pending intent
		//
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

		//
		// setup notification details
		//
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		//
		// pass notification to manager
		//
		m_notification_manager.notify(ConfigurationSettings.NOTIFICATION_ID,
				notification);

		//
		// done
		//
		Logger.i("Notified notification manager");
	}

	@Override
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

		if (m_registered == false) {
			//
			// get path to be observed (external disk)
			//
			String path = android.os.Environment.getExternalStorageDirectory()
					.getAbsolutePath();

			//
			// register path
			//
			m_registered = m_observer.addObserver(path,
					new FileSystemObserverNotification() {

						@Override
						public void notify(String file_name,
								NotificationType type) {
							notificationCallback(file_name, type);

						}
					});

			Logger.i("Logging path: " + path + " first time: " + m_registered);

		} else {
			//
			// another superflous registration request
			//
			Logger.e("FileWatchdogService::onStartCommand another registration request");
		}
	}

	@Override
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

	@Override
	public IBinder onBind(Intent intent) {

		//
		// starts the service
		//
		internalServiceStartup();

		return new FileWatchdogServiceBinder<FileWatchdogService>(this);
	}
}
