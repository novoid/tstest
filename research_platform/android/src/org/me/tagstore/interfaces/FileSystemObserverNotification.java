package org.me.tagstore.interfaces;

/**
 * This class is used for notifications for file system changes. When a
 * directory is registered for change, it also registers a
 * FileSystemObserverNotification. This notification is then invoked when a file
 * is created or changed.
 * 
 * @author Johannes Anderwald
 * 
 */
public interface FileSystemObserverNotification {

	/**
	 * The enumeration describes the different types of changes
	 */
	public enum NotificationType {

		FILE_CREATED, FILE_DELETED
	}

	/**
	 * Notifies a client of a file system change
	 * 
	 * @param file_name
	 *            name of the file which has changed
	 * @param type
	 *            of change
	 */
	public void notify(String file_name, NotificationType type);
}
