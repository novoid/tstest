package org.me.TagStore.interfaces;

/**
 * This interface is used to receive events when a file has been tagged
 * @author Johannes Anderwald
 *
 */
public interface TagEventNotification {

	/**
	 * name of the file which has been tagged
	 * @param file_name name of the file
	 */
	public abstract void notifyFileTagged(String file_name);
}
