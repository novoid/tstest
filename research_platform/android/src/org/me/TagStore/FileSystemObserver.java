package org.me.TagStore;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import android.os.FileObserver;

/**
 * This class is used to watch the file system for notifications when a file is
 * created or deleted. The class is used in a singleton pattern
 * 
 * @author Johannes Anderwald
 * 
 */
public class FileSystemObserver {

	/**
	 * stores the mapping between path and notification
	 */
	HashMap<String, FsObserver> m_observer_map = null;

	/**
	 * stores the static instance of FileSystemObserver
	 */
	static private final FileSystemObserver s_instance = new FileSystemObserver();

	/**
	 * private constructor of class FileSystemObserver
	 */
	private FileSystemObserver() {

		//
		// allocate observer map
		//
		m_observer_map = new HashMap<String, FsObserver>();
	}

	/**
	 * acquires an instance of FileSystemObserver
	 * 
	 * @return
	 */
	public static FileSystemObserver acquireInstance() {

		//
		// returns the instance
		//
		return s_instance;
	}

	/**
	 * this function adds an observer. When a file is changed or deleted the
	 * notification is invoked
	 * 
	 * @param destination_path
	 *            path to observer
	 * @param notification
	 *            which receives a callback
	 * @return
	 */
	public boolean addObserver(String destination_path,
			FileSystemObserverNotification notification) {

		//
		// check for invalid parameters
		//
		if (destination_path == null || notification == null) {

			//
			// invalid parameters
			//
			Logger.e("Error: FileSystemObserver::addObserver invalid parameters");

			return false;
		}

		//
		// check if path is valid
		//
		File file = new File(destination_path);

		//
		// check if the path is a directory
		//
		if (!file.isDirectory()) {
			//
			// path must be a directory
			//
			Logger.e("Error: FileSystemObserver::addObserver directory does not exist");
			return false;
		}

		if (!file.exists()) {
			//
			// error file does not exist
			//
			Logger.e("Error: FileSystemObserver::addObserver directory "
					+ destination_path + " does not exist");
			return false;
		}

		//
		// check if the path already exists
		//
		if (m_observer_map.containsKey(destination_path)) {
			//
			// destination path already exists
			//
			FsObserver observer = m_observer_map.get(destination_path);

			//
			// stop observer
			//
			observer.stopWatching();

			//
			// update notification
			//
			observer.setNotification(notification);

			//
			// restart watching
			//
			observer.startWatching();

			return false;
		}

		//
		// construct new observer
		//
		FsObserver observer = new FsObserver(destination_path, notification);

		//
		// store in destination map
		//
		m_observer_map.put(destination_path, observer);

		//
		// start watching now
		//
		observer.startWatching();

		//
		// done
		//
		return true;
	}

	/**
	 * goes through all observers and stops them
	 */
	public void removeAllObservers() {

		Collection<FsObserver> list = m_observer_map.values();

		//
		// stop all sub observers
		//
		for (FsObserver observer : list)
			observer.stopWatching();

		//
		// now clear the list
		//
		m_observer_map.clear();

	}

	/**
	 * This class is used internally for receiving call backs of the
	 * FileObserver class. It then analyzes the action and in the case it is
	 * accepted invokes the registered call back.
	 * 
	 * @author Johannes Anderwald
	 * 
	 */
	private class FsObserver extends FileObserver {

		/**
		 * invokes the notification
		 */
		FileSystemObserverNotification m_notification = null;

		/*
		 * stores all sub observers
		 */
		HashMap<String, FsObserver> m_sub_observers = null;

		/*
		 * stores the current directory
		 */
		File m_path = null;

		/**
		 * constructor of class FsObserver
		 * 
		 * @param destination_path
		 * @param notification
		 *            notification will receives a callback when a directory
		 *            changes
		 */
		FsObserver(String destination_path,
				FileSystemObserverNotification notification) {
			super(destination_path);

			//
			// store the notification
			//
			m_notification = notification;

			//
			// store current directory
			//
			m_path = new File(destination_path).getAbsoluteFile();

			m_sub_observers = new HashMap<String, FsObserver>();

			//
			// scan directory now
			//
			scanDirectory(m_path, false);

		}

		@Override
		public void stopWatching() {

			//
			// notify all sub observers that they should stop
			//
			Collection<FsObserver> list = m_sub_observers.values();

			if (list != null) {
				for (FsObserver observer : list)
					observer.stopWatching();
			}

			//
			// now stop this observer
			//
			super.stopWatching();
		}

		/**
		 * sets the notification member
		 * 
		 * @param notification
		 */

		public void setNotification(FileSystemObserverNotification notification) {

			//
			// store notification
			//
			m_notification = notification;

			//
			// update all sub observers too
			//
			for (FsObserver observer : m_sub_observers.values()) {
				//
				// update notification
				//
				observer.setNotification(notification);
			}

		}

		/**
		 * scans a directory for files and constructs new observers for sub
		 * directories and puts them into sub observer list
		 * 
		 * @param cur_path
		 *            current path to be scanned
		 * @param should_notify
		 *            if true then notification is invoked
		 */
		private void scanDirectory(File cur_path, boolean should_notify) {

			//
			// now collect all files present in this directory
			//
			File[] files = m_path.listFiles();
			if (files != null) {
				for (File cur_file : files) {
					addFileOrDirectory(cur_file, should_notify);
				}
			}
		}

		private void performNotify(
				File cur_file,
				FileSystemObserverNotification.NotificationType notification_type) {

			if (m_notification != null) {
				Logger.i("performNotify: " + cur_file + " Type: "
						+ notification_type);
				m_notification.notify(cur_file.getPath(), notification_type);
			}
		}

		/**
		 * adds a file or directory to the internal list depending on the
		 * Android OS version
		 * 
		 * @param path
		 *            of the file / directory to be added
		 */
		private void addFileOrDirectory(File cur_file, boolean should_notify) {

			//
			// check if this is a file
			//
			if (cur_file.isFile()) {

				//
				// should notification be invoked
				//
				if (should_notify) {
					//
					// notify of new file
					//
					performNotify(
							cur_file,
							FileSystemObserverNotification.NotificationType.FILE_CREATED);
				}
			} else if (cur_file.isDirectory()) {
				//
				// check for duplicate notification
				//
				if (!m_sub_observers.containsKey(cur_file.getPath())) {
					Logger.i("CREATE DIR: " + cur_file);
					//
					// construct new observer
					//
					FsObserver sub_observer = new FsObserver(
							cur_file.getPath(), m_notification);

					//
					// add new sub observer
					//
					m_sub_observers.put(cur_file.getPath(), sub_observer);

					//
					// start the new sub observer
					//
					sub_observer.startWatching();
				} else {
					Logger.i("path is present: " + cur_file);
					printSubobservers();
				}
			}
		}

		/**
		 * deletes a file or directory from the internal list depending on the
		 * Android OS version.
		 * 
		 * @param path
		 *            of the file / directory
		 * @param should_notify
		 *            if notification should be triggered
		 */
		public void deleteFileOrDirectory(File cur_file, boolean should_notify) {

			if (m_sub_observers.containsKey(cur_file.getPath())) {
				Logger.i("DELETE DIR: " + cur_file + " current path: " + m_path);

				//
				// notify observer that it got deleted
				//
				FsObserver observer = m_sub_observers.get(cur_file.getPath());

				//
				// notify delete will trigger delete notifications for all files
				// present in the observer and its sub-observers
				//
				observer.notifyDelete(should_notify);

				//
				// remove observer
				//
				m_sub_observers.remove(cur_file.getPath());

				//
				// stop watching now
				//
				observer.stopWatching();

				//
				// done
				//
				return;
			}

			//
			// check if notification should be invoked
			//
			if (should_notify) {
				//
				// perform notification
				//
				performNotify(
						cur_file,
						FileSystemObserverNotification.NotificationType.FILE_DELETED);
			}
		}

		/**
		 * notifies an observer, that its watched directory got deleted
		 * 
		 * @param should_notify
		 *            if notifications should be invoked
		 */
		public void notifyDelete(boolean should_notify) {

			//
			// now iterate through all observers and stop watching
			//
			Collection<FsObserver> list = m_sub_observers.values();

			for (FsObserver cur_observer : list) {
				//
				// notify of deletion
				//
				cur_observer.notifyDelete(should_notify);

				//
				// stop watching sub directories
				//
				cur_observer.stopWatching();
			}

			//
			// clear sub observer list
			//
			m_sub_observers.clear();

		}

		private void printSubobservers() {

			for (FsObserver observer : m_sub_observers.values())
				Logger.i("printSubobservers Path: " + observer.m_path);

		}

		/**
		 * callback routine of FileObserver
		 */
		@Override
		public void onEvent(int event, String path) {

			//
			// construct full path
			//
			String cur_directory = m_path.getPath();

			//
			// add path seperator
			//
			cur_directory += File.separator;

			//
			// add filename
			//
			String filename = cur_directory + path;

			//
			// check notification type
			//
			if ((event & FileObserver.CREATE) != 0) {

				//
				// add file
				//
				addFileOrDirectory(new File(filename), true);

			} else if ((event & FileObserver.DELETE) != 0) {
				//
				// remove file / directory
				//
				deleteFileOrDirectory(new File(filename), true);

			}
		}
	};
}
