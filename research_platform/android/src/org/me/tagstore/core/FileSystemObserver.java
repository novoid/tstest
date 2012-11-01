package org.me.tagstore.core;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import org.me.tagstore.interfaces.FileSystemObserverNotification;

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
	private HashMap<String, FsObserver> m_observer_map = null;

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
			Logger.e("Error: FileSystemObserver::addObserver path is not a directory");
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
			// observer is already registered
			//
			return true;
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

		Logger.i("FileSystemObserver::addObserver path " + destination_path + " was added");
		
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
	 * removes an observer from the map and stops it if it exists
	 * @param path
	 * @return
	 */
	public boolean removeObserver(String path) {

		//
		// check if the path already exists
		//
		if (m_observer_map.containsKey(path)) {
			
			//
			// get the observer
			//
			FsObserver observer = m_observer_map.get(path);
			
			//
			// remove it from list
			//
			m_observer_map.remove(path);
			
			//
			// set the notification to null
			//
			observer.setNotification(null);
			
			//
			// stop the observer
			//
			observer.stopWatching();
			
			//
			// done
			//
			return true;
		}
		
		//
		// observer not found
		//
		return false;
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
		private FileSystemObserverNotification m_notification = null;

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
		}

		@Override
		public void stopWatching() {

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
		 * @param path of the file / directory to be added
		 */
		private void addFileOrDirectory(File cur_file) {

			//
			// check if this is a file
			//
			if (cur_file.isFile()) {

				//
				// notify of new file
				//
				performNotify(cur_file, FileSystemObserverNotification.NotificationType.FILE_CREATED);
			} 
		}

		/**
		 * deletes a file or directory from the internal list depending on the
		 * Android OS version.
		 * 
		 * @param path of the file / directory
		 */
		public void deleteFileOrDirectory(File cur_file) {

			//
			// check if it is a file
			//
			if (cur_file.isFile())
			{
				//
				// notify of new file
				//
				performNotify(cur_file, FileSystemObserverNotification.NotificationType.FILE_DELETED);
			}
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
				addFileOrDirectory(new File(filename));

			} else if ((event & FileObserver.DELETE) != 0) {
				//
				// remove file / directory
				//
				deleteFileOrDirectory(new File(filename));

			}
		}
	}


}
