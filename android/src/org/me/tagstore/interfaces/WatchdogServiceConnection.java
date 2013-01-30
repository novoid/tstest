package org.me.tagstore.interfaces;


public interface WatchdogServiceConnection {

	/**
	 * unregisters the external observers
	 * @param notification to be unregistered
	 */
	public abstract boolean unregisterExternalNotification(
			FileSystemObserverNotification notification);

	/**
	 * registers an external observer
	 * @param notification to be registered
	 * @return true on success
	 */
	public abstract boolean registerExternalNotification(
			FileSystemObserverNotification notification);

	/**
	 * registers a new directory to be observed
	 * @param path to be observed
	 */
	public abstract boolean registerDirectory(String path);

	/**
	 * unregisters the directory to be observed
	 * @param path to be unregisterd
	 * @return true on success
	 */
	public abstract boolean unregisterDirectory(String path);

}